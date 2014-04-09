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

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.source.StreamSource;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.service.impl.DefaultEntityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DefaultEntityServiceIT extends AbstractLarchIT {
    @Autowired
    private DefaultEntityService entityService;

    @Test
    public void testCreateEntity() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new StreamSource(this.getClass().getClassLoader().getResourceAsStream("fixtures/image_1.png")));
        bin1.setName("image-1");
        Binary bin2 = new Binary();
        bin2.setMimetype("image/png");
        bin2.setFilename("image_2.png");
        bin2.setSource(new UrlSource(this.getClass().getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin2.setName("image-2");
        Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin1.getName(), bin1);
        binaries.put(bin2.getName(), bin2);
        Entity e = new Entity();
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test","integration-test"));
        e.setType("Book");
        e.setBinaries(binaries);
        entityService.create(e);
    }
}
