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

package net.objecthunter.larch.service.backend;

import net.objecthunter.larch.model.Workspace;

import java.io.IOException;

public interface BackendWorkspaceService {

    String createWorkspace(Workspace workspace) throws IOException;

    Workspace retrieveWorkspace(String id) throws IOException;

    void updateWorkspace(Workspace workspace) throws IOException;

    void patchWorkSpace(Workspace workspace) throws IOException;

}
