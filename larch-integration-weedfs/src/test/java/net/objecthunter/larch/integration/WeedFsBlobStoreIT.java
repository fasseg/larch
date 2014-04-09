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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.model.state.WeedFsBlobstoreState;
import net.objecthunter.larch.weedfs.WeedFSBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WeedFsBlobStoreIT extends AbstractWeedFsLarchIT{
    @Autowired
    LarchServerConfiguration config;

    private WeedFSBlobstoreService blobstoreService;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void waitForWeedFs() throws Exception {
        blobstoreService = (WeedFSBlobstoreService) config.weedFSBlobstoreService();
        int count = 0;
        boolean weedfsReady = false;
        do {
            HttpResponse resp = Request.Get("http://localhost:9333/dir/status")
                    .execute()
                    .returnResponse();
            JsonNode node = mapper.readTree(resp.getEntity().getContent());
            if (node.get("Topology").get("DataCenters").get(0) != null) {
                weedfsReady = true;
            }else{
                Thread.sleep(50);
            }
        }while (!weedfsReady && count++ < 150);

    }

    @Test
    public void testCreateAndRetrieve() throws Exception {
        String data = "mysimpledatawithÄ";
        String path = blobstoreService.create(new ByteArrayInputStream(data.getBytes()));
        try (InputStream src = blobstoreService.retrieve(path)) {
            assertEquals(data, IOUtils.toString(src));
        }
    }

    @Test
    public void testUpdateAndRetrieve() throws Exception {
        String data = "mysimpledatawithö";
        String path = blobstoreService.create(new ByteArrayInputStream(data.getBytes()));
        String update = "mysimpledatawithßandé";
        blobstoreService.update(path, new ByteArrayInputStream(update.getBytes()));
        try (InputStream src = blobstoreService.retrieve(path)) {
            assertEquals(update, IOUtils.toString(src));
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testCreateAndDelete() throws Exception {
        String data = "mysimpledatawithÄ";
        String path = blobstoreService.create(new ByteArrayInputStream(data.getBytes()));
        blobstoreService.delete(path);
        blobstoreService.retrieve(path);
    }

    @Test
    public void testGetStatus() throws Exception {
        blobstoreService.create(new ByteArrayInputStream("foo".getBytes()));
        blobstoreService.create(new ByteArrayInputStream("bar".getBytes()));
        blobstoreService.create(new ByteArrayInputStream("baz".getBytes()));
        WeedFsBlobstoreState state = blobstoreService.status();
        assertNotNull(state);
        assertNotNull(state.getVersion());
    }
}
