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
package net.objecthunter.larch.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class InputStreamLoggerTask implements Callable<Void> {
    private static final Logger log = LoggerFactory.getLogger(InputStreamLoggerTask.class);

    private final BufferedReader reader;

    public InputStreamLoggerTask(InputStream src) {
        this.reader = new BufferedReader(new InputStreamReader(src));
    }

    @Override
    public Void call() throws Exception {
        String line = null;
        while (true) {
            if (this.reader.ready() && (line = this.reader.readLine()) != null) {
                log.info(line);
            } else {
                Thread.yield();
            }
        }
    }

}
