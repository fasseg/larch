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

import net.objecthunter.larch.model.Settings;
import net.objecthunter.larch.service.RepositoryService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * Web Controller responsible for Settings views
 */
@RequestMapping("/settings")
@Controller
public class SettingsController {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private BackendBlobstoreService blobstoreService;

    @Autowired
    private BackendEntityService entityService;

    /**
     * Retrieve the settings overview page from the repository
     *
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveHtml() throws IOException {
        return new ModelAndView("settings", new ModelMap("settings", this.retrieve()));
    }

    /**
     * Retrieve the settings response
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Settings retrieve() throws IOException {
        return this.createSettings();
    }

    private Settings createSettings() throws IOException {
        final Settings settings = new Settings();
        settings.setLarchState(this.repositoryService.status());
        settings.setDescribe(this.repositoryService.describe());
        settings.setIndexState(this.entityService.status());
        settings.setBlobstoreState(this.blobstoreService.status());
        return settings;
    }
}
