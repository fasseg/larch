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
package net.objecthunter.larch.model.source;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.objecthunter.larch.model.state.FilesystemBlobstoreState;
import net.objecthunter.larch.model.state.WeedFsBlobstoreState;

import java.io.IOException;
import java.io.InputStream;

@JsonSubTypes({
        @JsonSubTypes.Type(value = UrlSource.class, name = "url-source"),
        @JsonSubTypes.Type(value = StreamSource.class, name = "stream-source")
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface Source {
    public InputStream getInputStream() throws IOException;
    public boolean isInternal();
}
