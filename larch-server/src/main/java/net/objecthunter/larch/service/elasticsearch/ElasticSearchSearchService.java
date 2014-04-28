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

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.SearchService;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of a {@link net.objecthunter.larch.service.SearchService} built on top of ElasticSearch
 */
public class ElasticSearchSearchService implements SearchService {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchSearchService.class);

    @Autowired
    private Client client;
    private int maxRecords = 50;

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public SearchResult scanIndex(int offset, int numRecords) {
        final long time = System.currentTimeMillis();
        numRecords = numRecords > maxRecords ? maxRecords : numRecords;
        final SearchResponse resp = this.client.prepareSearch(ElasticSearchIndexService.INDEX_ENTITIES)
                .setQuery(QueryBuilders.matchAllQuery())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(offset)
                .setSize(numRecords)
                .addFields("id", "label", "type", "tags")
                .execute()
                .actionGet();

        final SearchResult result = new SearchResult();
        result.setOffset(offset);
        result.setNumRecords(numRecords);
        result.setHits(resp.getHits().getHits().length);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setOffset(offset);
        result.setNextOffset(offset + numRecords);
        result.setPrevOffset(Math.max(offset - numRecords, 0));
        result.setMaxRecords(maxRecords);

        final List<Entity> entites = new ArrayList<>(numRecords);
        for (final SearchHit hit : resp.getHits()) {
            String label = hit.field("label") != null ? hit.field("label").getValue() : "";
            String type = hit.field("type") != null ? hit.field("type").getValue() : "";
            final Entity e = new Entity();
            e.setId(hit.field("id").getValue());
            e.setLabel(label);
            e.setType(type);
            List<String> tags = new ArrayList<>();
            if (hit.field("tags") != null) {
                for (Object o : hit.field("tags").values()) {
                    tags.add((String) o);
                }
            }
            e.setTags(tags);
            entites.add(e);
        }

        result.setData(entites);
        result.setDuration(System.currentTimeMillis() - time);
        return result;
    }

    @Override
    public SearchResult searchEntities(String terms) {
        int numRecords = 20;
        final long time = System.currentTimeMillis();
        final ActionFuture<RefreshResponse> refresh = this.client.admin().indices().refresh(new RefreshRequest(ElasticSearchIndexService.INDEX_ENTITIES));
        refresh.actionGet();

        final SearchResponse resp = this.client.prepareSearch(ElasticSearchIndexService.INDEX_ENTITIES)
                .addFields("id", "label", "type", "tags")
                .setQuery(QueryBuilders.wildcardQuery("_all", terms))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .execute()
                .actionGet();
        LOG.debug("ES returned {} results for '{}'", resp.getHits().getHits().length, terms);
        final SearchResult result = new SearchResult();

        final List<Entity> entities = new ArrayList<>();
        for (final SearchHit hit : resp.getHits()) {
            String label = hit.field("label") != null ? hit.field("label").getValue() : "";
            String type = hit.field("type") != null ? hit.field("type").getValue() : "";
            final Entity e = new Entity();
            e.setId(hit.field("id").getValue());
            e.setType(type);
            e.setLabel(label);

            final List<String> tags = new ArrayList<>();
            if (hit.field("tags") != null) {
                for (Object o : hit.field("tags").values()) {
                    tags.add((String) o);
                }
            }
            e.setTags(tags);
            entities.add(e);
        }
        result.setData(entities);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setMaxRecords(maxRecords);
        result.setHits(resp.getHits().getHits().length);
        result.setNumRecords(numRecords);
        result.setOffset(0);
        result.setTerm(terms);
        result.setPrevOffset(0);
        result.setNextOffset(0);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setDuration(System.currentTimeMillis() - time);
        return result;
    }

    @Override
    public SearchResult scanIndex(int offset) {
        return scanIndex(offset, maxRecords);
    }

}
