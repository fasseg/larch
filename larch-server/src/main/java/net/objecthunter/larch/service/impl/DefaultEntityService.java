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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.elasticsearch.ElasticSearchIndexService;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.service.BlobstoreService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.ExportService;
import net.objecthunter.larch.service.IndexService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;

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
        if (e.getId() == null || e.getId().isEmpty()) {
            e.setId(generateId());
        } else {
            if (this.indexService.exists(e.getId())) {
                throw new IOException("Entity with id " + e.getId() + " could not be created because it already exists in the index");
            }
        }
        if (e.getBinaries() != null) {
            for (final Binary b : e.getBinaries().values()) {
                createAndMutateBinary(e.getId(), b);
            }
        }
        e.setState(ElasticSearchIndexService.STATE_INGESTED);
        e.setVersion(1);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
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
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (final DigestInputStream src = new DigestInputStream(b.getSource().getInputStream(), digest)) {
            final ZonedDateTime created = ZonedDateTime.now(ZoneOffset.UTC);
            final String path = this.blobstoreService.create(src);
            final String checksum = new BigInteger(1, digest.digest()).toString(16);
            b.setChecksum(checksum);
            b.setSize(digest.getDigestLength());
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
        e.setVersion(oldVersion.getVersion() + 1);
        e.setVersionPaths(oldVersion.getVersionPaths() != null ? oldVersion.getVersionPaths() : new HashMap<Integer, String>(1));
        e.getVersionPaths().put(oldVersion.getVersion(), oldVersionPath);
        e.setUtcCreated(oldVersion.getUtcCreated());
        e.setUtcLastModified(ZonedDateTime.now(ZoneOffset.UTC).toString());
        if (e.getBinaries() != null) {
            for (final Binary b : e.getBinaries().values()) {
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
            blobstoreService.delete(b.getPath());
        }
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
        return mapper.readValue(this.blobstoreService.retrieveOldVersionBlob(e.getVersionPaths().get(i)), Entity.class);
    }

    @Override
    public void createBinary(String entityId, String name, String contentType, InputStream inputStream) throws IOException {
        final Entity e = indexService.retrieve(entityId);
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e1) {
            throw new IOException(e1);
        }
        String path = blobstoreService.create(new DigestInputStream(inputStream, digest));
        final Binary b = new Binary();
        b.setName(name);
        b.setMimetype(contentType);
        b.setChecksum(new BigInteger(1, digest.digest()).toString(16));
        b.setChecksumType("MD5");
        b.setSource(new UrlSource(URI.create("http://localhost:8080/entity/" + entityId + "/binary/" + name + "/content"), true));
        b.setPath(path);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        b.setUtcCreated(now);
        b.setUtcLastModified(now);
        if (e.getBinaries()== null) {
            e.setBinaries(new HashMap<>(1));
        }
        e.getBinaries().put(name, b);
        indexService.update(e);
    }

}
