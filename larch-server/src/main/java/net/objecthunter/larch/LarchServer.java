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
package net.objecthunter.larch;

import net.objecthunter.larch.net.objecthunter.weedfs.WeedFsMaster;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class LarchServer {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .showBanner(false)
                .web(true)
                .sources(LarchServerConfiguration.class)
                .listeners(createWeedFsInitListener())
                .run();
    }

    private static ApplicationListener<ContextRefreshedEvent> createWeedFsInitListener() {
        return new ApplicationListener<ContextRefreshedEvent>() {
            @Override
            public void onApplicationEvent(ContextRefreshedEvent event) {
                final WeedFsMaster master = event.getApplicationContext().getBean(WeedFsMaster.class);
                master.runMaster();
            }
        };
    }

}
