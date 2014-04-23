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

/**
 * DTO class for wrapping audit information
 */
public class AuditRecord {
    public static final String EVENT_CREATE_ENTITY = "Create entity";
    public static final String EVENT_UPDATE_ENTITY = "Update entity";
    public static final String EVENT_PUBLISH_ENTITY = "Publish entity";
    public static final String EVENT_CREATE_BINARY = "Create binary";
    public static final String EVENT_DELETE_BINARY = "Delete binary";
    public static final String EVENT_CREATE_METADATA = "Create metadata";
    public static final String EVENT_DELETE_METADATA = "Delete metadata";
    public static final String EVENT_CREATE_RELATION = "Add relation";
    public static final String EVENT_DELETE_RELATION = "Delete relation";
    public static final String EVENT_DELETE_ENTITY = "Delete entity";
    public static final String EVENT_UPDATE_BINARY = "Update binary";
    public static final String EVENT_UPDATE_RELATION = "Update relation";
    public static final String EVENT_UPDATE_METADATA = "Update metadata";

    private String id;
    private String entityId;
    private String agentName;
    private String action;
    private String timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ID:" + this.id + ", TS:" + this.timestamp + ", EVENT:" + this.action + ", ENTITY:" + this.entityId;
    }
}
