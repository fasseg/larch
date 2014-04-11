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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class BrowseController {
    @Autowired
    private SearchService searchService;

    @RequestMapping(value = "/browse", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browse() throws IOException {
        return this.searchService.scanIndex(0);
    }

    @RequestMapping(value = "/browse/{offset}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browse(@PathVariable("offset") final int offset) throws IOException {
        return this.searchService.scanIndex(offset);
    }

    @RequestMapping(value = "/browse/{offset}/{numrecords}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browse(@PathVariable("offset") final int offset, @PathVariable("numrecords") final int numRecords) throws IOException {
        return this.searchService.scanIndex(offset, numRecords);
    }

    @RequestMapping(value = "/browse", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseHtml() throws IOException {
        return new ModelAndView("browse","result",this.searchService.scanIndex(0));
    }

    @RequestMapping(value = "/browse/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseHtml(@PathVariable("offset") final int offset) throws IOException {
        return new ModelAndView("browse","result",this.searchService.scanIndex(offset));
    }

    @RequestMapping(value = "/browse/{offset}/{numrecords}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseHtml(@PathVariable("offset") final int offset, @PathVariable("numrecords") final int numRecords) throws IOException {
        return new ModelAndView("browse","result",this.searchService.scanIndex(offset, numRecords));
    }
}
