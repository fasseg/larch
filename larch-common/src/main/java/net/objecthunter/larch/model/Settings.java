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
package net.objecthunter.larch.model;

import net.objecthunter.larch.model.state.BlobstoreState;
import net.objecthunter.larch.model.state.IndexState;
import net.objecthunter.larch.model.state.LarchState;

public class Settings {
    private BlobstoreState blobstoreState;
    private IndexState indexState;
    private LarchState larchState;
    private Describe describe;

    public Describe getDescribe() {
        return describe;
    }

    public void setDescribe(Describe describe) {
        this.describe = describe;
    }

    public BlobstoreState getBlobstoreState() {
        return blobstoreState;
    }

    public void setBlobstoreState(BlobstoreState blobstoreState) {
        this.blobstoreState = blobstoreState;
    }

    public IndexState getIndexState() {
        return indexState;
    }

    public void setIndexState(IndexState indexState) {
        this.indexState = indexState;
    }

    public LarchState getLarchState() {
        return larchState;
    }

    public void setLarchState(LarchState larchState) {
        this.larchState = larchState;
    }
}
