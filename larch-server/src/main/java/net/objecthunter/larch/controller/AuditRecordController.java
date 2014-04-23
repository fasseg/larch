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

import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * Web controller for interaction with {@link net.objecthunter.larch.model.AuditRecord} objects
 */
@Controller
public class AuditRecordController extends AbstractLarchController{
    @Autowired
    private AuditService auditService;

    @RequestMapping(value = "/entity/{entity-id}/audit", method = RequestMethod.GET)
    public List<AuditRecord> retrieve(@PathVariable("entity-id") final String entityId,
                                      @RequestParam(value = "offset", defaultValue = "0") final int offset,
                                      @RequestParam(value = "count", defaultValue = "25") final int count) throws
            IOException {
        return auditService.retrieve(entityId, offset, count);
    }

    @RequestMapping(value = "/entity/{entity-id}/audit", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView retrieveHtml(@PathVariable("entity-id") final String entityId,
                                     @RequestParam(value = "offset", defaultValue = "0") final int offset,
                                     @RequestParam(value = "count", defaultValue = "25") final int count) throws
            IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("auditRecords", this.retrieve(entityId, offset, count));
        return new ModelAndView("audit", model);
    }
}
