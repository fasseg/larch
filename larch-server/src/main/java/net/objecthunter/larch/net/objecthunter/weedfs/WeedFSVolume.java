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

import net.objecthunter.larch.helpers.InputStreamLoggerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WeedFSVolume {
    private static final Logger log = LoggerFactory.getLogger(WeedFSVolume.class);

    @Autowired
    Environment env;
    private Process volumeProcess;
    private InputStreamLoggerTask loggerTask;

    @Async
    public void runVolume() {
        /* check if the master dir exists and create if neccessary */
        final File dir = new File(env.getProperty("weedfs.volume.dir"));
        if (!dir.exists()) {
            log.info("creating WeedFS volume directory at " + dir.getAbsolutePath());
            if (!dir.mkdir()) {
                throw new IllegalArgumentException("Unable to create volume directory. Please check the configuration");
            }
        }
        if (!dir.canRead() || !dir.canWrite()) {
            log.error("Unable to create volume directory. The application was not initialiazed correctly");
            throw new IllegalArgumentException("Unable to use volume directory. Please check the configuration");
        }
        try {
            log.info("starting WeedFS volume");

            /* start weedfs volume server */
            String[] args = new String[]{
                    env.getProperty("weedfs.binary"),
                    "volume",
                    "-ip=localhost",
                    "-publicUrl=localhost:8081",
                    "-dir=" + env.getProperty("weedfs.volume.dir"),
                    "-mserver=" + env.getProperty("weedfs.master.host") + ":" + env.getProperty("weedfs.master.port"),
                    "-port=" + env.getProperty("weedfs.volume.port")
            };
            volumeProcess = new ProcessBuilder(args)
                    .redirectErrorStream(true)
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .start();

            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            if (!volumeProcess.isAlive()) {
                throw new IOException("WeedFS volume could not be started! Exitcode " + volumeProcess.exitValue());
            } else {
                log.info("WeedFs volume is running");
                executorService.submit(new InputStreamLoggerTask(volumeProcess.getInputStream())).get();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean isAlive() {
        return (volumeProcess != null) && volumeProcess.isAlive();
    }

    public void shutdown() {
        log.info("shutting down WeedFS volume");
        this.volumeProcess.destroy();
    }
}
