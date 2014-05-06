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

import net.objecthunter.larch.client.LarchClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;

public class ActionWorker implements Callable<BenchToolResult> {

    private static final Logger log = LoggerFactory.getLogger(ActionWorker.class);

    private final BenchTool.Action action;
    private final long size;

    private final LarchClient client;

    protected ActionWorker(BenchTool.Action action, String larchUri, long size, String user, String pass) {
        this.action = action;
        this.size = size;
        this.client = new LarchClient(URI.create(larchUri), user, pass);
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
        String id = client.postEntity(Entities.createRandomEmptyEntity(size));
        client.postBinary(id, RandomStringUtils.randomAlphabetic(16), "application/octet-stream",
                new RandomInputStream(size));
        long duration = System.currentTimeMillis() - time;
        return new BenchToolResult(duration);
    }


}
