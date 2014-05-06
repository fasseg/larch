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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.Entity;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;

public class ActionWorker implements Callable<BenchToolResult> {

    private static final Logger log = LoggerFactory.getLogger(ActionWorker.class);

    private final BenchTool.Action action;
    private final long size;
    private final CloseableHttpClient httpClient;
    private final String larchUri;
    private final ObjectMapper mapper = new ObjectMapper();

    protected ActionWorker(BenchTool.Action action, long size, CloseableHttpClient httpClient, String larchUri) {
        this.action = action;
        this.size = size;
        this.httpClient = httpClient;
        this.larchUri = larchUri;
    }

    @Override
    public BenchToolResult call() throws Exception {
        switch (this.action) {
            case INGEST:
                return doIngest();
            default:
                throw new IllegalArgumentException("Unknown action '" + this.action + "'");
        }
    }

    private BenchToolResult doIngest() throws IOException {
        long time = System.currentTimeMillis();
        final Entity e = BenchToolEntities.createRandomEmptyEntity();

        /* create a new entity */
        final HttpPost postEntity = new HttpPost(larchUri + "/entity");
        postEntity.setEntity(new StringEntity(mapper.writeValueAsString(e), ContentType.APPLICATION_JSON));
        final HttpResponse postEntityResp = this.httpClient.execute(postEntity);
        if (postEntityResp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to create entity:\n{}", EntityUtils.toString(postEntityResp.getEntity()));
            throw new IOException("Unable to create entity for ingest");
        }
        postEntity.releaseConnection();
        final String entityId = EntityUtils.toString(postEntityResp.getEntity());

        /* add a binary */
        final HttpPost postBinary = new HttpPost(larchUri + "/entity/" + entityId + "/binary?name=" +
                RandomStringUtils.randomAlphabetic(16) + "&mimetype=application/octet-stream");
        postBinary.setEntity(new InputStreamEntity(new RandomInputStream(size), size));
        final HttpResponse postBinaryResp = this.httpClient.execute(postBinary);
        if (postBinaryResp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to create binary\n{}",EntityUtils.toString(postBinaryResp.getEntity()));
            throw new IOException("unable to create binary");
        }
        postBinary.releaseConnection();

        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }


}
