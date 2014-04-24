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

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class SizeCalculatingDigestInputStream extends DigestInputStream {
    private long bytesRead = 0;

    public SizeCalculatingDigestInputStream(InputStream stream, MessageDigest digest) {
        super(stream, digest);
    }

    @Override
    public int read() throws IOException {
        bytesRead++;
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = super.read(b);
        bytesRead += read;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        bytesRead += read;
        return read;
    }

    /**
     * returns the number of bytes produced by this {@link java.io.InputStream}
     * @return
     */
    public long getBytesRead() {
        return bytesRead;
    }
}
