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
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.BlobstoreService;
import net.objecthunter.larch.service.ExportService;
import net.objecthunter.larch.service.IndexService;
import net.objecthunter.larch.test.util.Fixtures;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
        Entity e = Fixtures.createEntity();

        mockIndexService.update(e);
        expectLastCall();
        expect(mockIndexService.retrieve(e.getId())).andReturn(e);
        expect(mockBlobstoreService.createOldVersionBlob(e)).andReturn("oldversionpath");

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.update(e);
        verify(mockIndexService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testRetrieve() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockIndexService.retrieve(e.getId())).andReturn(e);

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.retrieve(e.getId());
        verify(mockIndexService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testDelete() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockIndexService.retrieve(e.getId())).andReturn(e);
        mockIndexService.delete(e.getId());
        expectLastCall();
        mockBlobstoreService.delete(e.getBinaries().entrySet().iterator().next().getValue().getPath());
        expectLastCall();

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.delete(e.getId());
        verify(mockIndexService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testGetContent() throws Exception {
        Entity e = Fixtures.createEntity();
        Binary b = Fixtures.createBinary();

        expect(mockIndexService.retrieve(e.getId())).andReturn(e);
        expect(mockBlobstoreService.retrieve(b.getPath())).andReturn(new ByteArrayInputStream(new byte[3]));

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.getContent(e.getId(),"BINARY-1");
        verify(mockIndexService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testRetrieve1() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockIndexService.retrieve(e.getId())).andReturn(e);

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.retrieve(e.getId());
        verify(mockIndexService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testCreateBinary() throws Exception {
        Entity e = Fixtures.createEntity();
        Binary b = Fixtures.createBinary();
        b.setName("BINARY_CREATE");

        expect(mockBlobstoreService.createOldVersionBlob(e)).andReturn("oldpath");
        expect(mockIndexService.retrieve(e.getId())).andReturn(e);
        expect(mockBlobstoreService.create(anyObject(InputStream.class))).andReturn("/path/to/bin");
        mockIndexService.update(e);
        expectLastCall();

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.createBinary(e.getId(), b.getName(), "application/octet-stream",
                new ByteArrayInputStream(new byte[3]));
        verify(mockIndexService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testPatch() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockIndexService.retrieve(e.getId())).andReturn(e).times(2);
        expect(mockBlobstoreService.createOldVersionBlob(e)).andReturn("oldpath");
        mockIndexService.update(e);
        expectLastCall();

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.patch(e.getId(), new ObjectMapper().readTree("{\"label\": \"label update\"}"));
        verify(mockIndexService, mockExportService, mockBlobstoreService);

    }

    @Test
    public void testCreateRelation() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockIndexService.retrieve(e.getId())).andReturn(e);
        expect(mockBlobstoreService.createOldVersionBlob(e)).andReturn("oldpath");
        mockIndexService.update(e);
        expectLastCall();

        replay(mockIndexService, mockExportService, mockBlobstoreService);
        this.entityService.createRelation(e.getId(), "<http://example.com/hasType>", "test");
        verify(mockIndexService, mockExportService, mockBlobstoreService);

    }
}
