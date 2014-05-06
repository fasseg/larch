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
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.*;
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

/**
 * Web controller class responsible for larch {@link net.objecthunter.larch.model.Binary} objects
 */
@Controller
public class BinaryController extends AbstractLarchController {
    @Autowired
    private EntityService entityService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private BlobstoreService blobstoreService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for adding a {@link net.objecthunter.larch.model.Binary} to an existing {@link net
     * .objecthunter.larch.model.Entity} using a multipart/form-data encoded HTTP POST
     *
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s to which the created Binary should get added.
     * @param name     The name of the Binary
     * @param file     A {@link org.springframework.web.multipart.MultipartFile} containing the multipart encoded file
     * @return The redirect address to view the updated Entity
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST, consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    @ResponseStatus(HttpStatus.OK)
    public String create(@PathVariable("id") final String entityId, @RequestParam("name") final String name, @RequestParam("binary") final MultipartFile file) throws IOException {
        entityService.createBinary(entityId, name, file.getContentType(), file.getInputStream());
        this.auditService.create(AuditRecords.createBinaryRecord(entityId));
        return "redirect:/entity/" + entityId;
    }

    /**
     * Controller method for adding a {@link net.objecthunter.larch.model.Binary} to an existing {@link net
     * .objecthunter.larch.model.Entity} using a multipart/form-data encoded HTTP POST
     *
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s to which the created Binary should get added.
     * @param name     The name of the Binary
     * @param src     The request body containing the actual data
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("id") final String entityId,
                         @RequestParam("name") final String name,
                         @RequestParam("mimetype") final String mimeType,
                         final InputStream src) throws IOException {
        entityService.createBinary(entityId, name, mimeType, src);
        this.auditService.create(AuditRecords.createBinaryRecord(entityId));
    }

    /**
     * Controller method for adding a {@link net.objecthunter.larch.model.Binary} to an existing {@link net
     * .objecthunter.larch.model.Entity} using a application/json POST
     *
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s to which the created Binary should get added.
     * @param src      An Inputstream holding the request body's content
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("id") final String entityId, final InputStream src) throws IOException {
        final Binary b = this.mapper.readValue(src, Binary.class);
        this.entityService.createBinary(entityId, b.getName(), b.getMimetype(), b.getSource().getInputStream());
        this.auditService.create(AuditRecords.createBinaryRecord(entityId));
    }

    /**
     * Controller method to retrieve a JSON representation of a {@link net.objecthunter.larch.model.Binary} from the
     * repository
     *
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s id, which contains the requested Binary
     * @param name     The name of the Binary
     * @return The Binary object requested
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Binary retrieve(@PathVariable("id") final String entityId, @PathVariable("name") final String name) throws IOException {
        final Entity e = this.entityService.retrieve(entityId);
        if (e.getBinaries() == null || !e.getBinaries().containsKey(name)) {
            throw new IOException("The Binary " + name + " does not exist on the entity " + entityId);
        }
        return e.getBinaries().get(name);
    }

    /**
     * Controller method to retrieve the HTML representation of a {@link net.objecthunter.larch.model.Binary}
     *
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s id, which contains the requested Binary
     * @param name     The name of the Binary
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} object used for rendering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{name}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveHtml(@PathVariable("id") final String entityId, @PathVariable("name") final String name)
            throws
            IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("binary", this.retrieve(entityId, name));
        model.addAttribute("metadataTypes", this.schemaService.getSchemaTypes());
        return new ModelAndView("binary", model);
    }

    /**
     * Controller method for downloading the content (i.e. The actual bytes) of a {@link net.objecthunter.larch.model
     * .Binary}.
     *
     * @param id       The {@link net.objecthunter.larch.model.Entity}'s id, which contains the requested Binary
     * @param name     The name of the Binary
     * @param response The {@link javax.servlet.http.HttpServletResponse} which gets injected by Spring MVC. This is
     *                 used to write the actual byte stream to the client.
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{binary-name}/content", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void download(@PathVariable("id") final String id, @PathVariable("binary-name") final String name, final HttpServletResponse response) throws IOException {
        // TODO: Content Size
        final Entity e = indexService.retrieve(id);
        final Binary bin = e.getBinaries().get(name);
        response.setContentType(bin.getMimetype());
        response.setContentLength(-1);
        response.setHeader("Content-Disposition", "inline");
        IOUtils.copy(blobstoreService.retrieve(bin.getPath()), response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Controller method to delete a binary
     *
     * @param entityId the entity's id
     * @param name     the name of the binary to delete
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{name}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void delete(@PathVariable("id") final String entityId, @PathVariable("name") final String name) throws
            IOException {
        this.entityService.deleteBinary(entityId, name);
        this.auditService.create(AuditRecords.deleteEntityRecord(entityId));
    }
}
