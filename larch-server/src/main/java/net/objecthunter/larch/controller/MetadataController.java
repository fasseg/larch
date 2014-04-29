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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.helpers.AuditRecords;
import net.objecthunter.larch.model.*;
import net.objecthunter.larch.service.AuditService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.IndexService;
import net.objecthunter.larch.service.SchemaService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Web controller responsible for interaction on the meta data level
 */
@Controller
public class MetadataController extends AbstractLarchController {
    @Autowired
    private EntityService entityService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an {@link net
     * .objecthunter.larch.model.Entity} using a HTTP POST with multipart/form-data
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param mdName The name of the Metadata
     * @param type The type of the Metadata
     * @param file The Spring MVC injected MutlipartFile containing the actual data from a html form submission
     * @return a redirection to the Entity to which the Metadata was added
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/metadata", method= RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public String addMetadata(@PathVariable("id") final String entityId, @RequestParam("name") final String mdName, @RequestParam("type") final String type, @RequestParam("metadata") final MultipartFile file) throws IOException {
        final Entity e = entityService.retrieve(entityId);
        if (e.getMetadata() == null) {
            e.setMetadata(new HashMap<>());
        } else if( e.getMetadata().get(mdName) != null) {
            throw new IOException("Metdata " + mdName + " already exists on Entity " + entityId);
        }
        final Metadata md = new Metadata();
        md.setName(mdName);
        md.setData(IOUtils.toString(file.getInputStream()));
        md.setMimetype(file.getContentType());
        md.setType(type);
        md.setOriginalFilename(file.getOriginalFilename());
        e.getMetadata().put(mdName, md);
        entityService.update(e);
        this.auditService.create(AuditRecords.createMetadataRecord(entityId));
        return "redirect:/entity/" + entityId;
    }

    /**
     * Controller method to retrieve the XML data of a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Entity} using a HTTP GET
     * @param id The id of the Entity
     * @param metadataName The name of the Metadata to retrieve
     * @param accept the Spring MVC injected accept header of the HTTP GET request
     * @param resp the Spting MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *             directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/entity/{id}/metadata/{metadata-name}/content", produces = {"application/xml", "text/xml"})
    @ResponseStatus(HttpStatus.OK)
    public void retrieveXml(@PathVariable("id") final String id, @PathVariable("metadata-name") final String metadataName, @RequestHeader("Accept") final String accept, final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "inline");
        final String data = indexService.retrieve(id).getMetadata().get(metadataName).getData();
        IOUtils.write(data, resp.getOutputStream());
        resp.flushBuffer();
    }

    /**
     * Controller method to request the validation result for a {@link net.objecthunter.larch.model.Metadata} object
     * of a given {@link net.objecthunter.larch.model.Entity}
     * @param id the is of the Entity
     * @param metadataName the name of the Metadata
     * @return A JSON representation of a {@link net.objecthunter.larch.model.MetadataValidationResult}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/entity/{id}/metadata/{metadata-name}/validate",
            produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MetadataValidationResult validate(@PathVariable("id") final String id,
                                             @PathVariable("metadata-name") final String metadataName) throws IOException {
        return this.schemaService.validate(id, metadataName);
    }

    /**
     * Controller method to retrieve the available {@link net.objecthunter.larch.model.MetadataType}s in the
     * repository as a JSON representation
     * @return A JSON representation of a list of MetadataType objects
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value="/metadatatype", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<MetadataType> retrieveTypes() throws IOException {
        return this.schemaService.getSchemaTypes();
    }

    /**
     * Controller method to retrieve the available {@link net.objecthunter.larch.model.MetadataType}s in the
     * repository in a HTML view
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value="/metadatatype", produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveTypesHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("types", this.schemaService.getSchemaTypes());
        return new ModelAndView("metadatatype", model);
    }

    /**
     * Add a new {@link net.objecthunter.larch.model.MetadataType} to the repository that can be used to validate
     * different kind of Metadata objects.
     * @param src A JSON representation of the new {@link net.objecthunter.larch.model.MetadataType}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, value="/metadatatype", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void addSchemaType(final InputStream src) throws IOException {
        final MetadataType newType = mapper.readValue(src, MetadataType.class);
        this.schemaService.createSchemaType(newType);
    }

    /**
     * Add a new {@link net.objecthunter.larch.model.MetadataType} to the repository that can be used to validate
     * different kind of Metadata objects, using a HTML form
     * @param name The name of the new {@link net.objecthunter.larch.model.MetadataType}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, value="/metadatatype", consumes = "multipart/form-data",
            produces ="text/html")
    @ResponseStatus(HttpStatus.CREATED)
    public String addSchemaType(@RequestParam("name") final String name, @RequestParam("schemaUrl") final String
            schemUrl) throws IOException {
        final MetadataType newType = new MetadataType();
        newType.setName(name);
        newType.setSchemaUrl(schemUrl);
        this.schemaService.createSchemaType(newType);
        return "redirect:/metadatatype";
    }

}
