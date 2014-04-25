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
package net.objecthunter.larch.test.util;

import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;

import java.util.Arrays;

public abstract class Fixtures {
    public static User getUser() {
        User u = new User();
        Group g = new Group();
        g.setName("ROLE_TEST");
        u.setGroups(Arrays.asList(g));
        u.setName("test");
        u.setPwhash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"); //sha256 hash for pw 'test'
        return u;
    }
}
