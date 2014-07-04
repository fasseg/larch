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


/**
 * Spring-security JavaConfig class defining the security context of the larch repository
 */
// @Configuration
// @EnableWebSecurity
// @Order(10)
public class LarchServerSecurityConfiguration {

    // @Autowired
    // private Environment env;
    //
    // @Override
    // protected void configure(HttpSecurity http) throws Exception {
    // http.authorizeRequests().requestMatchers(new AntPathRequestMatcher("/", "GET"))
    // .hasAnyRole("USER", "ADMIN", "ANONYMOUS").requestMatchers(
    // new AntPathRequestMatcher("/entity", "POST"))
    // .hasAnyRole("USER", "ADMIN").requestMatchers(new AntPathRequestMatcher("/credentials", "GET"))
    // .hasAnyRole("ADMIN").requestMatchers(new AntPathRequestMatcher("/login", "GET"))
    // .hasAnyRole("USER", "ADMIN")
    // // TODO: add missing matchers for other endpoints
    // .and().httpBasic();
    // http.csrf().requireCsrfProtectionMatcher(new LarchCsrfRequestMatcher());
    // if (!Boolean.valueOf(env.getProperty("larch.security.csrf.enabled", "true"))) {
    // http.csrf().disable();
    // }
    // }
    //
    // private static class LarchCsrfRequestMatcher implements RequestMatcher {
    //
    // private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
    //
    // @Override
    // public boolean matches(HttpServletRequest request) {
    // if (request.getContentType() == null) {
    // return false;
    // }
    // // protect HTML forms from Cross Site forgeries using Sessions
    // if (request.getContentType().equalsIgnoreCase("multipart/form-data")
    // || request.getContentType().equalsIgnoreCase("application/x-www-form-urlencoded")) {
    // return true;
    // }
    // // everything else must authenticate and does not need a CSRF protection
    // return false;
    // }
    // }
}
