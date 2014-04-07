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

import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.net.objecthunter.weedfs.WeedFSVolume;
import net.objecthunter.larch.net.objecthunter.weedfs.WeedFsMaster;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LarchServerConfiguration.class)
@IntegrationTest
@WebAppConfiguration
public class WeedFSTest {

    @Autowired
    private WeedFsMaster master;

    @Autowired
    private WeedFSVolume volume;

    @Test
    public void testStartStopMasterAndVolume() throws Exception {
        master.runMaster();
        // wait at most 500ms until the master is up then throw an exception
        long time = System.currentTimeMillis();
        while (!master.isAlive()) {
            if (System.currentTimeMillis() > time + 500) {
                fail("WeedFS master not alive after 500ms");
            }
        }
        assertTrue(master.isAlive());

        //Start the volume node
        volume.runVolume();
        time = System.currentTimeMillis();
        while (!volume.isAlive()) {
            if (System.currentTimeMillis() > time + 500) {
                fail("WeedFS volume not alive after 500ms");
            }
        }
        assertTrue(volume.isAlive());

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
