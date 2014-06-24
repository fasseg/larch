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
package net.objecthunter.larch.service.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.service.CredentialsService;
import net.objecthunter.larch.service.MailService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of a spring-security {@link org.springframework.security.authentication.AuthenticationManager}
 * which uses ElasticSearch indices as a persistence layer
 */
public class ElasticSearchCredentialsService extends AbstractElasticSearchService implements AuthenticationManager, CredentialsService {
    public static final String INDEX_USERS = "users";
    public static final String INDEX_GROUPS = "groups";
    public static final String INDEX_USERS_TYPE = "user";
    public static final String INDEX_GROUPS_TYPE = "group";
    public static final String INDEX_USERS_REQUEST = "user_requests";
    public static final String INDEX_USERS_REQUEST_TYPE = "user_request";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MailService mailService;

    @PostConstruct
    public void setup() throws IOException {
        this.checkAndOrCreateIndex(INDEX_USERS);
        this.checkAndOrCreateIndex(INDEX_GROUPS);
        this.checkAndOrCreateIndex(INDEX_USERS_REQUEST);
        checkAndOrCreateDefaultGroups();
        checkAndOrCreateDefaultUsers();
    }

    private void checkAndOrCreateDefaultUsers() throws IOException {
        long count = client.prepareCount(INDEX_USERS)
                .execute()
                .actionGet()
                .getCount();
        if (count == 0) {
            // create default user
            final Group g = mapper.readValue(client.prepareGet(INDEX_GROUPS, INDEX_GROUPS_TYPE, "ROLE_ADMIN")
                    .execute()
                    .actionGet()
                    .getSourceAsBytes()
                    , Group.class);
            final User admin = new User();
            admin.setPwhash(DigestUtils.sha256Hex("admin"));
            admin.setName("admin");
            admin.setFirstName("Generic");
            admin.setLastName("Superuser");
            admin.setGroups(Arrays.asList(g));
            client.prepareIndex(INDEX_USERS, "user", admin.getName())
                    .setSource(mapper.writeValueAsBytes(admin))
                    .execute()
                    .actionGet();
        }
    }

