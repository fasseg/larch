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
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.test.util.Fixtures;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.easymock.EasyMock.*;

public class ElasticSearchIndexServiceTest {
    private ElasticSearchIndexService indexService;
    private Client mockClient;
    private AdminClient mockAdminClient;
    private IndicesAdminClient mockIndicesAdminClient;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        mockClient = createMock(Client.class);
        mockAdminClient = createMock(AdminClient.class);
        mockIndicesAdminClient = createMock(IndicesAdminClient.class);
        indexService = new ElasticSearchIndexService();
        ReflectionTestUtils.setField(indexService, "mapper", mapper);
        ReflectionTestUtils.setField(indexService, "client", mockClient);
    }

    @Test
    public void testCreate() throws Exception {
        Entity e = Fixtures.createEntity();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchIndexService.INDEX_ENTITIES,
                ElasticSearchIndexService.INDEX_ENTITY_TYPE, e.getId())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(false);

        /* user indexing */
        expect(mockClient.prepareIndex(ElasticSearchIndexService.INDEX_ENTITIES,
                ElasticSearchIndexService.INDEX_ENTITY_TYPE,
                e.getId())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture, mockGetResponse,
                mockIndexRequestBuilder);
        this.indexService.create(e);
        verify(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture, mockGetResponse,
                mockIndexRequestBuilder);
    }

    @Test
    public void testUpdate() throws Exception {
        Entity e = Fixtures.createEntity();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* indexing */
        expect(mockClient.prepareIndex(ElasticSearchIndexService.INDEX_ENTITIES,
                ElasticSearchIndexService.INDEX_ENTITY_TYPE,
                e.getId())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture, mockGetResponse,
                mockIndexRequestBuilder);
        this.indexService.update(e);
        verify(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture, mockGetResponse,
                mockIndexRequestBuilder);

    }

    @Test
    public void testRetrieve() throws Exception {
        Entity e = Fixtures.createEntity();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        SearchHit[] hitArray = new SearchHit[0];
        SearchHits mockHits = createMock(SearchHits.class);

        /* retrieve */
        expect(mockClient.prepareGet(ElasticSearchIndexService.INDEX_ENTITIES,
                ElasticSearchIndexService.INDEX_ENTITY_TYPE, e.getId())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(e));

        /* retrieve children */
        expect(mockClient.prepareSearch(ElasticSearchIndexService.INDEX_ENTITIES)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setTypes(ElasticSearchIndexService.INDEX_ENTITY_TYPE)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setFrom(0)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSize(anyInt())).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockHits);
        expect(mockHits.getHits()).andReturn(hitArray);
        expect(mockSearchResponse.getHits()).andReturn(mockHits);
        expect(mockHits.getTotalHits()).andReturn(0l);

        replay(mockClient,mockHits, mockSearchRequestBuilder, mockSearchResponse, mockGetResponse,
                mockGetRequestBuilder,
                mockFuture);
        this.indexService.retrieve(e.getId());
        verify(mockClient,mockHits, mockSearchRequestBuilder, mockSearchResponse,  mockGetRequestBuilder,
                mockGetResponse,
                mockFuture);
    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testStatus() throws Exception {

    }

    @Test
    public void testExists() throws Exception {

    }
}
