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
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.security.User;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.easymock.EasyMock.*;

public class ElasticSearchSearchServiceTest {
    private ElasticSearchSearchService searchService;

    private Client mockClient;


    @Before
    public void setup() {
        searchService = new ElasticSearchSearchService();
        mockClient = createMock(Client.class);
        ReflectionTestUtils.setField(searchService, "client", mockClient);
    }

    @Test
    public void testScanIndex() throws Exception {
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchHit[] hitArray = new SearchHit[1];
        SearchHit mockHit = createMock(SearchHit.class);
        hitArray[0] = mockHit;
        SearchHitField mockField = createMock(SearchHitField.class);
        SearchHits mockHits = createMock(SearchHits.class);

        expect(mockClient.prepareSearch(ElasticSearchIndexService.INDEX_ENTITIES)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setFrom(0)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSize(10)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.addFields("id", "label", "type", "tags")).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockHits).times(3);
        expect(mockHits.getHits()).andReturn(hitArray);
        expect(mockHits.getTotalHits()).andReturn((long) hitArray.length);
        expect(mockHits.iterator()).andReturn(Arrays.asList(hitArray).iterator());
        expect(mockHit.field("label")).andReturn(mockField).times(2);
        expect(mockField.getValue()).andReturn("test label");
        expect(mockHit.field("type")).andReturn(mockField).times(2);
        expect(mockField.getValue()).andReturn("test type");
        expect(mockHit.field("id")).andReturn(mockField);
        expect(mockField.getValue()).andReturn("testid");
        expect(mockHit.field("tags")).andReturn(mockField).times(2);
        expect(mockField.values()).andReturn(Arrays.asList("testtag1","testtag2"));

        replay(mockClient, mockSearchRequestBuilder, mockFuture, mockSearchResponse, mockHits, mockHit, mockField);
        SearchResult result = searchService.scanIndex(0, 10);
        verify(mockClient, mockSearchRequestBuilder, mockFuture, mockSearchResponse, mockHits, mockHit, mockField);

    }

    @Test
    public void testSearchEntities() throws Exception {

    }

    @Test
    public void testScanIndex1() throws Exception {

    }
}
