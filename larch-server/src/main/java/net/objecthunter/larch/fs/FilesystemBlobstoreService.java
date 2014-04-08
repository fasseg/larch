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
package net.objecthunter.larch.fs;

import net.objecthunter.larch.service.BlobstoreService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.*;

public class FilesystemBlobstoreService implements BlobstoreService {

    private static final Logger log = LoggerFactory.getLogger(FilesystemBlobstoreService.class);

    @Autowired
    private Environment env;

    private RandomStringUtils rnd;

    private File directory;

    @PostConstruct
    public void init() throws IOException {
        this.directory = new File(env.getProperty("fs.path"));
        if (!this.directory.exists()) {
            log.info("Creating non existing data directory {}", this.directory.getAbsolutePath());
            this.directory.mkdir();
        }
        if (!this.directory.isDirectory()) {
            throw new IOException(this.directory.getAbsolutePath() + " does exist, and is not a directory");
        }
    }

    @Override
    public String create(InputStream src) throws IOException {
        final File folder = new File(this.directory, RandomStringUtils.random(2));
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new IOException("unabel to create folder " + folder.getAbsolutePath());
            }
        }
        File data;
        do {
             /* create a new random file name */
            data = new File(folder, RandomStringUtils.random(16));
        } while (data.exists());
        log.debug("creating Blob at {}", data.getAbsolutePath());
        final FileOutputStream sink = new FileOutputStream(data);
        IOUtils.copy(src, sink);
        return folder.getName() + "/" + data.getName();
    }

    @Override
    public InputStream retrieve(String path) throws IOException {
        return new FileInputStream(new File(directory, path));
    }

    @Override
    public void delete(String path) throws IOException {
        final File f = new File(directory, path);
        if (!f.delete()) {
            throw new IOException("Unable to delete file " + f.getAbsolutePath());
        }
    }

    @Override
    public void update(String path, InputStream src) throws IOException {
        final File data = new File(directory, path);
        if (!data.exists()) {
            throw new IOException(data.getAbsolutePath() + " can not be updated sine it does not exist");
        }
        IOUtils.copy(src, new FileOutputStream(data));
    }
}
