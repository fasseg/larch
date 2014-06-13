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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.bench.BenchTool;
import net.objecthunter.larch.bench.BenchToolResult;
import net.objecthunter.larch.bench.BenchToolRunner;
import net.objecthunter.larch.bench.ResultFormatter;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LarchServerConfiguration.class)
@IntegrationTest
@WebAppConfiguration
@ActiveProfiles("weedfs")
public class PerformanceIT {
    private static final Logger log = LoggerFactory.getLogger(PerformanceIT.class);

    @Autowired
    private Environment env;

    @PostConstruct
    public void waitForWeedFs() throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        int count = 0;
        boolean weedfsReady = false;
        final ObjectMapper mapper = new ObjectMapper();
        final String weedUri = "http://" + env.getProperty("weedfs.master.public") + ":" + env.getProperty("weedfs.master.port");
        log.info("waiting for (datacenters != null) at " + weedUri + "/dir/status");
        do {
            HttpResponse resp = Request.Get(weedUri + "/dir/status")
                    .execute()
                    .returnResponse();
            JsonNode node = mapper.readTree(resp.getEntity().getContent());
            if (node.get("Topology").get("DataCenters").get(0) != null) {
                weedfsReady = true;
            } else {
                Thread.sleep(150);
            }
        } while (!weedfsReady && count++ < 500);
        if (!weedfsReady) {
            throw new Exception("WeedFS not ready after " + count * 150 + " ms");
        }
    }

    @Test
    public void ingestSingle100MBFile() throws Exception {
        BenchToolRunner bench = new BenchToolRunner(BenchTool.Action.INGEST,
                URI.create("http://localhost:8080"),
                "admin",
                "admin",
                1,
                1,
                1024 * 1024 * 100);
        List<BenchToolResult> results = bench.run();
        ResultFormatter.printResults(results, 1, 1024 * 1024 * 100, System.out, 100f);
    }
}
