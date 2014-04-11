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
package net.objecthunter.larch.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.Entity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.ocsp.Req;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Timed;

import java.time.Duration;
import java.time.ZonedDateTime;

import static net.objecthunter.larch.integration.helpers.Fixtures.*;
import static org.junit.Assert.*;

public class EntityControllerIT extends AbstractLarchIT {
    private static final Logger log = LoggerFactory.getLogger(EntityControllerIT.class);

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testCreateAndUpdateEntity() throws Exception {
        HttpResponse resp = Request.Post("http://localhost:8080/entity")
                .bodyString(mapper.writeValueAsString(createFixtureEntity()), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        Entity update = createFixtureEntity();
        update.setLabel("My updated Label");
        resp = Request.Put("http://localhost:8080/entity/" + id)
                .bodyString(mapper.writeValueAsString(update), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());

        resp = Request.Get("http://localhost:8080/entity/" + id)
                .execute()
                .returnResponse();
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNotNull(fetched.getVersionPaths());
        assertEquals(1, fetched.getVersionPaths().size());
        assertEquals(2, fetched.getVersion());

        resp = Request.Get("http://localhost:8080/entity/" + id + "/version/1")
                .execute()
                .returnResponse();
        Entity oldVersion = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNull(oldVersion.getVersionPaths());
        assertEquals(1, oldVersion.getVersion());
        assertNotNull(oldVersion.getUtcCreated());
        assertNotNull(oldVersion.getUtcLastModified());
        assertTrue(Duration.between(ZonedDateTime.parse(oldVersion.getUtcLastModified()), ZonedDateTime.parse(fetched.getUtcLastModified())).getNano() > 0);
        assertEquals(ZonedDateTime.parse(oldVersion.getUtcCreated()), ZonedDateTime.parse(fetched.getUtcCreated()));
        oldVersion.getBinaries().values().forEach(b -> {
            assertNotNull(b.getUtcCreated());
            assertNotNull(b.getUtcLastModified());
        });
        fetched.getBinaries().values().forEach(b -> {
            assertNotNull(b.getUtcCreated());
            assertNotNull(b.getUtcLastModified());
        });
    }

    @Test
    public void testCreateAndRetrieveEntityWithChildren() throws Exception {
        HttpResponse resp = Request.Post("http://localhost:8080/entity")
                .bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0 ;i<2;i++) {
            Entity child =createSimpleFixtureEntity();
            child.setParentId(id);
            resp = Request.Post("http://localhost:8080/entity")
                    .bodyString(mapper.writeValueAsString(child), ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }

        resp = Request.Get("http://localhost:8080/entity/" + id)
                .execute()
                .returnResponse();
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(2, fetched.getChildren().size());
    }
    @Test
    public void testCreateAndRetrieveEntityWithOneHundredChildren() throws Exception {
        Entity e = createFixtureCollectionEntity();
        long time = System.currentTimeMillis();
        HttpResponse resp = Request.Post("http://localhost:8080/entity")
                .bodyString(mapper.writeValueAsString(e), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0 ;i<100;i++) {
            Entity child =createSimpleFixtureEntity();
            child.setParentId(id);
            resp = Request.Post("http://localhost:8080/entity")
                    .bodyString(mapper.writeValueAsString(child), ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }
        log.debug("creating an entity with 100 children took {} ms", System.currentTimeMillis() - time);
        assertEquals(200, resp.getStatusLine().getStatusCode());

        time = System.currentTimeMillis();
        resp = Request.Get("http://localhost:8080/entity/" + id)
                .execute()
                .returnResponse();
        log.debug("fetching an entity with 100 children took {} ms", System.currentTimeMillis() - time);
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(100, fetched.getChildren().size());
        assertEquals("Collection", fetched.getType());
    }

}