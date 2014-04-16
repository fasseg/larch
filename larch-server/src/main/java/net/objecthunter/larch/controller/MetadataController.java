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
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.IndexService;
import net.objecthunter.larch.service.SchemaService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Controller
public class MetadataController {
    @Autowired
    private EntityService entityService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(value = "/entity/{id}/metadata", method= RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public String addMetadata(@PathVariable("id") final String entityId, @RequestParam("name") final String mdName, @RequestParam("type") final String type, @RequestParam("metadata") final MultipartFile file) throws IOException {
        final Entity e = entityService.retrieve(entityId);
        if (e.getMetadata() == null) {
            e.setMetadata(new HashMap<>());
        } else if( e.getMetadata().get(mdName) != null) {
            throw new IOException("Metdata " + mdName + " already exists on Entity " + entityId);
        }
        final Metadata md = new Metadata();
        md.setName(mdName);
        md.setSchemaUrl(schemaService.getSchemUrlForType(type));
        md.setData(IOUtils.toString(file.getInputStream()));
        md.setMimetype(file.getContentType());
        md.setType(type);
        md.setOriginalFilename(file.getOriginalFilename());
        e.getMetadata().put(mdName, md);
        entityService.update(e);
        return "redirect:/entity/" + entityId;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/entity/{id}/metadata/{metadata-name}/content", produces = {"application/xml", "text/xml"})
    @ResponseStatus(HttpStatus.OK)
    public void retrieveXml(@PathVariable("id") final String id, @PathVariable("metadata-name") final String metadataName, @RequestHeader("Accept") final String accept, final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "inline");
        final String data = indexService.retrieve(id).getMetadata().get(metadataName).getData();
        IOUtils.write(data, resp.getOutputStream());
        resp.flushBuffer();
    }

}
