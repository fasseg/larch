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
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsIndices;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequestBuilder;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.shard.DocsStats;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.easymock.EasyMock.*;

public class DefaultRepositoryServiceTest {

    private DefaultRepositoryService repositoryService;
    private Client mockClient;
    private BlobstoreService mockBlobstoreService;
    private IndexService mockIndexService;
    private Environment mockEnv;
    private AdminClient mockAdminClient;
    private ClusterAdminClient mockClusterAdminClient;

    @Before
    public void setup() {
        repositoryService = new DefaultRepositoryService();
        mockClient = createMock(Client.class);
        mockBlobstoreService = createMock(BlobstoreService.class);
        mockIndexService = createMock(IndexService.class);
        mockEnv = createMock(Environment.class);
        mockAdminClient = createMock(AdminClient.class);
        mockClusterAdminClient = createMock(ClusterAdminClient.class);
        ReflectionTestUtils.setField(repositoryService, "client", mockClient);
        ReflectionTestUtils.setField(repositoryService, "blobstoreService", mockBlobstoreService);
        ReflectionTestUtils.setField(repositoryService, "indexService", mockIndexService);
        ReflectionTestUtils.setField(repositoryService, "env", mockEnv);
    }

    @Test
    public void testStatus() throws Exception {
        expect(mockIndexService.status()).andReturn(null);
        expect(mockBlobstoreService.status()).andReturn(null);

        replay(mockClient, mockIndexService, mockBlobstoreService);
        LarchState state = this.repositoryService.status();
        verify(mockClient, mockIndexService, mockBlobstoreService);
    }

    @Test
    public void testDescribe() throws Exception {
        ClusterStateRequestBuilder mockClusterStateRequest = createMock(ClusterStateRequestBuilder.class);
        ClusterStateResponse mockClusterStateResponse = createMock(ClusterStateResponse.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        ClusterState mockClusterState = createMock(ClusterState.class);
        DiscoveryNodes mockDiscoveryNodes = createMock(DiscoveryNodes.class);
        DiscoveryNode mockDiscoveryNode = createMock(DiscoveryNode.class);
        TransportAddress mockAddress = createMock(TransportAddress.class);
        ClusterStatsRequestBuilder mockClusterStatsRequest = createMock(ClusterStatsRequestBuilder.class);
        ClusterStatsResponse mockClusterStatsResponse = createMock(ClusterStatsResponse.class);
        ClusterStatsIndices mockIndicesStats = createMock(ClusterStatsIndices.class);
        DocsStats mockDocStats = createMock(DocsStats.class);

        expect(mockEnv.getProperty("larch.version")).andReturn("0.0-TEST");
        expect(mockEnv.getProperty("server.port")).andReturn("8080");
        expect(mockEnv.getProperty("larch.cluster.name")).andReturn("larch-cluster");
        expect(mockClient.admin()).andReturn(mockAdminClient).times(2);
        expect(mockAdminClient.cluster()).andReturn(mockClusterAdminClient).times(2);
        expect(mockClusterAdminClient.prepareState()).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.setBlocks(false)).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.setMetaData(true)).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.setRoutingTable(false)).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.setNodes(true)).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockClusterStateResponse);
        expect(mockClusterStateResponse.getState()).andReturn(mockClusterState).times(4);
        expect(mockClusterState.getNodes()).andReturn(mockDiscoveryNodes).times(4);
        expect(mockDiscoveryNodes.getMasterNodeId()).andReturn("master-node-1");
        expect(mockDiscoveryNodes.getDataNodes()).andReturn(ImmutableOpenMap.<String, DiscoveryNode>of());
        expect(mockDiscoveryNodes.getMasterNode()).andReturn(mockDiscoveryNode);
        expect(mockDiscoveryNode.getAddress()).andReturn(mockAddress);
        expect(mockDiscoveryNodes.getLocalNodeId()).andReturn("local-node-1");
        expect(mockClusterAdminClient.prepareClusterStats()).andReturn(mockClusterStatsRequest);
        expect(mockClusterStatsRequest.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockClusterStatsResponse);
        expect(mockClusterStatsResponse.getIndicesStats()).andReturn(mockIndicesStats);
        expect(mockIndicesStats.getDocs()).andReturn(mockDocStats);
        expect(mockDocStats.getCount()).andReturn(1l);


        replay(mockClient, mockIndexService, mockBlobstoreService, mockEnv, mockAdminClient, mockClusterAdminClient,
                mockFuture, mockClusterStateRequest, mockClusterStateResponse, mockClusterState, mockDiscoveryNodes,
                mockDiscoveryNode, mockAddress, mockClusterStatsRequest, mockClusterStatsResponse, mockIndicesStats,
                mockDocStats);
        Describe desc = this.repositoryService.describe();
        verify(mockClient, mockIndexService, mockBlobstoreService, mockEnv, mockAdminClient, mockClusterAdminClient,
                mockFuture, mockClusterStateRequest, mockClusterStateResponse, mockClusterState, mockDiscoveryNodes,
                mockDiscoveryNode, mockAddress, mockClusterStatsRequest, mockClusterStatsResponse, mockIndicesStats,
                mockDocStats);

    }
}
