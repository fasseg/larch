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

import java.util.List;
import java.util.Map;

/**
 * A DTO for a top level larch repository object. Creating any object in the larch repository normally starts with
 * creating an {@link Entity} and adding content/metadata to it.
 */
public class Entity {
    private int version;
    private String id;
    private String label;
    private String type;
    private String parentId;
    private String state;
    private String utcCreated;
    private String utcLastModified;
    private List<String> tags;
    private List<String> children;
    private Map<String, Metadata> metadata;
    private Map<String, Binary> binaries;
    private Map<Integer, String> versionPaths;
    private Map<String, List<String>> relations;

    public Map<String, List<String>> getRelations() {
        return relations;
    }

    public void setRelations(Map<String, List<String>> relations) {
        this.relations = relations;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public String getUtcLastModified() {
        return utcLastModified;
    }

    public void setUtcLastModified(String utcLastModified) {
        this.utcLastModified = utcLastModified;
    }

    public String getUtcCreated() {
        return utcCreated;
    }

    public void setUtcCreated(String utcCreated) {
        this.utcCreated = utcCreated;
    }

    public Map<Integer, String> getVersionPaths() {
        return versionPaths;
    }

    public void setVersionPaths(Map<Integer, String> versionPaths) {
        this.versionPaths = versionPaths;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Map<String, Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Metadata> metadata) {
        this.metadata = metadata;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Binary> getBinaries() {
        return binaries;
    }

    public void setBinaries(Map<String, Binary> binaries) {
        this.binaries = binaries;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
