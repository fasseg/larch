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
import net.objecthunter.larch.weedfs.WeedFSBlobstoreService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.PostConstruct;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LarchServerConfiguration.class)
@IntegrationTest
@WebAppConfiguration
@ActiveProfiles("weedfs")
public abstract class AbstractWeedFsLarchIT {
    private static final Logger log = LoggerFactory.getLogger(AbstractWeedFsLarchIT.class);
    @PostConstruct
    public void waitForWeedFs() throws Exception {
        int count = 0;
        boolean weedfsReady = false;
        final ObjectMapper mapper = new ObjectMapper();
        do {
            HttpResponse resp = Request.Get("http://localhost:9333/dir/status")
                    .execute()
                    .returnResponse();
            JsonNode node = mapper.readTree(resp.getEntity().getContent());
            if (node.get("Topology").get("DataCenters").get(0) != null) {
                weedfsReady = true;
            }else{
                Thread.sleep(150);
            }
        }while (!weedfsReady && count++ < 150);
        if (!weedfsReady) {
            throw new Exception("WeedFS not ready after " + count * 150 + " ms");
        }
    }
}
