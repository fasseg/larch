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
import net.objecthunter.larch.service.PublishService;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchPublishService extends AbstractElasticSearchService implements PublishService {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchPublishService.class);
    public static final String INDEX_PUBLISHED = "publish";
    public static final String TYPE_PUBLISHED = "publishedentity";

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        this.checkAndOrCreateIndex(INDEX_PUBLISHED);
    }

    @Override
    public void publish(Entity e) throws IOException{
        this.client.prepareIndex(INDEX_PUBLISHED, TYPE_PUBLISHED)
                .setSource(this.mapper.writeValueAsBytes(e))
                .execute()
                .actionGet();
        this.refreshIndex(INDEX_PUBLISHED);
    }

    @Override
    public Entity retrievePublishedEntity(String id) throws IOException {
        final GetResponse resp = this.client.prepareGet(INDEX_PUBLISHED, TYPE_PUBLISHED, id)
                .execute()
                .actionGet();
        if (!resp.isExists()) {
            throw new FileNotFoundException("The entity with the id " + id + " can not be found in the publish index");
        }
        return this.mapper.readValue(resp.getSourceAsBytes(), Entity.class);
    }

    @Override
    public List<Entity> retrievePublishedEntities(String entityId) throws IOException {
        final SearchResponse search = this.client.prepareSearch(INDEX_PUBLISHED)
                .setTypes(TYPE_PUBLISHED)
                .setQuery(QueryBuilders.matchQuery("id", entityId))
                .execute()
                .actionGet();
        if (search.getHits().getTotalHits() == 0) {
            throw new FileNotFoundException("There are no published versions of the entity " + entityId);
        }
        final List<Entity> result = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            result.add(this.mapper.readValue(hit.getSourceAsString(), Entity.class));
        }
        return result;
    }


}
