/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DisambiguationService;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.utils.PublicationUtils;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
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

    @Inject
    private QueriesService queriesService;

    @Inject
    private CommonsServices commonsServices;

    @Override
    public void Proccess() {
        Provider a0 = new Provider("Authors", constantService.getAuthorsGraph(), sparqlService);
        Provider a1 = new Provider("Scopus", "http://rediclon.cedia.edu.ec/context/scp", sparqlService);
        Provider a2 = new Provider("MSAK", "http://rediclon.cedia.edu.ec/context/mak", sparqlService);
        Provider a3 = new Provider("DBLP", constantService.getDBLPGraph(), sparqlService);
        List<Provider> Providers = new ArrayList();
        Providers.add(a0);
        Providers.add(a1);
        Providers.add(a2);
        Providers.add(a3);

        try {
            Init(Providers);
            Init2(Providers);
            //Map(Providers);
        } catch (Exception ex) {
            Logger.getLogger(DisambiguationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Init(List<Provider> list) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException, InvalidArgumentException, UpdateExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Provider AuthorsProvider = list.get(0);
        List<Person> lsAuthors = AuthorsProvider.getAuthors();
        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < lsAuthors.size(); i++) {
            Person PersonAG = lsAuthors.get(i);
            final List<Map.Entry<Provider, List<Person>>> Candidates = new ArrayList<>();
            Candidates.add(new AbstractMap.SimpleEntry<Provider, List<Person>>(AuthorsProvider, Lists.newArrayList(PersonAG)));
            for (int j = 1; j < list.size(); j++) {
                Provider aProvider = list.get(j);
                List<Person> candidates = aProvider.getCandidates(PersonAG.URI);
                //aProvider.FillData(candidates);
                if (!candidates.isEmpty()) {
                    Candidates.add(new AbstractMap.SimpleEntry<>(aProvider, candidates));
                }
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (Map.Entry<Provider, List<Person>> aCl : Candidates) {
                            aCl.getKey().FillData(aCl.getValue());
                        }
                        Disambiguate(Candidates, 0, new Person());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
    }

    public void Disambiguate(List<Map.Entry<Provider, List<Person>>> Candidates, int level, Person P) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException, InvalidArgumentException, UpdateExecutionException {
        String CG = "http://redi.cedia.edu.ec/person/sameAs";
        if (level >= Candidates.size()) {
            return;
        }
        List<Person> get = Candidates.get(level).getValue();
        for (Person p : get) {
            if (P.check(p)) {
                Person enrich = P.enrich(p);
                registerSameAs(CG, P.URI, p.URI);
                Disambiguate(Candidates, level + 1, enrich);
            } else {
                Disambiguate(Candidates, level + 1, P);
            }
        }
    }

    public void Init2(List<Provider> list) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String CG = "http://redi.cedia.edu.ec/person/sameAs";
        String qry = " select distinct ?p { graph <" + CG + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, qry);
        for (Map<String, Value> v : query) {
            String stringValue = v.get("p").stringValue();
            disPub(list, stringValue);
        }

    }

    public void Map(List<Provider> list) throws MarmottaException {
        String CG = "http://redi.cedia.edu.ec/CG";
        String CGP = "http://redi.cedia.edu.ec/person/sameAs";
        String CGPu = "http://redi.cedia.edu.ec/publications/sameAs";
        String qry = " select distinct ?p { graph <" + CGP + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, qry);
        for (Map<String, Value> v : query) {
            String stringValue = v.get("p").stringValue();
            MapPerson(list, stringValue);

        }
    }

    public void MapPerson(List<Provider> list, String p) {
        String CGP = "http://redi.cedia.edu.ec/person/sameAs";
        String gp = "  ";
        for (Provider pro : list) {
            gp += " <" + pro.Graph + "> ";
        }
        String qry = "construct { <" + p + "> ?p ?v . "
                + "     ?v ?w ?r. } where {\n"
                + "  	graph <" + CGP + ">{\n"
                + "		<" + p + "> <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "    }\n"
                + "    values ?g { " + gp + " } graph ?g {\n"
                + "  	 ?c ?p ?v.\n"
                + "  	 filter (?p != <http://xmlns.com/foaf/0.1/publications>) .\n"
                + "      optional { ?v ?w ?r }\n"
                + "    }\n"
                + "} ";
    }

    public void disPub(List<Provider> list, String p) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String CGP = "http://redi.cedia.edu.ec/person/sameAs";
        String gp = "  ";
        for (Provider pro : list) {
            gp += " <" + pro.Graph + "> ";
        }
        String CG = "http://redi.cedia.edu.ec/publication/sameAs";
        final double th = 0.9;
        String qry = "select distinct ?p ?t {\n"
                + "  	graph <" + CGP + ">{\n"
                + "		<" + p + "> <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "    }\n"
                + "    values ?g { " + gp + " } graph ?g {\n"
                + "  	 ?c <http://xmlns.com/foaf/0.1/publications> ?p.\n"
                + "  	 ?p <http://purl.org/dc/terms/title> ?t.\n"
                + "    }\n"
                + "} ";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, qry);

        Set<String> usd = new HashSet<>();
        Set<String> all = new HashSet<>();
        for (int i = 0; i < query.size(); i++) {
            Map<String, Value> a = query.get(i);
            for (int j = i + 1; j < query.size(); j++) {
                Map<String, Value> b = query.get(j);
                String URIA = a.get("p").stringValue();
                String URIB = b.get("p").stringValue();
                all.add(URIA);
                all.add(URIB);
                if (!usd.contains(URIA) && !usd.contains(URIB)) {
                    if (PublicationUtils.compareTitle(a.get("t").stringValue(), b.get("t").stringValue()) >= th) {
                        registerSameAs(CG, URIA, URIB);
                        usd.add(URIA);
                        usd.add(URIB);
                    }
                }

            }
        }
        all.removeAll(usd);
        for (String x : all) {
            registerSameAs(CG, x, x);
        }

    }

    public void registerSameAs(String graph, String URIO, String URIP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {

        if (URIO != null && URIP != null && URIO.compareTo(URIP) != 0) {
            String buildInsertQuery = buildInsertQuery(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
            sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery);
        }
    }

    private String buildInsertQuery(String grapfhProv, String sujeto, String predicado, String objeto) {
        if (commonsServices.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(grapfhProv, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(grapfhProv, sujeto, predicado, objeto);
        }
    }

//    public void registerSameAs(Provider O, String URIO, Provider P, String URIP) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
//        String CG = "http://redi.cedia.edu.ec/tcg";
//        String askAuthor = "ask {\n"
//                + "  graph <" + CG + ">{\n"
//                + "    <" + URIO + "> ?p ?v .\n"
//                + "  }\n"
//                + "}";
//        boolean ask = sparqlService.ask(QueryLanguage.SPARQL, askAuthor);
//        MergeAuthorInfo(CG, URIO, O, URIO);
//        MergeAuthorInfo(CG, URIO, P, URIP);
//        if (ask) {
//
//        } else {
//
//        }
//    }
//
//    public void MergePublicationsInfo(String graph, String URIO, Provider P, String URIP) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
//        String CG = "http://redi.cedia.edu.ec/tcg";
//        final double thresH = 0.9;
//        String qryPub = "select ?p ?t where {\n"
//                + "  graph <GRP>{\n"
//                + "  	<URI> <http://xmlns.com/foaf/0.1/publications> ?p . \n"
//                + "    ?p <http://purl.org/dc/terms/title> ?t.\n"
//                + "  }\n"
//                + "}";
//        String qryPubA = qryPub.replaceAll("GRP", graph).replaceAll("URI", URIO);
//        String qryPubB = qryPub.replaceAll(P.Graph, graph).replaceAll("URI", URIP);
//        List<Map<String, Value>> query1 = sparqlService.query(QueryLanguage.SPARQL, qryPubA);
//        List<Map<String, Value>> query2 = sparqlService.query(QueryLanguage.SPARQL, qryPubB);
//
//        Set<String> used = new HashSet<>();
//        Set<String> all = new HashSet<>();
//
//        for (Map<String, Value> A : query1) {
//            for (Map<String, Value> B : query2) {
//                all.add(B.get("p").stringValue());
//                if (PublicationUtils.compareTitle(A.get("t").stringValue(), B.get("t").stringValue()) >= thresH) {
//                    used.add(B.get("p").stringValue());
//                    MergeAuthorPublication(CG, URIO, A.get("p").stringValue(), P, B.get("p").stringValue());
//                }
//            }
//        }
//        all.removeAll(used);
//        for (String new_ : all) {
//            MergeAuthorPublication(CG, URIO, null, P, new_);
//        }
//    }
//
//    public void MergeAuthorPublication(String graph, String URIO, String URIOP, Provider P, String URIP) throws RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
//        String qry = "construct {\n"
//                + "  <algo> ?p ?v .\n"
//                + "  ?v ?w ?e .\n"
//                + "  <algo> <http://www.w3.org/2002/07/owl#sameAs> <" + URIP + "> .\n"
//                + "\n"
//                + "} where {\n"
//                + "  graph <" + P.Graph + ">{\n"
//                + "	<" + URIP + ">  ?p ?v .\n"
//                + "    optional {\n"
//                + "    	?v ?w ?e .\n"
//                + "    }\n"
//                + "    filter (?p != <http://purl.org/dc/terms/contributor>  && ?p != <http://purl.org/dc/terms/creator> && ?p != <http://purl.org/ontology/bibo/cites>)\n"
//                + "  }\n"
//                + "} ";
//
//        if (URIOP != null) {
//            qry = qry.replaceAll("algo", URIOP);
//        } else {
//            qry = qry.replaceAll("algo", URIP);
//        }
//        update(graph, qry);
//        
//        
//        
//    }
//
//    public void MergeAuthorInfo(String graph, String URIO, Provider P, String URIP) throws RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
//        String insertNA = "construct {\n"
//                + "  <" + URIO + "> ?p ?v .\n"
//                + "  ?v ?w ?r .\n"
//                + "  <" + URIO + "> <http://www.w3.org/2002/07/owl#sameAs> <" + URIP + "> .\n"
//                + "} where {\n"
//                + "  graph <" + P.Graph + ">{\n"
//                + "    <" + URIP + "> ?p ?v .\n"
//                + "    filter (?p != <http://xmlns.com/foaf/0.1/publications>)\n"
//                + "    optional { ?v ?w ?r }\n"
//                + "  }\n"
//                + "}";
//                update(graph, insertNA);
//    }
//
//    
//    
//    
//    
//    public void update(String graph, String qry) throws RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
//        RepositoryConnection connection = sesameService.getConnection();
//        Model model = new LinkedHashModel();
//        RDFHandler createModelHandler = ModelCommons.createModelHandler(model);
//        GraphQuery prepareGraphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, qry);
//        prepareGraphQuery.evaluate(createModelHandler);
//        URI createURI = connection.getValueFactory().createURI(graph);
//        connection.add(model, createURI);
//        connection.commit();
//        connection.close();
//
//    }
}
