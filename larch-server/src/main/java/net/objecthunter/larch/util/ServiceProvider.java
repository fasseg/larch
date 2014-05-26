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
package net.objecthunter.larch.util;

import org.crsh.command.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public abstract class ServiceProvider {
    private static final Logger log = LoggerFactory.getLogger(ServiceProvider.class);


    public static <T extends Object> T getService(InvocationContext ctx, Class<T> type) {
        final BeanFactory bf = (BeanFactory) ctx.getAttributes().get("spring.beanfactory");
        try {
            return bf.getBean(type);
        }catch (NoSuchBeanDefinitionException e) {
            log.error("Unable to provide bean of type {}", type.getName());
            return null;
        }
    }
}
