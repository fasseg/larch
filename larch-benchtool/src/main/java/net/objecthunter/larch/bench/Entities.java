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
package net.objecthunter.larch.bench;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.source.StreamSource;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class Entities {
    public static Entity createRandomEntityWithBinary(long binarySize) {
        final Entity e = new Entity();
        e.setLabel("benchtool-" + RandomStringUtils.randomAlphabetic(16));
        e.setType("book");
        final Binary bin = createRandomBinary(binarySize);
        final Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin.getName(), bin);
        e.setBinaries(binaries);
        return e;
    }

    private static Binary createRandomBinary(long binarySize) {
        final Binary bin = new Binary();
        bin.setName(RandomStringUtils.randomAlphabetic(16));
        bin.setSource(new StreamSource(new RandomInputStream(binarySize)));
        bin.setMimetype("application/json");
        bin.setSize(binarySize);
        return bin;
    }
}
