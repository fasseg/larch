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
package net.objecthunter.larch.weedfs;

import net.objecthunter.larch.service.BlobstoreService;

import java.io.IOException;
import java.io.InputStream;

public class WeedFSBlobstoreService implements BlobstoreService {
    @Override
    public String create(InputStream src) throws IOException {
        return null;
    }

    @Override
    public InputStream retrieve(String path) throws IOException {
        return null;
    }

    @Override
    public void delete(String path) throws IOException {

    }

    @Override
    public void update(String path, InputStream src) throws IOException {

    }
}
