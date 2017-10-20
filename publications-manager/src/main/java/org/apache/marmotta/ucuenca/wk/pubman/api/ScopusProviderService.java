/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

import com.google.gson.JsonArray;

public interface ScopusProviderService {

    /**
     * Get publications data from source, and load into provider graph
     *
     * @param param
     * @return
     */
    String runPublicationsProviderTaskImpl(boolean param, String[] organizations);

    /**
     * Load publications: Provider Graph to General Graph
     *
     * @param param
     * @return
     */
    String runPublicationsTaskImpl(String param);

    /**
     * Function to Search 1 Author ( param ) in DBLP, Insert in DBLP Graph and
     * Return in JSONLD
     *
     * @param url
     * @return
     */
    JsonArray SearchAuthorTaskImpl(String url);

}
