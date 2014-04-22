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
package net.objecthunter.larch.service;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.BlobstoreState;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service definition for CRUD operations on Blobs
 */
public interface BlobstoreService {
    String create(InputStream src) throws IOException;
    InputStream retrieve(String path) throws IOException;
    void delete(String path) throws IOException;
    void update(String path, InputStream src) throws IOException;
    BlobstoreState status() throws IOException;
    String createOldVersionBlob(Entity oldVersion) throws IOException;
    InputStream retrieveOldVersionBlob(String path) throws IOException;
}
