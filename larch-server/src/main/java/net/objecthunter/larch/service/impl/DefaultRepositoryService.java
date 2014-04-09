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

import net.objecthunter.larch.model.LarchState;
import net.objecthunter.larch.service.BlobstoreService;
import net.objecthunter.larch.service.IndexService;
import net.objecthunter.larch.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class DefaultRepositoryService implements RepositoryService {
    @Autowired
    private IndexService indexService;

    @Autowired
    private BlobstoreService blobstoreService;

    @Override
    public LarchState status() throws IOException {
        final LarchState state = new LarchState();
        state.setBlobstoreState(blobstoreService.status());
        state.setIndexState(indexService.status());
        return state;
    }
}
