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

package net.objecthunter.larch.controller;

import java.io.IOException;

import net.objecthunter.larch.helpers.AuditRecords;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.MessagingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Web controller responsible for interactions on the relation level
 */
@Controller
@RequestMapping("//workspace/{workspaceId}/entity/{id}/relation")
public class RelationController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private MessagingService messagingService;

    /**
     * Controller method for adding a new triple relating an {@link net.objecthunter.larch.model.Entity} via a
     * predicate to an object using a HTTP POST
     * 
     * @param id the id of the Entity which should be the subject of this relation
     * @param predicate the predicate of the relation
     * @param object the object of the relation
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("workspaceId") final String workspaceId, @PathVariable("id") final String id, @RequestParam("predicate") final String predicate,
            @RequestParam("object") final String object) throws IOException {
        this.entityService.createRelation(workspaceId, id, predicate, object);
        this.entityService.createAuditRecord(AuditRecords.createRelationRecord(id));
        this.messagingService.publishCreateRelation(id, predicate, object);
    }

    /**
     * Controller method for adding a new triple relating an {@link net.objecthunter.larch.model.Entity} via a
     * predicate to an object using a HTTP POSTm that redirects to an HTML view of the
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the id of the Entity which should be the subject of this relation
     * @param predicate the predicate of the relation
     * @param object the object of the relation
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public String createHtml(@PathVariable("workspaceId") final String workspaceId, @PathVariable("id") final String id, @RequestParam("predicate") final String predicate,
            @RequestParam("object") final String object) throws IOException {
        this.entityService.createRelation(workspaceId, id, predicate, object);
        this.messagingService.publishCreateRelation(id, predicate, object);
        return "redirect:/entity/" + id;
    }
}
