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

import java.util.*;

public class Entity {
    private final String id;
    private final String label;
    private final String type;
    private final List<String> tags;
    private final String parentId;
    private final Map<String, Metadata> metadata;
    private final Map<String, Binary> binaries;

    private Entity(Builder b) {
        this.id = b.id;
        this.label = b.label;
        this.tags = b.tags;
        this.parentId = b.parentId;
        this.metadata = b.metadata;
        this.type = b.type;
        this.binaries = b.binaries;
    }
    private Entity() {
        this.binaries = null;
        this.id = null;
        this.label = null;
        this.tags = null;
        this.parentId = null;
        this.metadata = null;
        this.type = null;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getParentId() {
        return parentId;
    }

    public Map<String, Metadata> getMetadata() {
        return metadata;
    }

    public String getType() {
        return type;
    }

    public static class Builder  {
        private final String id;
        private String label;
        private String type;
        private List<String> tags = new ArrayList<>();
        private String parentId;
        private Map<String, Metadata> metadata = new HashMap<>();
        public Map<String,Binary> binaries = new HashMap<>();

        public Builder(String id) {
            this.id = id;
        }

        public Builder binaries(Map<String, Binary> binaries) {
            this.binaries.putAll(binaries);
            return this;
        }

        public Builder binary(Binary binary) {
            this.binaries.put(binary.getName(), binary);
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder tags(Collection<String> tags) {
            this.tags.addAll(tags);
            return this;
        }

        public Builder tag(String name) {
            this.tags.add(name);
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

        public Entity build() {
            return new Entity(this);
        }
    }
}
