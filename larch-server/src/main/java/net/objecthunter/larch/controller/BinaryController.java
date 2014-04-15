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

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.BlobstoreService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.IndexService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class BinaryController {
    @Autowired
    private EntityService entityService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private BlobstoreService blobstoreService;

    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST, consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    @ResponseStatus(HttpStatus.OK)
    public String create(@PathVariable("id") final String entityId, @RequestParam("name") final String name, @RequestParam("binary") final MultipartFile file) throws IOException {
        entityService.createBinary(entityId, name, file.getContentType(), file.getInputStream());
        return "redirect:/entity/" + entityId;
    }
    @RequestMapping(value = "/entity/{id}/binary/{binary-name}/content", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void download(@PathVariable("id") final String id, @PathVariable("binary-name") final String name, final HttpServletResponse response) throws IOException {
        // TODO: Content Size
        final Entity e = indexService.retrieve(id);
        final Binary bin =e.getBinaries().get(name);
        response.setContentType(bin.getMimetype());
        response.setContentLength(-1);
        response.setHeader("Content-Disposition", "inline");
        IOUtils.copy(blobstoreService.retrieve(bin.getPath()), response.getOutputStream());
        response.flushBuffer();
    }

}
