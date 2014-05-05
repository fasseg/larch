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

import net.objecthunter.larch.client.LarchClient;
import net.objecthunter.larch.integration.helpers.Fixtures;
import net.objecthunter.larch.model.Entity;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LarchClientIT extends AbstractLarchIT {
    private LarchClient client = new LarchClient();

    @Test
    public void testPostEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntity();
        e.setId(RandomStringUtils.randomAlphabetic(16));
        client.postEntity(e);
    }

    @Test
    public void testRetrieveEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntity();
        e.setId(RandomStringUtils.randomAlphabetic(16));
        client.postEntity(e);
        Entity fetched = client.retrieveEntity(e.getId());
        assertEquals(e.getId(), fetched.getId());
        assertEquals(e.getLabel(), fetched.getLabel());
        assertEquals(e.getBinaries().size(), fetched.getBinaries().size());
    }
}
