/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DisambiguationService;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Provider;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author cedia
 */
@ApplicationScoped
public class DisambiguationServiceImpl implements DisambiguationService {

    @Inject
    private ConstantService constantService;

    @Inject
    private SparqlService sparqlService;

    @Inject
    private SesameService sesameService;

    @Override
    public void Proccess() {
        Provider a0 = new Provider("Authors", constantService.getAuthorsGraph(), sparqlService);
        Provider a1 = new Provider("Scopus", constantService.getScopusGraph(), sparqlService);
        Provider a2 = new Provider("MSAK", constantService.getAcademicsKnowledgeGraph(), sparqlService);
        Provider a3 = new Provider("DBLP", constantService.getDBLPGraph(), sparqlService);
        List<Provider> Providers = new ArrayList();
        Providers.add(a0);
        Providers.add(a1);
        Providers.add(a2);
        Providers.add(a3);

        try {
            Init(Providers);
        } catch (Exception ex) {
            Logger.getLogger(DisambiguationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Init(List<Provider> list) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
        Provider AuthorsProvider = list.get(0);
        List<Person> lsAuthors = AuthorsProvider.getAuthors();
        AuthorsProvider.FillData(lsAuthors);
        for (int i = 0; i < lsAuthors.size(); i++) {
            Person PersonAG = lsAuthors.get(i);
            List<List<Person>> Candidates = new ArrayList<>();
            Candidates.add(Lists.newArrayList(PersonAG));
            for (int j = 1; j < list.size(); j++) {
                Provider aProvider = list.get(j);
                List<Person> candidates = aProvider.getCandidates(PersonAG.URI);
                aProvider.FillData(candidates);
                if (!candidates.isEmpty()) {
                    Candidates.add(candidates);
                }
            }
            Disambiguate(Candidates, 0, new Person());
        }
    }

    public void Disambiguate(List<List<Person>> Candidates, int level, Person P) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
        if (level >= Candidates.size()) {
            return;
        }
        List<Person> get = Candidates.get(level);
        for (Person p : get) {
            if (P.check(p)) {
                Person enrich = P.enrich(p);
                Disambiguate(Candidates, level + 1, enrich);
                registerSameAs(P.Origin, P.URI, p.Origin, p.URI);
            } else {
                Disambiguate(Candidates, level + 1, P);
            }
        }
    }

    public void registerSameAs(Provider O, String URIO, Provider P, String URIP) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
        String CG = "http://redi.cedia.edu.ec/tcg";
        String askAuthor = "ask {\n"
                + "  graph <" + CG + ">{\n"
                + "    <" + URIO + "> ?p ?v .\n"
                + "  }\n"
                + "}";
        boolean ask = sparqlService.ask(QueryLanguage.SPARQL, askAuthor);
        MergeAuthorInfo(CG, URIO, O, URIO);
        MergeAuthorInfo(CG, URIO, P, URIP);
        if (ask) {

        } else {

        }
    }

    public void MergeAuthorInfo(String graph, String URIO, Provider P, String URIP) throws RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
        String insertNA = "construct {\n"
                + "  <" + URIO + "> ?p ?v .\n"
                + "  ?v ?w ?r .\n"
                + "  <" + URIO + "> <http://w.sameas> <" + URIP + "> .\n"
                + "} where {\n"
                + "  graph <" + P.Graph + ">{\n"
                + "    <" + URIP + "> ?p ?v .\n"
                + "    filter (?p != <http://xmlns.com/foaf/0.1/publications>)\n"
                + "    optional { ?v ?w ?r }\n"
                + "  }\n"
                + "}";
        RepositoryConnection connection = sesameService.getConnection();
        Model model = new LinkedHashModel();
        RDFHandler createModelHandler = ModelCommons.createModelHandler(model);
        GraphQuery prepareGraphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, insertNA);
        prepareGraphQuery.evaluate(createModelHandler);
        URI createURI = connection.getValueFactory().createURI(graph);
        connection.add(model, createURI);
        connection.commit();
        connection.close();
    }

    public void algo() {

    }

}
