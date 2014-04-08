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

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WeedFsMaster {
    private static final Logger log = LoggerFactory.getLogger(WeedFsMaster.class);

    @Autowired
    Environment env;
    private Process masterProcess;
    private InputStreamLoggerTask loggerTask;

    @Async
    public void runMaster() {
        /* check if the master dir exists and create if neccessary */
        final File dir = new File(env.getProperty("weedfs.master.dir"));
        if (!dir.exists()) {
            log.info("creating WeedFS master directory at " + dir.getAbsolutePath());
            if (!dir.mkdir()) {
                throw new IllegalArgumentException("Unable to create master directory. Please check the configuration");
            }
        }
        if (!dir.canRead() || !dir.canWrite()) {
            log.error("Unable to create master directory. The application was not initialiazed correctly");
            throw new IllegalArgumentException("Unable to use master directory. Please check the configuration");
        }
        try {
            log.info("starting WeedFS master");

            final List<String> command = Arrays.asList(
                    env.getProperty("weedfs.binary"),
                    "master",
                    "-mdir=" + env.getProperty("weedfs.master.dir"),
                    "-port=" + env.getProperty("weedfs.master.port")
            );
            masterProcess = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .start();

            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            if (!masterProcess.isAlive()) {
                throw new IOException("WeedFS Master could not be started! Exitcode " + masterProcess.exitValue());
            } else {
                log.info("WeedFs master is running");
                executorService.submit(new InputStreamLoggerTask(masterProcess.getInputStream())).get();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean isAvailable() {
        final File binary = new File(env.getProperty("weedfs.binary"));
        return binary.exists() && binary.canExecute();
    }

    public boolean isAlive() {
        return (masterProcess != null) && masterProcess.isAlive();
    }

    public void shutdown() {
        log.info("shutting down WeedFS master");
        if (this.masterProcess != null && this.masterProcess.isAlive()) {
            this.masterProcess.destroy();
        }
    }
 }
