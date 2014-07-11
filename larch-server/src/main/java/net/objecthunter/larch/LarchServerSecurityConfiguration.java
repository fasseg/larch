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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring-security JavaConfig class defining the security context of the larch repository
 */
@Configuration
@EnableWebSecurity
public class LarchServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("larchElasticSearchAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requestMatchers()
                .antMatchers("/**")
                .and()
                .anonymous()
                .authorities("ROLE_ANONYMOUS")
                .and().authorizeRequests().requestMatchers(new AntPathRequestMatcher("/oauth/authorize")).hasAnyRole(
                        "USER",
                        "ADMIN")
                .and().httpBasic();
        http.csrf().requireCsrfProtectionMatcher(new LarchCsrfRequestMatcher());
        if (!Boolean.valueOf(env.getProperty("larch.security.csrf.enabled", "true"))) {
            http.csrf().disable();
        }
    }

}
