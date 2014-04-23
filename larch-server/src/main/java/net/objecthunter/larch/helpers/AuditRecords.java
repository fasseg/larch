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

import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.model.security.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public abstract class AuditRecords {
    public static AuditRecord skeletonRecord(String entityId) {
        final AuditRecord record = new AuditRecord();
        record.setEntityId(entityId);
        record.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
        final User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        record.setAgentName(u.getName());
        return record;
    }

    public static AuditRecord createEntityRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_ENTITY);
        return record;
    }
    public static AuditRecord updateEntityRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_ENTITY);
        return record;
    }
    public static AuditRecord deleteEntityRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_ENTITY);
        return record;
    }
    public static AuditRecord createBinaryRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_BINARY);
        return record;
    }
    public static AuditRecord updateBinaryRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_UPDATE_BINARY);
        return record;
    }
    public static AuditRecord deleteBinaryRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_BINARY);
        return record;
    }
    public static AuditRecord createRelationRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_RELATION);
        return record;
    }
    public static AuditRecord updateRelationRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_UPDATE_RELATION);
        return record;
    }
    public static AuditRecord deleteRelationRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_RELATION);
        return record;
    }
    public static AuditRecord createMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_METADATA);
        return record;
    }
    public static AuditRecord updateMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_UPDATE_METADATA);
        return record;
    }
    public static AuditRecord deleteMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_METADATA);
        return record;
    }

}
