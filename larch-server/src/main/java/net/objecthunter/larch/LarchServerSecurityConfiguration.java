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

package net.objecthunter.larch;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Spring-security JavaConfig class defining the security context of the larch repository
 */
@Configuration
@EnableWebSecurity
@Order(10)
public class LarchServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers(new AntPathRequestMatcher("/", "GET"))
                    .hasAnyRole("USER", "ADMIN", "ANONYMOUS")
                .requestMatchers(new AntPathRequestMatcher("/entity", "POST"))
                    .hasAnyRole("USER", "ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/credentials", "GET"))
                    .hasAnyRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/settings", "GET"))
                    .hasAnyRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/login", "GET"))
                    .hasAnyRole("USER", "ADMIN")
                .and().httpBasic();
        // TODO: add missing matchers for other endpoints
        http.csrf().requireCsrfProtectionMatcher(new LarchCsrfRequestMatcher());
        if (!Boolean.valueOf(env.getProperty("larch.security.csrf.enabled", "true"))) {
            http.csrf().disable();
        }
    }

    private static class LarchCsrfRequestMatcher implements RequestMatcher {

        private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

        @Override
        public boolean matches(HttpServletRequest request) {
            if (request.getContentType() == null) {
                return false;
            }
            // protect HTML forms from Cross Site forgeries using Sessions
            if (request.getContentType().equalsIgnoreCase("multipart/form-data")
                    || request.getContentType().equalsIgnoreCase("application/x-www-form-urlencoded")) {
                return true;
            }
            // everything else must authenticate and does not need a CSRF protection
            return false;
        }
    }
}
