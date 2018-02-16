/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.marmotta.ucuenca.wk.provider.dblp;

import org.apache.marmotta.commons.sesame.model.ModelCommons;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.marmotta.ucuenca.wk.commons.function.Delay;
import org.apache.marmotta.ucuenca.wk.commons.function.LDClientTools;

/**
 * Support DBLP Author data lookup
 * <p/>
 * Author: Santiago Gonzalez
 */
public class DBLPAuthorRawProvider extends AbstractHttpProvider {

    public static final String NS_AUTHOR = "http://rdf.dblp.com/ns/author/";
    public static final String NAME = "DBLP Author Raw Provider";
    public static final String PATTERN = "(http://dblp\\.org/pers/)(.*)";
    //public static final String LEGACY_PATTERN = "(http://dblp\\.(.*)\\.de/pers/)(.*)";

    public static ConcurrentMap<String, String> dblpNamespaces = new ConcurrentHashMap<String, String>();

    static {
        dblpNamespaces.put("dblp", "http://dblp.org/rdf/schema-2017-04-18#");
        dblpNamespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        dblpNamespaces.put("owl", "http://www.w3.org/2002/07/owl#");
        dblpNamespaces.put("dcterms", "http://purl.org/dc/terms/");
        dblpNamespaces.put("foaf", "http://xmlns.com/foaf/0.1");
        dblpNamespaces.put("bibtex", "http://data.bibbase.org/ontology/#");
    }

    private static Logger log = LoggerFactory.getLogger(DBLPAuthorRawProvider.class);
    
    private ClientConfiguration conf = new ClientConfiguration();
    private LDClient ldClient = new LDClient(conf);

    /**
     * Return the name of this data provider. To be used e.g. in the
     * configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[]{
            "application/rdf+xml"
        };
    }

    /**
     * Build the URL to use to call the webservice in order to retrieve the data
     * for the resource passed as argument. In many cases, this will just return
     * the URI of the resource (e.g. Linked Data), but there might be data
     * providers that use different means for accessing the data for a resource,
     * e.g. SPARQL or a Cache.
     *
     *
     * @param resource
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {
        Delay.call();
        return Collections.singletonList(resource.replaceFirst("/hd/", "/xr/").concat(".rdf"));
    }

    @Override
    public List<String> parseResponse(final String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request {0} succesful", requestUrl);
        ValueFactory factory = ValueFactoryImpl.getInstance();
        RDFFormat format = RDFFormat.forMIMEType(contentType);
        try {
            ModelCommons.add(triples, input, resource, format);
        } catch (UnsupportedRDFormatException e) {
            throw new DataRetrievalException("Error while parsing response. Unsupported format: " + contentType, e);
        } catch (RDFParseException e) {
            throw new DataRetrievalException("Error while parsing response", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing response", e);
        }
        Model publications = triples.filter(null, factory.createURI(dblpNamespaces.get("dblp") + "authorOf"), null);
        //Model type = triples.filter(null, null,factory.createURI(dblpNamespaces.get("dblp") + "Person"));
        //Resource subject = type.subjects().iterator().next();
        //triples.add(factory.createURI(resource), OWL.SAMEAS, subject);
        Set<Value> resources = publications.objects();
        if (!resources.isEmpty()) {
            Model resourceModel = null;
            for (Value dblpResource : resources) {
                String resourceDoc = ((Resource) dblpResource).stringValue();
                resourceDoc = resourceDoc.replaceFirst("rec", "rec/html");
                ClientResponse response = LDClientTools.retryLDClient(ldClient, resourceDoc, 2, 60);
                Model rsModel = response.getData();
                if (resourceModel == null) {
                    resourceModel = rsModel;
                } else {
                    resourceModel.addAll(rsModel);
                }
            }
            triples.addAll(resourceModel);
        }
        return Collections.emptyList();

    }

}
