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

import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller responsible for search views
 */
@Controller
@RequestMapping("/search")
public class SearchController extends AbstractLarchController {
    @Autowired
    private SearchService searchService;

    /**
     * Controller method for searching {@link net.objecthunter.larch.model.Entity}s in the repository using an HTTP
     * POST which returns a JSON representation of the {@link net.objecthunter.larch.model.SearchResult}
     *
     * @param query The search query
     * @return A {@link net.objecthunter.larch.model.SearchResult} containing the found {@link net.objecthunter.larch
     * .model.Entity}s as s JSON representation
     */
    @RequestMapping(method = RequestMethod.POST, produces = {"application/json"})
    public SearchResult searchMatchFields(@RequestParam("term") final String query) {
        return searchService.searchEntities(query);
    }


    /**
     * Controller method for displaying a HTML search page
     *
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public ModelAndView searchHtml() {
        final ModelMap model = new ModelMap();
        return new ModelAndView("search", model);
    }

    /**
     * Controller method for searching {@link net.objecthunter.larch.model.Entity}s in the repository using an HTTP
     * POST which returns a HTML view of the {@link net.objecthunter.larch.model.SearchResult}
     *
     * @param query The search query
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(method = RequestMethod.POST, produces = {"text/html"})
    public ModelAndView searchMatchFieldsHtml(@RequestParam("term") final String query) {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", searchMatchFields(query));
        return new ModelAndView("searchresult", model);
    }
}
