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
package net.objecthunter.larch.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.IndexState;
import net.objecthunter.larch.service.IndexService;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.status.IndexStatus;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequest;
import org.elasticsearch.action.admin.indices.status.IndicesStatusResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;

public class ElasticSearchIndexService implements IndexService {
    public static final String INDEX_ENTITIES = "entities";
    public static final String INDEX_ENTITY_TYPE = "entity";
    public static final String STATE_PUBLISHED="published";
    public static final String STATE_ARCHIVED="archived";
    public static final String STATE_INGESTED="ingested";

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchIndexService.class);

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() {
        log.debug("initialising ElasticSearchIndexService");
        boolean indexExists = client.admin()
                .indices()
                .exists(new IndicesExistsRequest(INDEX_ENTITIES))
                .actionGet()
                .isExists();
        if (!indexExists) {
            client.admin()
                    .indices()
                    .create(new CreateIndexRequest(INDEX_ENTITIES))
                    .actionGet();
        }
        this.client.admin().cluster()
                .prepareHealth(INDEX_ENTITIES)
                .setWaitForYellowStatus()
                .execute()
                .actionGet();
    }
    @Override
    public void create(Entity e) throws IOException {
        log.debug("creating new entity");
        e.setVersion(1);
        e.setState("Active");
        if (e.getId() != null) {
            final GetResponse resp = client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId())
                    .execute()
                    .actionGet();
            if (resp.isExists()) {
                throw new IOException("Entity with id " + e.getId() + " already exists");
            }
        }
        final IndexResponse resp = client.prepareIndex(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId())
                .setSource(mapper.writeValueAsBytes(e))
                .execute()
                .actionGet();
    }

    @Override
    public void update(Entity e) throws IOException {
        log.debug("updating entity " + e.getId());
        /* fetch the source and copy it to another record */
        final GetResponse get = client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId())
                .execute()
                .actionGet();
        final Entity oldVersion = mapper.readValue(get.getSourceAsBytes(), Entity.class);
        final String oldVersionId = oldVersion.getId() + ".V" + oldVersion.getVersion();
        e.setVersion(oldVersion.getVersion() + 1);
        e.setState(oldVersion.getState());
        oldVersion.setState(STATE_ARCHIVED);

        IndexResponse resp = client.prepareIndex(INDEX_ENTITIES, INDEX_ENTITY_TYPE, oldVersionId)
                .setSource(mapper.writeValueAsBytes(oldVersion))
                .execute()
                .actionGet();
        log.debug("created old version " + oldVersionId);

        /* and create the updated document */
        resp = client.prepareIndex(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId())
                .setSource(mapper.writeValueAsBytes(e))
                .execute()
                .actionGet();
        /* refresh the index before returning */
        this.client.admin()
                .indices()
                .refresh(new RefreshRequest(INDEX_ENTITIES))
                .actionGet();
    }

    @Override
    public Entity retrieve(String id, int version) throws IOException {
        if (version > 0) {
            id = id + ".V" + version;
        }
        log.debug("fetching entity " + id);
        GetResponse resp = client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, id)
                .execute()
                .actionGet();
        return mapper.readValue(resp.getSourceAsBytes(), Entity.class);
    }

    @Override
    public void delete(String id) throws IOException {
        log.debug("deleting entity " + id);
        DeleteResponse resp = client.prepareDelete(INDEX_ENTITIES,INDEX_ENTITY_TYPE, id)
                .execute()
                .actionGet();
        this.client.admin().indices()
                .refresh(new RefreshRequest(("groven")))
                .actionGet();
    }

    @Override
    public IndexState status() throws IOException {
        final IndicesStatusResponse resp = client.admin().indices().status(new IndicesStatusRequest(INDEX_ENTITIES))
                .actionGet();
        final IndexStatus esState = resp.getIndices().get(INDEX_ENTITIES);

        final IndexState state = new IndexState();
        state.setName(INDEX_ENTITIES);
        state.setStoreSize(esState.getStoreSize().getBytes());
        state.setShardsSize(esState.getShards().size());
        state.setNumDocs(esState.getDocs().getNumDocs());
        state.setMaxDocs(esState.getDocs().getMaxDoc());
        state.setTotalFlushTime(esState.getFlushStats().getTotalTimeInMillis());
        state.setTotalMergeTime(esState.getMergeStats().getTotalTimeInMillis());
        state.setNumDocsToMerge(esState.getMergeStats().getCurrentNumDocs());
        state.setSizeToMerge(esState.getMergeStats().getTotalSizeInBytes());
        state.setTotalRefreshTime(esState.getRefreshStats().getTotalTimeInMillis());
        return state;
    }

    @Override
    public boolean exists(String id) throws IOException {
        return client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, id)
                .execute()
                .actionGet()
                .isExists();
    }
}
