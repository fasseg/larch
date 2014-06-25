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
import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.service.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Web controller class responsible for larch {@link net.objecthunter.larch.model.Binary} objects
 */
@Controller
public class UserController extends AbstractLarchController {
    @Autowired
    private CredentialsService credentialsService;

    /**
     * Controller method for confirming a {@link net.objecthunter.larch.model.security.UserRequest}
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView confirmUserRequest(@PathVariable("token") final String token) throws IOException {
        final UserRequest req = this.credentialsService.retrieveUserRequest(token);
        final ModelMap model = new ModelMap();
        model.addAttribute("token", token);
        return new ModelAndView("confirm", model);
    }

    /**
     * Controller method for confirming a {@link net.objecthunter.larch.model.security.UserRequest}
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView confirmUserRequest(@PathVariable("token") final String token,
                                           @RequestParam("password") final String password,
                                           @RequestParam("passwordRepeat") final String passwordRepeat) throws IOException {
        final User u = this.credentialsService.createUser(token, password, passwordRepeat);
        final ModelMap model = new ModelMap();
        return success("The user " + u.getName() + " has been created.");
    }

    /**
     * Controller method for deleting a given {@link net.objecthunter.larch.model.security.User}
     */
    @RequestMapping(value = "/user/{name}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable("name") final String name) throws IOException {
        this.credentialsService.deleteUser(name);
    }

    /**
     * Controller method for retrieving a List of existing {@link net.objecthunter.larch.model.security.User}s in the
     * repository as a JSON representation
     *
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
     * Controller method for creating a new {@link net.objecthunter.larch.model.security.User}
     *
     * @param userName  the name of the user
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @param email     the user's mail address
     * @param groups    the user's groups
     * @throws IOException if the user could not be created
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String createUser(@RequestParam("name") final String userName,
                             @RequestParam("first_name") final String firstName,
                             @RequestParam("last_name") final String lastName,
                             @RequestParam("email") final String email,
                             @RequestParam("groups") final List<String> groups) throws IOException {
        final User u = new User();
        u.setName(userName);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        final List<Group> groupList = new ArrayList<>(groups.size());
        for (String groupName : groups) {
            final Group g = new Group();
            g.setName(groupName);
            groupList.add(g);
        }
        u.setGroups(groupList);
        UserRequest request = this.credentialsService.createNewUserRequest(u);
        return "http://localhost:8080/confirm/" + request.getToken();
    }

    /**
     * Controller method for retrieving an existing {@link net.objecthunter.larch.model.security.User}s in the
     * repository as a JSON representation
     *
     * @return A JSON representation of the user
     * @throws IOException
     */
    @RequestMapping(value = "/user/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public User retrieveUser(@PathVariable("name") final String name) throws IOException {
        return credentialsService.retrieveUser(name);
    }

    /**
     * Controller method for retrieving an existing {@link net.objecthunter.larch.model.security.User}s in the
     * repository as a JSON representation
     *
     * @param name The user's name
     * @return A JSON representation of the user
     * @throws IOException
     */
    @RequestMapping(value = "/user/{name}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveUserHtml(@PathVariable("name") final String name) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("user", credentialsService.retrieveUser(name));
        model.addAttribute("groups", credentialsService.retrieveGroups());
        return new ModelAndView("user", model);
    }

    /**
     * Controller method for retrieving a HTML view via HTTP GET of all Users and Groups in the repository
     *
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

    @RequestMapping(value = "/user/{name}", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView updateUserDetails(@PathVariable("name") final String username,
                                          @RequestParam("firstName") final String firstName,
                                          @RequestParam("lastName") final String lastName,
                                          @RequestParam("email") final String email,
                                          @RequestParam("groups") final List<String> groupNames) throws IOException {
        final User u = this.credentialsService.retrieveUser(username);
        u.setLastName(lastName);
        u.setFirstName(firstName);
        u.setEmail(email);
        u.setGroups(this.credentialsService.retrieveGroups(groupNames));
        this.credentialsService.updateUser(u);
        return success("The user " + username + " has been updated");
    }


}
