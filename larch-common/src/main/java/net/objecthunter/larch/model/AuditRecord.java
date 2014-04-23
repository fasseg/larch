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
    public static final String ACTION_CREATE = "Create entity";
    public static final String ACTION_ADD_BINARY = "Add binary";
    public static final String ACTION_ADD_METADATA = "Add metadata";
    public static final String ACTION_UPDATE = "Update entity";
    public static final String ACTION_DELETE_BINARY = "Delete binary";
    public static final String ACTION_DELETE_METADATA = "Delete metadata";
    public static final String ACTION_PUBLISH = "Publish entity";

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
        return "ID:" + this.id + ", TS:" + this.timestamp + ", ACTION:" + this.action + ", ENTITY:" + this.entityId;
    }
}
