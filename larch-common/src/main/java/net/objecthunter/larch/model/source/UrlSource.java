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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * This class is intended for passing the location information of a {@link net.objecthunter.larch.model.Binary}'s
 * actual data. If for example a {@link net.objecthunter.larch.model.Binary} is ingested a UrlSource object is used
 * to reference the actual content on disk using a {@code file://}
 */
public class UrlSource {
    private final URI uri;
    private final boolean internal;

    private UrlSource() {
        this.uri = null;
        this.internal = false;
    }

    public UrlSource(URI uri, boolean internal) {
        this.uri = uri;
        this.internal = internal;
    }

    public UrlSource(URI uri) {
        this.uri = uri;
        this.internal = false;
    }

    public UrlSource(String url) {
        this.internal = false;
        this.uri = URI.create(url);
    }

    public URI getUri() {
        return uri;
    }

    public boolean isInternal() {
        return internal;
    }

    @JsonIgnore
    public InputStream getInputStream() throws IOException {
        return uri.toURL().openStream();
    }
}
