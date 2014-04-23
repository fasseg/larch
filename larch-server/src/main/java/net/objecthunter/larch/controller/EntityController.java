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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.impl.DefaultEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Web controller responsible for interactions on the entity level
 */
@Controller
@RequestMapping("/entity")
public class EntityController {

    @Autowired
    private DefaultEntityService entityService;

    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    public void patch(@PathVariable("id") final String id, final InputStream src) throws IOException {
        final JsonNode node = mapper.readTree(src);
        this.entityService.patch(id, node);
    }

    @RequestMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Entity retrieve(@PathVariable("id") final String id) throws IOException {
        return entityService.retrieve(id);
    }

    @RequestMapping(value = "/{id}", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView retrieveHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entity", entityService.retrieve(id));
        return new ModelAndView("entity", model);
    }

    @RequestMapping("/{id}/version/{version}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Entity retrieve(@PathVariable("id") final String id, @PathVariable("version") final int version) throws IOException {
        return entityService.retrieve(id, version);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "text/plain")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String create(final InputStream src) throws IOException {
        return this.entityService.create(mapper.readValue(src, Entity.class));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") final String id, final InputStream src) throws IOException {
        final Entity e = mapper.readValue(src, Entity.class);
        if (e.getId() == null) {
            e.setId(id);
        } else if (!e.getId().equals(id)) {
            throw new IOException("The id of the Entity and the id used in the PUT request are not the same");
        }
        this.entityService.update(e);
    }
}
