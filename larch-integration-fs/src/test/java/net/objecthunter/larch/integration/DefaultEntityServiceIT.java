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

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.impl.DefaultEntityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static net.objecthunter.larch.integration.helpers.Fixtures.*;

public class DefaultEntityServiceIT extends AbstractLarchIT {
    @Autowired
    private DefaultEntityService entityService;

    @Test
    public void testCreateAndGetEntityAndContent() throws Exception {
        Entity e = createFixtureEntity();
        entityService.create(e);
        Entity fetched = entityService.retrieve(e.getId());
        assertEquals(e.getId(), fetched.getId());
        assertEquals(e.getLabel(), fetched.getLabel());
        assertEquals(e.getBinaries().size(), fetched.getBinaries().size());
        assertEquals(1, fetched.getVersion());
        fetched.getBinaries().values().forEach(b -> {
            assertNotNull(b.getChecksum());
            assertNotNull(b.getChecksumType());
            assertNotNull(b.getFilename());
            assertNotNull(b.getMimetype());
            assertNotNull(b.getPath());
            try (final InputStream src = entityService.getContent(e.getId(), b.getName())) {
                assertTrue(src.available() > 0);
            } catch (IOException e1) {
                fail("IOException: " + e1.getLocalizedMessage());
            }
        });
    }

    @Test
    public void testCreateAndGetEntityWithChildren() throws Exception {
        Entity e = createFixtureEntityWithChildren();
        entityService.create(e);
        Entity fetched = entityService.retrieve(e.getId());
        assertEquals(2, fetched.getChildren().size());
    }

    @Test
    public void testCreateAndUpdate() throws Exception {
        Entity e = createFixtureEntity();
        String id = entityService.create(e);
        Entity orig = entityService.retrieve(id);
        Entity update = createFixtureEntity();
        update.setId(id);
        update.setLabel("My updated label");
        entityService.update(update);
        Entity fetched = entityService.retrieve(e.getId());
        assertEquals(update.getLabel(), fetched.getLabel());
        assertEquals(orig.getUtcCreated(), fetched.getUtcCreated());
    }

    @Test
    public void testCreateUpdateAndFetchOldVersion() throws Exception {
        Entity e = createFixtureEntity();
        String id = entityService.create(e);
        Entity orig = entityService.retrieve(id);
        Entity update = createFixtureEntity();
        update.setId(id);
        update.setLabel("My updated label");
        entityService.update(update);
        Entity fetched = entityService.retrieve(e.getId(),1);
        assertEquals(orig.getLabel(), fetched.getLabel());
        assertEquals(1, orig.getVersion());
        assertNull(orig.getVersionPaths());
        assertEquals(1, fetched.getVersion());
        assertNull(fetched.getVersionPaths());
    }
}
