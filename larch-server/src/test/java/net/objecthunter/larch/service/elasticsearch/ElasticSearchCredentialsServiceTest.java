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
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ElasticSearchCredentialsServiceTest {
    private ElasticSearchCredentialsService credentialsService;
    private Client mockClient;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        this.credentialsService = new ElasticSearchCredentialsService();
        this.mockClient = createMock(Client.class);
        ReflectionTestUtils.setField(credentialsService, "mapper", mapper);
        ReflectionTestUtils.setField(credentialsService, "client", mockClient);
    }

    @Test
    public void testAuthenticate() throws Exception {
        User u = new User();
        Group g = new Group();
        g.setName("ROLE_TEST");
        u.setGroups(Arrays.asList(g));
        u.setName("test");
        u.setPwhash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"); //sha256 hash for test

        GetResponse mockResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);

        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE,u.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockResponse);
        expect(mockResponse.isExists()).andReturn(true);
        expect(mockResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(u));

        replay(mockClient, mockFuture, mockGetRequestBuilder, mockResponse);
        Authentication auth = this.credentialsService.authenticate(new UsernamePasswordAuthenticationToken("test",
                "test"));
        verify(mockClient, mockFuture, mockGetRequestBuilder, mockResponse);

        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertEquals("test", ((User)auth.getPrincipal()).getName());
    }

    @Test
    public void testCreateUser() throws Exception {

    }

    @Test
    public void testCreateGroup() throws Exception {

    }

    @Test
    public void testUpdateUser() throws Exception {

    }

    @Test
    public void testUpdateGroup() throws Exception {

    }

    @Test
    public void testAddUserToGroup() throws Exception {

    }

    @Test
    public void testDeleteUser() throws Exception {

    }

    @Test
    public void testDeleteGroup() throws Exception {

    }

    @Test
    public void testRemoveUserFromGroup() throws Exception {

    }

    @Test
    public void testRetrieveUser() throws Exception {

    }

    @Test
    public void testRetrieveGroup() throws Exception {

    }

    @Test
    public void testRetrieveUsers() throws Exception {

    }

    @Test
    public void testRetrieveGroups() throws Exception {

    }
}
