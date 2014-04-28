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
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.BlobstoreService;
import net.objecthunter.larch.service.ExportService;
import net.objecthunter.larch.service.IndexService;
import net.objecthunter.larch.test.util.Fixtures;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.easymock.EasyMock.*;

public class DefaultEntityServiceTest {
    private DefaultEntityService entityService;
    private IndexService mockIndexService;
    private BlobstoreService mockBlobstoreService;
    private ExportService mockExportService;

    @Before
    public void setup() {
        entityService = new DefaultEntityService();
        mockIndexService = createMock(IndexService.class);
        mockBlobstoreService = createMock(BlobstoreService.class);
        mockExportService = createMock(ExportService.class);
        ReflectionTestUtils.setField(entityService, "mapper", new ObjectMapper());
        ReflectionTestUtils.setField(entityService, "indexService", mockIndexService);
        ReflectionTestUtils.setField(entityService, "exportService", mockExportService);
        ReflectionTestUtils.setField(entityService, "blobstoreService", mockBlobstoreService);
    }

    @Test
    public void testCreate() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockIndexService.exists(e.getId())).andReturn(false);
        expect(mockIndexService.create(e)).andReturn(e.getId());

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.create(e);
        verify(mockIndexService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testUpdate() throws Exception {

    }

    @Test
    public void testRetrieve() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testGetContent() throws Exception {

    }

    @Test
    public void testRetrieve1() throws Exception {

    }

    @Test
    public void testCreateBinary() throws Exception {

    }

    @Test
    public void testPatch() throws Exception {

    }

    @Test
    public void testCreateRelation() throws Exception {

    }
}
