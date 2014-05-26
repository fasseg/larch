package shell

import net.objecthunter.larch.model.Describe
import net.objecthunter.larch.model.state.LarchState
import net.objecthunter.larch.service.RepositoryService
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

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

class status {

    @Usage("Repository status information")
    @Command
    def main(InvocationContext ctx) {
        final RepositoryService repositoryService = (RepositoryService) ServiceProvider.getService(ctx,
                RepositoryService.class);
        final Describe desc = repositoryService.describe();
        final LarchState state = repositoryService.status();
        final StringBuilder resp = new StringBuilder();
        resp.append("\n:::: Module Larch ::::\n\n")
                .append("Version\t\t\t\t")
                .append(desc.larchVersion)
                .append('\n')
                .append("Cluster name\t\t\t")
                .append(desc.larchClusterName)
                .append('\n')
                .append("Host name\t\t\t")
                .append(desc.larchHost)
                .append('\n\n')
                .append(":::: Module ElasticSearch ::::\n\n")
                .append("Version\t\t\t\t")
                .append(desc.esVersion)
                .append('\n')
                .append("Master node name\t\t")
                .append(desc.esMasterNodeName)
                .append('\n')
                .append("Master node address\t\t")
                .append(desc.esMasterNodeAddress)
                .append('\n')
                .append("Node name\t\t\t")
                .append(desc.esNodeName)
                .append('\n')
                .append("Number of nodes\t\t\t")
                .append(desc.esNumDataNodes)
                .append('\n')
                .append("Number of indexed records\t")
                .append(desc.esNumIndexedRecords)
                .append('\n')
                .append("Index name\t\t\t")
                .append(state.indexState.name)
                .append('\n')
                .append("Number of docs\t\t\t")
                .append(state.indexState.numDocs)
                .append('\n')
                .append("Max number of docs\t\t")
                .append(state.indexState.maxDocs)
                .append('\n')
                .append("Number of docs to merge\t\t")
                .append(state.indexState.numDocsToMerge)
                .append('\n')
                .append("Store size\t\t\t")
                .append(state.indexState.storeSize)
                .append('\n')
                .append("Shards size\t\t\t")
                .append(state.indexState.shardsSize)
                .append('\n')
                .append("Size to merge\t\t\t")
                .append(state.indexState.sizeToMerge)
                .append('\n')
                .append("Total flush time\t\t")
                .append(state.indexState.totalFlushTime)
                .append('\n')
                .append("Total refresh time\t\t")
                .append(state.indexState.totalRefreshTime)
                .append('\n\n')
                .append(":::: Module Blobstore ::::\n\n")
                .append("Name\t\t\t\t")
                .append(state.blobstoreState.name)
                .append('\n');
        return resp.toString();
    }
}