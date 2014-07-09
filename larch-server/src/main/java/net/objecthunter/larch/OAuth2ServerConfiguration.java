/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
public class OAuth2ServerConfiguration {

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId("larch");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                    .requestMatchers()
                    .antMatchers("/entity/**", "/metadatatype/**", "/browse/**", "/list/**", "/describe/**",
                            "/search/**", "/state/**", "/user/**", "/confirm/**", "/credentials/**", "/group/**")
                    .and()
                    .authorizeRequests()
                    .regexMatchers(HttpMethod.DELETE, "/oauth/users/([^/].*?)/tokens/.*")
                    .access("#oauth2.clientHasRole('ROLE_CLIENT') and (hasRole('ROLE_USER') or #oauth2.isClient()) and #oauth2.hasScope('write')")
                    .regexMatchers(HttpMethod.GET, "/oauth/clients/([^/].*?)/users/.*")
                    .access("#oauth2.clientHasRole('ROLE_CLIENT') and (hasRole('ROLE_USER') or #oauth2.isClient()) and #oauth2.hasScope('read')")
                    .regexMatchers(HttpMethod.GET, "/oauth/clients/.*")
                    .access("#oauth2.clientHasRole('ROLE_CLIENT') and #oauth2.isClient() and #oauth2.hasScope('read')");;
            // @formatter:on
        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends
            AuthorizationServerConfigurerAdapter {

        private TokenStore tokenStore = new InMemoryTokenStore();

        @Autowired
        @Qualifier("larchElasticSearchAuthenticationManager")
        private AuthenticationManager authenticationManager;

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
                throws Exception {
            // @formatter:off
            endpoints
                    .tokenStore(tokenStore).authenticationManager(authenticationManager);
            // @formatter:on
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            // @formatter:off
            clients
                    .inMemory().withClient("larch_user")
                    .resourceIds("larch")
                    .authorizedGrantTypes("authorization_code", "implicit")
                    .secret("secret")
                    .scopes("read", "write")
                    .autoApprove(true)
                    .redirectUris("http://localhost:8088/oauthclient/oauth?method=token")
                    .and()
                    .withClient("larch_admin")
                    .resourceIds("larch")
                    .authorizedGrantTypes("authorization_code", "implicit")
                    .secret("secret")
                    .authorities("ROLE_ADMIN")
                    .scopes("read", "write")
                    .autoApprove(true)
                    .redirectUris("http://localhost:8088/oauthclient/oauth?method=token");
            // @formatter:on
        }

    }

}
