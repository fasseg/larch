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

import net.objecthunter.larch.elasticsearch.ElasticSearchIndexService;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.BlobstoreService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.IndexService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DefaultEntityService implements EntityService {
    private static final Logger log = LoggerFactory.getLogger(DefaultEntityService.class);

    @Autowired
    private BlobstoreService blobstoreService;

    @Autowired
    private IndexService indexService;

    @Override
    public String create(Entity e) throws IOException {
        if (e.getId() == null || e.getId().isEmpty()) {
            e.setId(generateId());
        } else {
            if (this.indexService.exists(e.getId())) {
                throw new IOException("Entity with id " + e.getId() + " could not be created because it already exists in the index");
            }
        }
        for (final Binary b : e.getBinaries().values()) {
            createAndMutateBinary(b);
        }
        e.setState(ElasticSearchIndexService.STATE_INGESTED);
        final String id = this.indexService.create(e);
        log.debug("finished creating Entity {}", id);
        return id;
    }

    private void createAndMutateBinary(Binary b) throws IOException {
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
            b.setUtcCreated(created);
            b.setUtcLastModified(created);
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
        for (final Binary b : e.getBinaries().values()) {
            createAndMutateBinary(b);
        }
        this.indexService.update(e);
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

}