    private void checkAndOrCreateDefaultGroups() throws IOException {
        long count = client.prepareCount(INDEX_GROUPS)
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
                client.prepareIndex(INDEX_GROUPS, INDEX_GROUPS_TYPE, g.getName())
                        .setSource(mapper.writeValueAsBytes(g))
                        .execute()
                        .actionGet();
            }
        }
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        final String name = (String) auth.getPrincipal();
        final String hash = DigestUtils.sha256Hex((String) auth.getCredentials());
        final GetResponse get = client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, name)
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
                    return new UsernamePasswordAuthenticationToken(u, auth.getCredentials(), AuthorityUtils.createAuthorityList(roles));
                }
            } catch (IOException e) {
                throw new BadCredentialsException("Unable to authenticate");
            }
        }
        throw new BadCredentialsException("Unable to authenticate");
    }

    @Override
    public void createUser(User u) throws IOException {
        if (u.getName() == null) {
            throw new IOException("User name can not be null");
        }
        final GetResponse get = this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, u.getName())
                .execute()
                .actionGet();
        if (get.isExists()) {
            throw new IOException("The user " + u.getName() + " does already exist");
        }
        final IndexResponse resp = this.client.prepareIndex(INDEX_USERS, INDEX_USERS_TYPE, u.getName())
                .setSource(mapper.writeValueAsBytes(u))
                .execute()
                .actionGet();
    }

    @Override
    public void createNewUserRequest(User u) throws IOException {
        if (u.getName() == null || u.getName().isEmpty()) {
            throw new IOException("User name must be set");
        }
        if (u.getGroups() == null || u.getGroups().size() == 0) {
            throw new IOException("The user has no groups associated with it");
        }
        if (u.getEmail() == null || u.getEmail().isEmpty()) {
            throw new IOException("User's email can not be empty");
        }
        final UserRequest request = new UserRequest();
        request.setUser(u);
        request.setValidUntil(ZonedDateTime.now().plusWeeks(1));
        request.setToken(RandomStringUtils.randomAlphanumeric(128));
        this.mailService.sendUserRequest(request);
        this.client.prepareIndex(INDEX_USERS_REQUEST, INDEX_USERS_REQUEST_TYPE)
                .setSource(this.mapper.writeValueAsBytes(request))
                .execute()
                .actionGet();
    }

    @Override
    public void createGroup(Group g) throws IOException {
        if (g.getName() == null) {
            throw new IOException("Group name can not be null");
        }
        final GetResponse get = this.client.prepareGet(INDEX_GROUPS, INDEX_GROUPS_TYPE, g.getName())
                .execute()
                .actionGet();
        if (get.isExists()) {
            throw new IOException("The group " + g.getName() + " does already exist");
        }
        final IndexResponse resp = this.client.prepareIndex(INDEX_GROUPS, INDEX_GROUPS_TYPE, g.getName())
                .setSource(mapper.writeValueAsBytes(g))
                .execute()
                .actionGet();
    }

    @Override
    public void updateUser(User u) throws IOException {
        if (u.getName() == null) {
            throw new IOException("User name can not be null");
        }
        final GetResponse get = this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, u.getName())
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new IOException("The user " + u.getName() + " does not exist");
        }
        final IndexResponse resp = this.client.prepareIndex(INDEX_USERS, INDEX_USERS_TYPE, u.getName())
                .setSource(mapper.writeValueAsBytes(u))
                .execute()
                .actionGet();
    }

    @Override
    public void updateGroup(Group g) throws IOException {
        if (g.getName() == null) {
            throw new IOException("Group name can not be null");
        }
        final GetResponse get = this.client.prepareGet(INDEX_GROUPS, INDEX_GROUPS_TYPE, g.getName())
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new IOException("The group " + g.getName() + " does not exist");
        }
        final IndexResponse resp = this.client.prepareIndex(INDEX_GROUPS, INDEX_GROUPS_TYPE, g.getName())
                .setSource(mapper.writeValueAsBytes(g))
                .execute()
                .actionGet();
    }

    @Override
    public void addUserToGroup(String username, String groupname) throws IOException {
        final User u = this.retrieveUser(username);
        final Group g = this.retrieveGroup(groupname);
        if (u.getGroups() == null) {
            u.setGroups(new ArrayList<>());
        }
        for (Group existing : u.getGroups()) {
            if (existing.getName().equals(g.getName())) {
                return;
            }
        }
        u.getGroups().add(g);
        this.updateUser(u);
    }

    @Override
    public void deleteUser(String name) throws IOException {
        if (name == null) {
            throw new IOException("User name can not be null");
        }
        final GetResponse get = this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, name)
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new IOException("The user " + name + " does not exist");
        }
        final DeleteResponse resp = this.client.prepareDelete(INDEX_USERS, INDEX_USERS_TYPE, name)
                .execute()
                .actionGet();
    }

    @Override
    public void deleteGroup(String name) throws IOException {
        if (name == null) {
            throw new IOException("Group name can not be null");
        }
        if (this.isLastAdminUser(name)) {
            throw new IOException("Unable to delete last remaining Administrator");
        }
        final GetResponse get = this.client.prepareGet(INDEX_GROUPS, INDEX_GROUPS_TYPE, name)
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new IOException("The group " + name + " does not exist");
        }
        final DeleteResponse resp = this.client.prepareDelete(INDEX_GROUPS, INDEX_GROUPS_TYPE, name)
                .execute()
                .actionGet();
    }



    @Override
    public void removeUserFromGroup(String username, String groupname) throws IOException {
        final User u = this.retrieveUser(username);
        final Group g = this.retrieveGroup(groupname);
        if (u.getGroups() == null) {
            u.setGroups(new ArrayList<>());
        }
        u.getGroups().remove(g);
        this.updateUser(u);
    }

    private boolean isLastAdminUser(String name) throws IOException{
        if (!this.retrieveUser(name).getGroups().contains(this.retrieveGroup("ROLE_ADMIN"))) {
            return false;
        }
        final CountResponse resp = this.client.prepareCount(INDEX_USERS)
                .setQuery(QueryBuilders.matchQuery("groups.name", "ROLE_ADMIN"))
                .execute()
                .actionGet();
        return resp.getCount() < 2; // at least one other admin must exist
    }

    @Override
    public User retrieveUser(String name) throws IOException {
        final GetResponse get = this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, name)
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new IOException("User '" + name + "' is not valid");
        }
        return mapper.readValue(get.getSourceAsBytes(), User.class);
    }

    @Override
    public Group retrieveGroup(String name) throws IOException {
        final GetResponse get = this.client.prepareGet(INDEX_GROUPS, INDEX_GROUPS_TYPE, name)
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new IOException("Group '" + name + "' is not valid");
        }
        return mapper.readValue(get.getSourceAsBytes(), Group.class);
    }

    @Override
    public List<User> retrieveUsers() throws IOException {
        final SearchResponse resp = this.client.prepareSearch(INDEX_USERS)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute()
                .actionGet();
        final List<User> users = new ArrayList<>(resp.getHits().getHits().length);
        for (SearchHit hit: resp.getHits()) {
            users.add(mapper.readValue(hit.getSourceAsString(),User.class));
        }
        return users;
    }

    @Override
    public List<Group> retrieveGroups() throws IOException {
        final SearchResponse resp = this.client.prepareSearch(INDEX_GROUPS)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute()
                .actionGet();
        final List<Group> groups = new ArrayList<>(resp.getHits().getHits().length);
        for (SearchHit hit: resp.getHits()) {
            groups.add(mapper.readValue(hit.getSourceAsString(), Group.class));
        }
        return groups;
    }
}
