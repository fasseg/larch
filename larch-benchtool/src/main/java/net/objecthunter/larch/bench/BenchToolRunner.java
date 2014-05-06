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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BenchToolRunner {

    private static final Logger log = LoggerFactory.getLogger(BenchToolRunner.class);

    private final long size;
    private final int numActions;
    private final BenchTool.Action action;
    private final String larchUri;
    private final int numThreads;
    private final String username;
    private final String password;
    private final ExecutorService executor;

    public BenchToolRunner(BenchTool.Action action, String larchUri, int numActions, int numThreads, long size,
                           String username, String password) {
        this.size = size;
        this.numActions = numActions;
        this.action = action;
        this.larchUri = larchUri;
        this.numThreads = numThreads;
        this.username = username;
        this.password = password;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    public List<BenchToolResult> run() throws IOException {
        final List<Future<BenchToolResult>> futures = new ArrayList<>();
        for (int i = 0; i < numActions; i++) {
            futures.add(executor.submit(new ActionWorker(this.action, larchUri, this.size, this.username,
                    this.password)));
        }

        try {
            final List<BenchToolResult> results = new ArrayList<>();
            int count = 0;
            for (Future<BenchToolResult> f : futures) {
                results.add(f.get());
                log.info("Finished {} of {} {} actions", new Object[] {++count, numActions, action.name()});
            }
            return results;
        }catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }finally {
            this.executor.shutdown();
        }
    }
}
