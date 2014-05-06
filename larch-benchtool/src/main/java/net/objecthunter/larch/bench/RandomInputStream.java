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
import org.uncommons.maths.random.XORShiftRNG;

import java.io.IOException;
import java.io.InputStream;

public class RandomInputStream extends InputStream {

    private static final byte[] RANDOM_SLICE = new byte[65535];
    private static final XORShiftRNG RNG = new XORShiftRNG();
    private static final Logger log = LoggerFactory.getLogger(RandomInputStream.class);

    static {
        RNG.nextBytes(RANDOM_SLICE);
    }

    private final long size;
    private long bytesRead;
    private final int sliceLen;
    private int slicePos;

    public RandomInputStream(long size) {
        super();
        this.size = size;
        this.sliceLen = RANDOM_SLICE.length;
    }

    @Override
    public int read() throws IOException {
        if (bytesRead >= size) {
            log.debug("returning -1 after {} bytes", bytesRead);
            return -1;
        }
        if (slicePos == 0 || slicePos == sliceLen - 1) {
            slicePos = RNG.nextInt((int) Math.floor(sliceLen / 2f));
        }
        bytesRead++;
        return RANDOM_SLICE[slicePos++];
    }

    @Override
    public int read(byte[] b) throws IOException {
        int i = 0;
        int data = 0;
        for (; i < b.length; ++i) {
            if ((data = read()) == -1) {
                return i == 0 ? -1 : i;
            }
            b[i] = (byte) data;
        }
        return i;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int i = 0;
        int data = 0;
        for (; i < len; ++i) {
            if ((data = read()) == -1) {
                return i == 0 ? -1 : i;
            }
            b[i] = (byte) data;
        }
        return i;
    }
}