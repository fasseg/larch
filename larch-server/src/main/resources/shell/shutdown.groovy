package shell

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

class shutdown {

    @Usage("Shutdown the repository")
    @Command
    def main(InvocationContext ctx) {
        System.exit(0);
    }
}