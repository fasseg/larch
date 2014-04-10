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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.ZonedDateTime;

import static net.objecthunter.larch.integration.helpers.Fixtures.createFixtureEntity;
import static org.junit.Assert.*;

public class EntityControllerIT extends AbstractLarchIT {

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
        System.out.println(EntityUtils.toString(resp.getEntity()));
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
    }
}
