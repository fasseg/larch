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

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Spring-security JavaConfig class defining the security context of the larch repository
 */
public class LarchServerSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/").hasAnyRole("USER", "ADMIN")
                .antMatchers("/browse").hasAnyRole("USER", "ADMIN")
                .antMatchers("/entity").hasAnyRole("USER", "ADMIN")
                .antMatchers("/list").hasAnyRole("USER", "ADMIN")
                .antMatchers("/describe").hasAnyRole("USER", "ADMIN")
                .antMatchers("/state").hasAnyRole("USER", "ADMIN")
                .antMatchers("/search").hasAnyRole("USER", "ADMIN")
                .and()
                .httpBasic();
    }
}
