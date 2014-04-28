/* 
* Copyright 2014 Frank Asseg
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/
package net.objecthunter.larch.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import net.objecthunter.larch.helpers.SizeCalculatingDigestInputStream;
import net.objecthunter.larch.model.*;
import net.objecthunter.larch.service.*;
import net.objecthunter.larch.service.elasticsearch.ElasticSearchIndexService;
import net.objecthunter.larch.model.source.UrlSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The default implementation of {@link net.objecthunter.larch.service.EntityService} responsible for perofrming CRUD
 * operations of {@link net.objecthunter.larch.model.Entity} objects
 */
public class DefaultEntityService implements EntityService {
    private static final Logger log = LoggerFactory.getLogger(DefaultEntityService.class);

    @Autowired
    private BlobstoreService blobstoreService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ExportService exportService;

    @Autowired
    private Environment env;

    private boolean autoExport;

    @PostConstruct
    public void init() {
        final String val = env.getProperty("larch.export.auto");
        autoExport = val == null ? false : Boolean.valueOf(val);
    }

    @Override
    public String create(Entity e) throws IOException {
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        if (e.getId() == null || e.getId().isEmpty()) {
            e.setId(generateId());
        } else {
            if (this.indexService.exists(e.getId())) {
                throw new IOException("Entity with id " + e.getId() + " could not be created because it already exists in the index");
            }
        }
        if (e.getMetadata() != null) {
            for (final Metadata md : e.getMetadata().values()) {
                md.setUtcCreated(now);
                md.setUtcLastModified(now);
            }
        }
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed entity");
        }
        if (e.getBinaries() != null) {
            for (final Binary b : e.getBinaries().values()) {
                createAndMutateBinary(e.getId(), b);
            }
        }
        e.setState(ElasticSearchIndexService.STATE_INGESTED);
        e.setVersion(1);
        e.setUtcCreated(now);
        e.setUtcLastModified(now);
        final String id = this.indexService.create(e);
        log.debug("finished creating Entity {}", id);

        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", id);
        }
        return id;
    }

    private void createAndMutateBinary(String entityId, Binary b) throws IOException {
        if (b.getSource() == null) {
            log.warn("No source is set for binary '{}' of entity '{}' nothing to ingest", b.getName(), entityId);
            return;
        }
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (final SizeCalculatingDigestInputStream src = new SizeCalculatingDigestInputStream(b.getSource().getInputStream(),
                digest)) {
            final ZonedDateTime created = ZonedDateTime.now(ZoneOffset.UTC);
            final String path = this.blobstoreService.create(src);
            final String checksum = new BigInteger(1, digest.digest()).toString(16);
            b.setChecksum(checksum);
            b.setSize(src.getBytesRead());
            b.setChecksumType(digest.getAlgorithm());
            b.setPath(path);
            b.setSource(new UrlSource(URI.create("http://localhost:8080/entity/" + entityId + "/binary/" + b.getName() + "/content"), true));
            final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
            b.setUtcCreated(now);
            b.setUtcLastModified(now);
        }
    }


    private String generateId() throws IOException {
        String generated;
        do {
            generated = RandomStringUtils.randomAlphabetic(16);
        } while (indexService.exists(generated));
        return generated;
    }

    @Override
    public void update(Entity e) throws IOException {
        final Entity oldVersion = this.indexService.retrieve(e.getId());
        final String oldVersionPath = this.blobstoreService.createOldVersionBlob(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        e.setVersion(oldVersion.getVersion() + 1);
        if (oldVersion.getVersionPaths() == null) {
            e.setVersionPaths(new HashMap<>());
        } else {
            e.setVersionPaths(oldVersion.getVersionPaths());
        }
        if (e.getMetadata() != null) {
            for (final Metadata md : e.getMetadata().values()) {
                if (md.getUtcCreated() == null) {
                    md.setUtcCreated(now);
                }
                md.setUtcLastModified(now);
            }
        }
        e.getVersionPaths().put(oldVersion.getVersion(), oldVersionPath);
        e.setUtcCreated(oldVersion.getUtcCreated());
        e.setUtcLastModified(now);
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed entity");
        }
        if (e.getBinaries() != null) {
            for (final Binary b : e.getBinaries().values()) {
                if (b.getSource() == null) {
                    log.warn("No source on binary '{}' of entity '{}'", b.getName(), e.getId());
                    continue;
                }
                if (b.getSource().isInternal()) {
                    b.setUtcLastModified(oldVersion.getBinaries().get(b.getName()).getUtcLastModified());
                    b.setUtcCreated(oldVersion.getBinaries().get(b.getName()).getUtcCreated());
                } else {
                    createAndMutateBinary(e.getId(), b);
                }
            }
        }
        this.indexService.update(e);
        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", e.getId());
        }
    }

    @Override
    public Entity retrieve(String id) throws IOException {
        return indexService.retrieve(id);
    }

    @Override
    public void delete(String id) throws IOException {
        final Entity e = indexService.retrieve(id);
        for (Binary b : e.getBinaries().values()) {
            if (b.getPath() != null && !b.getPath().isEmpty()) {
                blobstoreService.delete(b.getPath());
            }
        }
        this.indexService.delete(id);
    }

    @Override
    public InputStream getContent(String id, String name) throws IOException {
        final Entity e = indexService.retrieve(id);
        final Binary b = e.getBinaries().get(name);
        return blobstoreService.retrieve(b.getPath());
    }

    @Override
    public Entity retrieve(String id, int i) throws IOException {
        final Entity e = this.indexService.retrieve(id);
        if (i == e.getVersion()) {
            return e; // the current version
        }
        if (e.getVersionPaths() == null || !e.getVersionPaths().containsKey(i)) {
            throw new IOException("Unknown version " + i + " for Entity " + id);
        }
        return mapper.readValue(this.blobstoreService.retrieveOldVersionBlob(e.getVersionPaths().get(i)), Entity.class);
    }

    @Override
    public void createBinary(String entityId, String name, String contentType, InputStream inputStream) throws IOException {
        final Entity e = indexService.retrieve(entityId);
        final String oldVersionPath = this.blobstoreService.createOldVersionBlob(e);
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e1) {
            throw new IOException(e1);
        }
        try (final SizeCalculatingDigestInputStream src =new SizeCalculatingDigestInputStream(inputStream, digest)) {
            final String path = blobstoreService.create(src);
            final Binary b = new Binary();
            final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
            b.setUtcCreated(now);
            b.setUtcLastModified(now);
            b.setName(name);
            b.setMimetype(contentType);
            b.setChecksum(new BigInteger(1, digest.digest()).toString(16));
            b.setChecksumType("MD5");
            b.setSize(src.getBytesRead());
            b.setSource(new UrlSource(URI.create("http://localhost:8080/entity/" + entityId + "/binary/" + name + "/content"), true));
            b.setPath(path);
            b.setUtcCreated(now);
            b.setUtcLastModified(now);
            if (e.getBinaries() == null) {
                e.setBinaries(new HashMap<>(1));
            }
            e.getBinaries().put(name, b);
            if (e.getVersionPaths() == null) {
                e.setVersionPaths(new HashMap<>());
            }
            e.getVersionPaths().put(e.getVersion(), oldVersionPath);
            e.setVersion(e.getVersion() + 1);
            this.indexService.update(e);
        }
        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", e.getId());
        }
    }

    @Override
    public void patch(final String id, final JsonNode node) throws IOException {
        final Entity e = this.indexService.retrieve(id);
        final Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().getNodeType() != JsonNodeType.STRING) {
                throw new IOException("The patch data is invalid");
            }
            switch (field.getKey()) {
                case "label":
                    e.setLabel(field.getValue().asText());
                    break;
                case "type":
                    e.setType(field.getValue().asText());
                    break;
                case "parentId":
                    final String parentId = field.getValue().asText();
                    if (parentId.equals(id)) {
                        throw new IOException("Can not add a parent relation to itself");
                    }
                    e.setParentId(parentId);
                    break;
                case "state":
                    final String state = field.getValue().asText();
                    e.setState(state);
                    break;
                default:
                    throw new IOException("Unable to update field " + field.getKey());
            }
        }
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed Entity");
        }
        update(e);
    }

    @Override
    public void createRelation(String id, String predicate, String object) throws IOException {
        if (object.startsWith("<" + LarchConstants.NAMESPACE_LARCH)) {
            // the object is an internal entity
            final String objId = object.substring(1 + LarchConstants.NAMESPACE_LARCH.length(), object.length() - 1);
            if (!this.indexService.exists(objId)) {
                throw new FileNotFoundException("The entity " + object + " referenced in the object of the relation does not exist in the repository");
            }
        }
        final Entity oldVersion = this.indexService.retrieve(id);
        final String oldVersionPath = this.blobstoreService.createOldVersionBlob(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        final Entity newVersion = oldVersion;
        newVersion.setUtcLastModified(now);
        if (newVersion.getVersionPaths() == null) {
            newVersion.setVersionPaths(new HashMap<>(1));
        }
        newVersion.getVersionPaths().put(oldVersion.getVersion(), oldVersionPath);
        newVersion.setVersion(oldVersion.getVersion() + 1);
        if (newVersion.getRelations() == null) {
            newVersion.setRelations(new HashMap<>());
        }
        if (newVersion.getRelations().get(predicate) == null) {
            newVersion.getRelations().put(predicate, new ArrayList<>(1));
        }
        newVersion.getRelations().get(predicate).add(object);
        this.indexService.update(newVersion);
    }

}
