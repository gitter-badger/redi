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
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.ProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.QuotaLimitException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.semarglproject.vocab.OWL;
import org.slf4j.Logger;

/**
 * Default Implementation of {@link PubVocabService}
 *
 * @author Xavier Sumba
 */
public abstract class AbstractProviderService implements ProviderService {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private CommonsServices commonsServices;

    @Inject
    private TaskManagerService taskManagerService;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    @Inject
    private SparqlService sparqlService;

    @Inject
    private SesameService sesameService;

    /**
     * Build a list of URLs to request authors with {@link LDClient}.
     *
     * @param firstname
     * @param lastname
     * @return
     */
    protected abstract List<String> buildURLs(String firstname, String lastname);

    /**
     * Graph of provider to store the return RDF from {@link LDClient}.
     *
     * For example: http://redi.cedia.edu.ec/context/provider/DummyProvider
     *
     * @return
     */
    protected abstract String getProviderGraph();

    /**
     * Name of the provider.
     *
     * @return
     */
    protected abstract String getProviderName();

    @Override
    public void extractAuthors(String[] organizations) {
        Task task = taskManagerService.createSubTask(String.format("%s Extraction", getProviderName()), "Publication Extractor");
        task.updateMessage(String.format("Extracting publications from %s Provider", getProviderName()));
        task.updateDetailMessage("Graph", getProviderGraph());

        LDClient ldClient = new LDClient(new ClientConfiguration());
        try {
            List<Map<String, Value>> resultAllAuthors = sparqlService.query(
                    QueryLanguage.SPARQL, queriesService.getAuthorsDataQuery(organizations));

            int totalAuthors = resultAllAuthors.size();
            int processedAuthors = 0;
            task.updateTotalSteps(totalAuthors);

            for (Map<String, Value> map : resultAllAuthors) {
                // Author information.
                // Alternate, fname and lname. Fix info uploaded from file.
                String authorResource = map.get("subject").stringValue();
                String lastName = map.get("fname").stringValue().trim().toLowerCase();
                String firstName = map.get("lname").stringValue().trim().toLowerCase();

                for (String reqResource : buildURLs(firstName, lastName)) {
                    boolean existNativeAuthor = sparqlService.ask(
                            QueryLanguage.SPARQL,
                            queriesService.getAskResourceQuery(getProviderGraph(), reqResource.replace(" ", "")));
                    if (existNativeAuthor) {
                        continue;
                    }
                    try {
                        ClientResponse response = ldClient.retrieveResource(reqResource);
                        switch (response.getHttpStatus()) {
                            // Manage only HTTP 200 responses, otherwise error. Which error? Stop or continue with next resource.
                            case 200:
                                break;
                            case 401:
                                log.error("Authentication Error: check your credentials.", new AuthenticationException());
                                return;
                            case 429:
                                log.error("Requester has exceeded the quota limits associated with their API Key.", new QuotaLimitException());
                                return;
                            default:
                                log.error("Invalid request/unexpected error for '{}', skipping resource; response with HTTP {}.",
                                        reqResource, response.getHttpStatus(), new QuotaLimitException());
                                continue;
                        }
                        // store search query.
                        String oneOfQuery = buildInsertQuery(reqResource.replace(" ", ""), OWL.ONE_OF, authorResource);
                        updatePub(oneOfQuery);
                        RepositoryConnection connection = sesameService.getConnection();
                        try {
                            Model data = response.getData();
                            Resource providerContext = connection.getValueFactory().createURI(getProviderGraph());
                            connection.add(data, providerContext);
                        } finally {
                            connection.close();
                        }
                    } catch (DataRetrievalException dre) {
                        log.error("Cannot retieve RDF for the given resource: '{}'", reqResource, dre);
                    }
                }
                // Update statistics.
                processedAuthors++;
                printprogress(processedAuthors, totalAuthors, getProviderName());
                task.updateProgress(processedAuthors);
            }
        } catch (MarmottaException me) {
            log.error("Cannot query.", me);
        } catch (RepositoryException re) {
            log.error("Cannot store data retrieved.", re);
        } finally {
            taskManagerService.endTask(task);
        }
    }

    /**
     * UPDATE - with SPARQL MODULE, to load triplet in marmotta plataform
     *
     * @param querytoUpdate
     * @return
     */
    private String updatePub(String querytoUpdate) {
        try {
            sparqlFunctionsService.updatePub(querytoUpdate);
        } catch (PubException ex) {
            log.error("No se pudo insertar: " + querytoUpdate);
        }
        return "Correcto";
    }

    /**
     * Log information about the progress of extraction process.
     *
     * @param actual
     * @param total
     * @param name of the provider.
     */
    private void printprogress(int actual, int total, String name) {
        int processpercent = actual * 100 / total;
        log.info("{}: processed authors ({}) {} of {}.", name, processpercent, actual, total);
    }

    //construyendo sparql query insert 
    private String buildInsertQuery(String sujeto, String predicado, String objeto) {
        if (commonsServices.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(getProviderGraph(), sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(getProviderGraph(), sujeto, predicado, objeto);
        }
    }

}