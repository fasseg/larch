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
import net.objecthunter.larch.source.InternalBinarySource;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DefaultEntityService implements EntityService {
    private static final Logger log = LoggerFactory.getLogger(DefaultEntityService.class);

    @Autowired
    private BlobstoreService blobstoreService;

    @Autowired
    private IndexService indexService;

    @Override
    public void create(Entity e) throws IOException {
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
        this.indexService.create(e);
        log.debug("finished creating Entity {}", e.getId());
    }

    private void createAndMutateBinary(Binary b) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (final DigestInputStream src = new DigestInputStream(b.getSource().getInputStream(), digest)) {
            final String path = this.blobstoreService.create(src);
            final String checksum = new BigInteger(1, digest.digest()).toString(16);
            b.setChecksum(checksum);
            b.setSize(digest.getDigestLength());
            b.setChecksumType(digest.getAlgorithm());
            b.setSource(new InternalBinarySource(path));
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

    }

    @Override
    public Entity retrieve(Entity e) throws IOException {
        return null;
    }

    @Override
    public Entity delete(Entity e) throws IOException {
        return null;
    }
}
