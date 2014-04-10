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
package net.objecthunter.larch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public abstract class FileSystemUtil {
    private static final Logger log = LoggerFactory.getLogger(FileSystemUtil.class);
    public static void checkAndCreate(File dir) throws IOException {
        if (!dir.exists()) {
            log.info("Creating non existing data directory {}", dir.getAbsolutePath());
            if (!dir.mkdir()) {
                throw new IOException(dir.getAbsolutePath() + " could not be created");
            }
        }
        if (!dir.isDirectory()) {
            throw new IOException(dir.getAbsolutePath() + " does exist, and is not a directory");
        }
    }

}
