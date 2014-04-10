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
package net.objecthunter.larch.integration.helpers;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.source.UrlSource;

import java.util.*;

public abstract class Fixtures {
    public static Entity createFixtureEntity() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin1.setName("image-1");
        Binary bin2 = new Binary();
        bin2.setMimetype("image/png");
        bin2.setFilename("image_2.png");
        bin2.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin2.setName("image-2");
        Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin1.getName(), bin1);
        binaries.put(bin2.getName(), bin2);
        Entity e = new Entity();
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test", "integration-test"));
        e.setType("Book");
        e.setBinaries(binaries);
        return e;
    }

    public static Entity createSimpleFixtureEntity() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin1.setName("image-1");
        Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin1.getName(), bin1);
        Entity e = new Entity();
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test", "integration-test"));
        e.setType("Image");
        e.setBinaries(binaries);
        return e;
    }

    public static Entity createFixtureEntityWithChildren() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin1.setName("image-1");
        Binary bin2 = new Binary();
        bin2.setMimetype("image/png");
        bin2.setFilename("image_2.png");
        bin2.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin2.setName("image-2");
        Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin1.getName(), bin1);
        binaries.put(bin2.getName(), bin2);
        Entity e = new Entity();
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test", "integration-test"));
        e.setType("Book");
        e.setBinaries(binaries);
        e.setChildren(Arrays.asList(createFixtureEntity(), createFixtureEntity()));
        return e;
    }

    public static Entity createFixtureEntityWith100Children() throws Exception {
        Entity e = createSimpleFixtureEntity();
        List<Entity> children = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            children.add(createSimpleFixtureEntity());
        }
        e.setType("Collection");
        e.setChildren(children);
        return e;
    }
}
