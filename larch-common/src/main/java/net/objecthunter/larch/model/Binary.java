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
package net.objecthunter.larch.model;

import java.util.HashMap;
import java.util.Map;

public class Binary {
    private final String name;
    private final long size;
    private final String mimetype;
    private final Map<String, Metadata> metadata;
    private final String filename;

    private Binary() {
        this.name = null;
        this.size = 0l;
        this.mimetype = null;
        this.metadata = null;
        this.filename = null;
    }

    private Binary(Builder b) {
        this.name = b.name;
        this.size = b.size;
        this.mimetype = b.mimetype;
        this.metadata = b.metadata;
        this.filename = b.filename;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getMimetype() {
        return mimetype;
    }

    public Map<String, Metadata> getMetadata() {
        return metadata;
    }

    public String getFilename() {
        return filename;
    }

    public static class Builder {
        private final String name;
        private long size;
        private Map<String, Metadata> metadata = new HashMap<>();
        private String mimetype;
        private String filename;

        public Builder(String name) {
            this.name = name;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder metadata(Map<String, Metadata> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public Builder metadata(Metadata md) {
            this.metadata.put(md.getName(), md);
            return this;
        }

        public Builder mimetype(String mimetype) {
            this.mimetype = mimetype;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Binary build() {
            return new Binary(this);
        }
    }
}
