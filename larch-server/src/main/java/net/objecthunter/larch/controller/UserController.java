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

import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * Web controller class responsible for larch {@link net.objecthunter.larch.model.Binary} objects
 */
@Controller
public class UserController extends AbstractLarchController {
    @Autowired
    private CredentialsService credentialsService;

    /**
     * Controller method for retrieving a List of existing {@link net.objecthunter.larch.model.security.User}s in the
     * repository as a JSON representation
     * @return A JSON representation of the user list
     * @throws IOException
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<User> retrieveUsers() throws IOException {
        return credentialsService.retrieveUsers();
    }

    /**
     * Controller method for retrieving a HTML view via HTTP GET of all Users and Groups in the repository
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} used for redering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/credentials", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveCredentials() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("users", this.credentialsService.retrieveUsers());
        model.addAttribute("groups", this.credentialsService.retrieveGroups());
        return new ModelAndView("credentials", model);
    }

    /**
     * Controller method to retrieve a list of {@link net.objecthunter.larch.model.security.Group}s that exist in the
     * repository as a JSON representation
     *
     * @return the list of {@link net.objecthunter.larch.model.security.Group}s as a JSON representation
     * @throws IOException
     */
    @RequestMapping(value = "/group", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Group> retrieveGroups() throws IOException {
        return credentialsService.retrieveGroups();
    }



}
