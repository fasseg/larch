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
package net.objecthunter.larch.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;

public class LarchElasticSearchAuthenticationManager implements AuthenticationManager {
    public static final String USER_INDEX = "users";
    public static final String GROUP_INDEX = "groups";

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void setup() throws IOException {
        checkAndOrCreateIndex(USER_INDEX);
        checkAndOrCreateIndex(GROUP_INDEX);
        checkAndOrCreateDefaultGroups();
        checkAndOrCreateDefaultUsers();
    }

    private void checkAndOrCreateDefaultUsers() throws IOException {
        long count = client.prepareCount(USER_INDEX)
                .execute()
                .actionGet()
                .getCount();
        if (count == 0) {
            // create default user
            final Group g = mapper.readValue(client.prepareGet(GROUP_INDEX, null, "ROLE_ADMIN")
                    .execute()
                    .actionGet()
                    .getSourceAsBytes()
                    , Group.class);
            final User admin = new User();
            admin.setName("admin");
            admin.setPwhash(DigestUtils.sha256Hex("admin"));
            admin.setGroups(Arrays.asList(g));
            client.prepareIndex(USER_INDEX, "user", admin.getName())
                    .setSource(mapper.writeValueAsBytes(admin))
                    .execute()
                    .actionGet();
        }
    }

    private void checkAndOrCreateDefaultGroups() throws IOException {
        long count = client.prepareCount(GROUP_INDEX)
                .execute()
                .actionGet()
                .getCount();
        if (count == 0) {
            // create default groups
            final Group[] groups = new Group[]{
                    new Group(),
                    new Group()
            };
            groups[0].setName("ROLE_ADMIN");
            groups[1].setName("ROLE_USER");
            for (final Group g : groups) {
                client.prepareIndex(GROUP_INDEX, "group", g.getName())
                        .setSource(mapper.writeValueAsBytes(g))
                        .execute()
                        .actionGet();
            }
        }
    }

    private void checkAndOrCreateIndex(String index) {
        final IndicesExistsResponse exists = client.admin().indices()
                .prepareExists(index)
                .execute()
                .actionGet();
        if (!exists.isExists()) {
            // create the group index
            client.admin().indices().prepareCreate(index)
                    .execute()
                    .actionGet();
        }
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        final String name = (String) auth.getPrincipal();
        final String hash = DigestUtils.sha256Hex((String) auth.getCredentials());
        final GetResponse get = client.prepareGet(USER_INDEX, "user", name)
                .execute()
                .actionGet();
        if (get.isExists()) {
            try {
                final User u = mapper.readValue(get.getSourceAsBytes(), User.class);
                if (u.getPwhash().equals(hash)) {
                    final String[] roles = new String[u.getGroups().size()];
                    for (int i = 0; i < roles.length; i++) {
                        roles[i] = u.getGroups().get(i).getName();
                    }
                    return new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), AuthorityUtils.createAuthorityList(roles));
                }
            } catch (IOException e) {
                throw new BadCredentialsException("Unable to authenticate");
            }
        }
        throw new BadCredentialsException("Unable to authenticate");
    }
}
