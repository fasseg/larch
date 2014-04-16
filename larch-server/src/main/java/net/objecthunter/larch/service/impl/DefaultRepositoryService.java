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

import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.state.LarchState;
import net.objecthunter.larch.service.BlobstoreService;
import net.objecthunter.larch.service.IndexService;
import net.objecthunter.larch.service.RepositoryService;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;

import java.io.IOException;

public class DefaultRepositoryService implements RepositoryService {
    @Autowired
    private IndexService indexService;

    @Autowired
    private BlobstoreService blobstoreService;

    @Autowired
    private Environment env;

    @Autowired
    private Client client;

    @Override
    @Secured("ROLE_ADMIN")
    public LarchState status() throws IOException {
        final LarchState state = new LarchState();
        state.setBlobstoreState(blobstoreService.status());
        state.setIndexState(indexService.status());
        return state;
    }

    @Override
    public Describe describe() {
        final Describe desc = new Describe();
        desc.setLarchVersion(env.getProperty("larch.version"));
        desc.setLarchHost("localhost:" + env.getProperty("server.port"));
        desc.setLarchClusterName(env.getProperty("larch.cluster.name"));
        final ClusterStateResponse state = client.admin().cluster().prepareState()
                .setBlocks(false)
                .setMetaData(true)
                .setRoutingTable(false)
                .setNodes(true)
                .execute()
                .actionGet();
        desc.setEsMasterNodeName(state.getState().getNodes().getMasterNodeId());
        desc.setEsNumDataNodes(state.getState().getNodes().getDataNodes().size());
        desc.setEsMasterNodeAddress(state.getState().getNodes().getMasterNode().address().toString());
        desc.setEsNodeName(state.getState().getNodes().getLocalNodeId());
        final ClusterStatsResponse stats = client.admin().cluster().prepareClusterStats()
                .execute()
                .actionGet();
        desc.setEsNumIndexedRecords(stats.getIndicesStats().getDocs().getCount());
        return desc;
    }
}
