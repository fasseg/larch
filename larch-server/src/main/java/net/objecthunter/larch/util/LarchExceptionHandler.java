/*
 * Copyright 2014 Michael Hoppe
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

package net.objecthunter.larch.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author mih
 */
@ControllerAdvice
public class LarchExceptionHandler {

    @Autowired
    private ObjectMapper mapper;

    public static final String DEFAULT_ERROR_VIEW = "error";

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ IOException.class })
    @ResponseBody
    public Object ioRequestExceptionHandler(HttpServletRequest req, Exception e)
            throws Exception {
        return handleException(req, e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler({ FileNotFoundException.class })
    @ResponseBody
    public Object notFoundRequestExceptionHandler(HttpServletRequest req, Exception e)
            throws Exception {
        return handleException(req, e, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler({ SocketException.class })
    @ResponseBody
    public Object alreadyExistsRequestExceptionHandler(HttpServletRequest req, Exception e)
            throws Exception {
        return handleException(req, e, HttpStatus.CONFLICT);
    }

    /**
     * Convert Exception either to a JSON-String or to a ModelAndView, depending on Accept-Header of request.
     * 
     * @param req HttpServletRequest
     * @param e Exception
     * @param status HttpStatus
     * @return JSON-String or ModelAndView, depending on Accept-Header of request
     * @throws Exception
     */
    private Object handleException(HttpServletRequest req, Exception e, HttpStatus status) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.addObject("timestamp", new Date());
        mav.addObject("status", status.value());
        mav.addObject("error", e.getClass().getName());
        mav.addObject("message", e.getMessage());
        mav.addObject("path", req.getRequestURL());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        if (req.getHeader("Accept") != null && req.getHeader("Accept").contains("html")) {
            return mav;
        }
        else {
            ModelMap m = mav.getModelMap();
            return mapper.writeValueAsString(m);
        }
    }

}