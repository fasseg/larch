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

import org.uncommons.maths.random.XORShiftRNG;

import java.io.IOException;
import java.io.InputStream;

public class RandomInputStream extends InputStream {

    private final long size;

    private long bytesRead;

    private final int sliceLen;

    private int slicePos;

    public static final byte[] RANDOM_SLICE = new byte[65535];

    public static final XORShiftRNG RNG = new XORShiftRNG();

    static {
        RNG.nextBytes(RANDOM_SLICE);
    }

    public RandomInputStream(long size) {
        super();
        this.size = size;
        this.sliceLen = RANDOM_SLICE.length;
    }

    @Override
    public int read() throws IOException {
        if (slicePos == 0 || slicePos == sliceLen - 1) {
            slicePos = RNG.nextInt((int) Math.floor(sliceLen / 2f));
        }
        return RANDOM_SLICE[slicePos++];
    }

    @Override
    public int read(byte[] b) throws IOException {
        int i = 0;
        for (; i < b.length; ++i) {
            b[i] = (byte) read();
        }
        return i;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int i = 0;
        for (; i < len; ++i) {
            b[i] = (byte) read();
        }
        return i;
    }
}