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
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.source.UrlSource;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

public abstract class Fixtures {
    public static Entity createFixtureEntityWithRandomId() throws Exception {
        Entity e = createFixtureEntity();
        e.setId(RandomStringUtils.randomAlphabetic(16));
        return e;
    }

    public static Entity createFixtureEntity() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin1.setName("image-1");
        Map<String, Metadata> bin1Md = new HashMap<>();
        Metadata md = createRandomDCMetadata();
        bin1Md.put(md.getName(), md);
        bin1.setMetadata(bin1Md);
        Binary bin2 = new Binary();
        bin2.setMimetype("image/png");
        bin2.setFilename("image_2.png");
        bin2.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin2.setName("image-2");
        Map<String, Metadata> bin2Md = new HashMap<>();
        md = createRandomDCMetadata();
        bin2Md.put(md.getName(),md);
        bin2.setMetadata(bin2Md);
        Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin1.getName(), bin1);
        binaries.put(bin2.getName(), bin2);
        Map<String, Metadata> metadata = new HashMap<>();
        md = createRandomDCMetadata();
        metadata.put(md.getName(), md);
        Entity e = new Entity();
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test", "integration-test"));
        e.setType("Book");
        e.setBinaries(binaries);
        e.setMetadata(metadata);
        return e;
    }

    public static Metadata createRandomDCMetadata() {
        Metadata md = new Metadata();
        md.setType("DC");
        md.setName("Dublin-Core-" + RandomStringUtils.randomAlphabetic(16));
        StringBuilder dcBuilder = new StringBuilder();
        dcBuilder.append("<metadata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:dc=\"http://purl" +
                ".org/dc/elements/1.1/\">")
                .append("\n\t<dc:title>Test Object</dc:title>")
                .append("\n\t<dc:creator>fasseg</dc:creator>")
                .append("\n\t<dc:subject>Testing Groven</dc:subject>")
                .append("\n\t<dc:description>Test Object to implement integration Tests</dc:description>")
                .append("\n<metadata>");
        md.setData(dcBuilder.toString());
        return md;
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

    public static Entity createFixtureCollectionEntity() throws Exception {
        Entity e = createSimpleFixtureEntity();
        e.setType("Collection");
        return e;
    }

    public static Binary createRandomImageBinary() throws Exception{
        Binary bin = new Binary();
        bin.setMimetype("image/png");
        bin.setFilename("image_1.png");
        bin.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin.setName(RandomStringUtils.randomAlphabetic(16));
        return bin;
    }
}
