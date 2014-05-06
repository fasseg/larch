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

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;

public class ResultFormatter {
    private static final Logger log = LoggerFactory.getLogger(ResultFormatter.class);
    private static final DecimalFormat format = new DecimalFormat("###.##");

    public static void printResults(List<BenchToolResult> results, int num, long size, long overallDuration,
                                    OutputStream sink) {
        long duration = 0;
        float throughput = 0f;
        for (BenchToolResult result : results) {
            duration += result.getDuration();
            throughput = result.getThroughput();
        }
        throughput /= (float) results.size();
        float overallTroughput = ((float) num * (float) size * 1000f / ((float) overallDuration * 1024f *1024f));
        log.info("-----------------------------------------------------------------------");
        log.info("RESULTS");
        log.info("-----------------------------------------------------------------------");
        log.info("Number of results\t\t{}", results.size());
        log.info("Individual size\t\t{} mb", format.format((float) size / (1024f*1024f)));
        log.info("Overall Size\t\t{} mb", format.format((float) (size * num) / (1024f * 1024f)));
        log.info("Duration of threads\t\t{} secs", format.format((float) duration / 1000f));
        log.info("Overall duration\t\t{} secs", format.format((float) overallDuration / 100f));
        log.info("Avg. throughput per thread\t{} mb/sec", format.format(throughput));
        log.info("Overall throughput:\t\t{} mb/sec", format.format(overallTroughput));
        log.info("-----------------------------------------------------------------------");
    }
}
