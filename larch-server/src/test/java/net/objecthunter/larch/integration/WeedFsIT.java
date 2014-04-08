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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.net.objecthunter.weedfs.WeedFSVolume;
import net.objecthunter.larch.net.objecthunter.weedfs.WeedFsMaster;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class WeedFsIT extends AbstractLarchIT{

    @Autowired
    private WeedFsMaster master;

    @Autowired
    private WeedFSVolume volume;

    @After
    public void cleanup() {
        master.shutdown();
        volume.shutdown();
    }

    @Test
    public void testStartStop() throws Exception {
        assumeTrue(master.isAvailable());
        assumeTrue(volume.isAvailable());
        master.runMaster();
        // wait at most 500ms until the master is up then throw an exception
        long time = System.currentTimeMillis();
        while (!master.isAlive()) {
            if (System.currentTimeMillis() > time + 3000) {
                fail("WeedFS master not alive after 1500ms");
            }
        }
        assertTrue(master.isAlive());

        //Start the volume node
        volume.runVolume();
        time = System.currentTimeMillis();
        while (!volume.isAlive()) {
            if (System.currentTimeMillis() > time + 3000) {
                fail("WeedFS volume not alive after 1500ms");
            }
        }
        assertTrue(volume.isAlive());

        time = System.currentTimeMillis();

        // retrieve a fid from WeedFs
        int count = 0;
        boolean fidFetched = false;
        while (count++ < 50 && !fidFetched) {
            final HttpResponse resp = Request.Get("http://localhost:9333/dir/assign")
                    .execute()
                    .returnResponse();
            final JsonNode node = new ObjectMapper().readTree(resp.getEntity().getContent());
            fidFetched = node.get("fid") != null;
            if (!fidFetched) {
                Thread.sleep(100);
            }
        }
        assertTrue(fidFetched);

        // stop the volume node
        volume.shutdown();
        // wait at most 500ms until the master is down then throw an exception
        time = System.currentTimeMillis();
        while (volume.isAlive()) {
            if (System.currentTimeMillis() > time + 500) {
                fail("WeedFS master alive 500ms after shutdown");
            }
        }
        assertFalse(volume.isAlive());

        master.shutdown();
        // wait at most 500ms until the master is down then throw an exception
        time = System.currentTimeMillis();
        while (master.isAlive()) {
            if (System.currentTimeMillis() > time + 500) {
                fail("WeedFS master alive 500ms after shutdown");
            }
        }
        assertFalse(master.isAlive());
    }

}
