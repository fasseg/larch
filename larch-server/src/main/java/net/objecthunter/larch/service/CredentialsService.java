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

import com.fasterxml.jackson.core.JsonParseException;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;

import java.io.IOException;
import java.util.List;

public interface CredentialsService {
    void createUser(User u) throws IOException;

    void createGroup(Group g) throws IOException;

    void updateUser(User u) throws IOException;

    void updateGroup(Group g) throws IOException;

    void addUserToGroup(String username, String groupname) throws IOException;

    void deleteUser(String name) throws IOException;

    void deleteGroup(String name) throws IOException;

    void removeUserFromGroup(String username, String groupname) throws IOException;

    User retrieveUser(String name) throws IOException;

    Group retrieveGroup(String name) throws IOException;

    List<User> retrieveUsers() throws IOException;

    List<Group> retrieveGroups() throws IOException;
}
