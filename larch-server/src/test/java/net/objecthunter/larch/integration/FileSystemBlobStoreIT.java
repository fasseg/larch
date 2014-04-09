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

import net.objecthunter.larch.fs.FilesystemBlobstoreService;
import net.objecthunter.larch.model.FilesystemBlobstoreState;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileSystemBlobStoreIT extends AbstractLarchIT{
    @Autowired
    private FilesystemBlobstoreService blobstoreService;

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
    public void testgetStatus() throws Exception {
        FilesystemBlobstoreState state = blobstoreService.status();
    }
}
