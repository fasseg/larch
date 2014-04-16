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
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/list")
public class ListController {
    @Autowired
    private SearchService searchService;

    @RequestMapping(method = RequestMethod.GET, produces = {"text/html"})
    @ResponseBody
    public ModelAndView listHtml(@AuthenticationPrincipal User user) {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.list(user));
        model.addAttribute("user", user);
        return new ModelAndView("list", model);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {"application/json", "application/xml", "text/xml"})
    @ResponseBody
    public SearchResult list(@AuthenticationPrincipal User user) {
        return searchService.scanIndex(0);
    }


}
