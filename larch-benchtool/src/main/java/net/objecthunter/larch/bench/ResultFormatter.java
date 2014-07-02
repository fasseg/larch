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

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultFormatter {

    private static final Logger log = LoggerFactory.getLogger(ResultFormatter.class);

    private static final DecimalFormat format = new DecimalFormat("###.##");

    public static void printResults(List<BenchToolResult> results, int num, long size, OutputStream sink) {
        ResultFormatter.printResults(results, num, size, sink, 0f);
    }

    public static void printResults(List<BenchToolResult> results, int num, long size, OutputStream sink,
            float minTroughput) {
        long duration = 0;
        float throughput = 0f;
        for (BenchToolResult result : results) {
            duration += result.getDuration();
            throughput += result.getThroughput();
        }
        log.info("-----------------------------------------------------------------------");
        log.info("RESULTS");
        log.info("-----------------------------------------------------------------------");
        log.info("Number of results\t\t\t{}", results.size());
        log.info("Individual size\t\t\t{} mb", format.format((float) size / (1024f * 1024f)));
        log.info("Overall Size\t\t{} mb", format.format((float) (size * num) / (1024f * 1024f)));
        log.info("Aggregate duration of threads\t\t{} secs", format.format((float) duration / 1000f));
        log.info("Avg. throughput per thread\t\t{} mb/sec", format.format(throughput));
        log.info("-----------------------------------------------------------------------");
        if (minTroughput > 0f) {
            if (throughput < minTroughput) {
                log.warn("-----------------------------------------------------------------------");
                log.warn("WARNING!! The larch throughput dropped below the threshold of {} mb/sec",
                        format.format(minTroughput));
                log.warn("This is real bad please check that your changes did not impact performance significantly");
                log.warn("-----------------------------------------------------------------------");
            }
        }
    }
}
