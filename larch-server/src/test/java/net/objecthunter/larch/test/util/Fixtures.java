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

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Fixtures {
    public static User createUser() {
        User u = new User();
        u.setGroups(Arrays.asList(createGroup()));
        u.setName("test");
        u.setPwhash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"); //sha256 hash for pw 'test'
        return u;
    }

    public static Group createGroup() {
        Group g = new Group();
        g.setName("ROLE_TEST");
        return g;
    }

    public static Entity createEntity() {
        Entity e = new Entity();
        e.setId("testid");
        e.setLabel("Test label");
        e.setType("Test type");
        e.setTags(Arrays.asList("tag1", "tag2"));
        e.setMetadata(createMetadataMap());
        e.setBinaries(createBinaryMap());
        e.setRelations(createRelations());
        return e;
    }

    public static Map<String, List<String>> createRelations() {
        Map<String, List<String>> relations = new HashMap<>();
        relations.put("testpredicate", Arrays.asList("object1", "object2"));
        return relations;
    }

    public static Map<String, Binary> createBinaryMap() {
        Map<String, Binary> bins = new HashMap<>(1);
        Binary bin = createBinary();
        bins.put(bin.getName(), bin);
        return bins;
    }

    public static Binary createBinary() {
        Binary bin = new Binary();
        bin.setSize(1);
        bin.setMimetype("text/plain");
        bin.setName("BINARY-1");
        bin.setPath("/path/to/testbinary");
        return bin;
    }

    public static Map<String, Metadata> createMetadataMap() {
        Map<String, Metadata> metadataMap = new HashMap<>(1);
        Metadata md = creatMetadata();
        metadataMap.put(md.getName(), md);
        return metadataMap;
    }

    public static Metadata creatMetadata() {
        Metadata data = new Metadata();
        data.setName("DC");
        data.setType("Dublin Core");
        data.setData("<empty/>");
        return data;
    }

    public static MetadataType createMetadataType() {
        MetadataType type = new MetadataType();
        type.setName("Dublin Core");
        type.setSchemaUrl("http://example.com");
        return type;
    }
}
