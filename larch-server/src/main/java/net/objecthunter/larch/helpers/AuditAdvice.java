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
import net.objecthunter.larch.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Aspect
public class AuditAdvice {

    @Autowired
    private AuditService auditService;

    private static final Logger log = LoggerFactory.getLogger(AuditAdvice.class);


    @AfterReturning(value = "@annotation(audit)", returning = "result")
    public void createAuditRecord(Audit audit, Object result) throws IOException {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final AuditRecord auditRecord = new AuditRecord();
        auditRecord.setAgentName(user.getName());
        auditRecord.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
        auditRecord.setEntityId(result.toString());
        auditRecord.setAction(audit.value());
        this.auditService.create(auditRecord);
        log.debug("audit record {}", auditRecord);
    }
}
