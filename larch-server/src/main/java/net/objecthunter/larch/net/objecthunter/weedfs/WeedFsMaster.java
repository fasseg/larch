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
package net.objecthunter.larch.net.objecthunter.weedfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class WeedFsMaster{
    private static final Logger log = LoggerFactory.getLogger(WeedFsMaster.class);

    @Autowired
    Environment env;

    @Async
    public void runMaster() {
        try {
            log.info("starting WeedFS master");

            final List<String> command = new ArrayList<>();
            command.add(env.getProperty("weedfs.binary"));
            command.addAll(Arrays.asList(env.getProperty("weedfs.master.args").split(" ")));
            final Process master =  new ProcessBuilder(command)
                    .inheritIO()
                    .start();
            if (!master.isAlive()) {
                throw new IOException("WeedFS Master could not be started! Exitcode " + master.exitValue());
            }else{
                log.info("WeedFs master is running");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
