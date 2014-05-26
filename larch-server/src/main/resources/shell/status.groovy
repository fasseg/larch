package shell

import net.objecthunter.larch.model.Describe
import net.objecthunter.larch.service.RepositoryService
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

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
        final Describe desc = repositoryService(ctx).describe()
        StringBuilder resp = new StringBuilder();
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
        return resp.toString();
    }

    def RepositoryService repositoryService(InvocationContext ctx) {
        BeanFactory bf = ctx.attributes["spring.beanfactory"];
        return bf.getBean(RepositoryService.class);
    }
}