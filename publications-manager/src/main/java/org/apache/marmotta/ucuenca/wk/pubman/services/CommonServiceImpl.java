/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.apache.marmotta.ucuenca.wk.pubman.api.ReportsService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.AcademicsKnowledgeProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.DBLPProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.DspaceProviderServiceImpl;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.GoogleScholarProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.ScopusProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.SpringerProviderService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.json.JSONArray;
import org.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map.Entry;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.slf4j.Logger;

/**
 *
 * @author Satellite
 *
 */
public class CommonServiceImpl implements CommonService {

    @Inject
    private ConstantService con;

    @Inject
    private Logger log;

    @Inject
    GoogleScholarProviderService googleProviderService;

    @Inject
    SpringerProviderService springerProviderService;

    @Inject
    AcademicsKnowledgeProviderService academicsKnowledgeService;

    @Inject
    DBLPProviderService dblpProviderServiceInt;

    @Inject
    DspaceProviderServiceImpl dspaceProviderService;

    @Inject
    ReportsService reportService;

    @Inject
    ReportsImpl reportsImpl;

    @Inject
    LantindexDetectionServiceImpl LatindexImpl;

    @Inject
    DisambiguationServiceImpl DisambiguationImpl;

    @Inject
    private QueriesService queriesService;

    @Inject
    private SparqlService sparqlService;

    @Inject
    private CommonsServices com;

    @Inject
    private org.apache.marmotta.ucuenca.wk.pubman.services.providers.DBLPProviderService providerServiceDblp1;

    @Inject
    private org.apache.marmotta.ucuenca.wk.pubman.services.providers.ScieloProviderService providerServiceScielo;

    @Inject
    private ScopusProviderService providerServiceScopus1;
    private Thread scopusThread;
    private Thread academicsThread;
    private Thread dblpThread;
    private Thread scieloThread;
    private Thread scholarThread;
    private Thread springerThread;

    @Override
    public String getDataFromSpringerProvidersService(final String[] organizations) {
        if (springerThread != null && springerThread.isAlive()) {
            return "Process is executing.";
        }

        springerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                springerProviderService.extractAuthors(organizations);
            }
        });
        springerThread.start();
        return "Data Provider Springer are extracted in background.   Please review main.log file for details";

    }

    @Override
    public String getDataFromScopusProvidersService(final String[] organizations) {
        // Find a way to execute thread and get response information.
        if (scopusThread != null && scopusThread.isAlive()) {
            return "Process is executing.";
        }

        scopusThread = new Thread(new Runnable() {
            @Override
            public void run() {
                providerServiceScopus1.extractAuthors(organizations);
            }
        });
        scopusThread.start();
        return "Data Provider SCOPUS are extracted in background.   Please review main.log file for details";

    }

    @Override
    public String getDataFromAcademicsKnowledgeProvidersService(final String[] organizations) {
        if (academicsThread != null && academicsThread.isAlive()) {
            return "Process is executing.";
        }
        academicsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                academicsKnowledgeService.extractAuthors(organizations);
            }
        });
        academicsThread.start();
        return "Data Provider AK are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String getDataFromDBLPProvidersService(final String[] organizations) {
        if (dblpThread != null && dblpThread.isAlive()) {
            return "Process is executing.";
        }
        dblpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                providerServiceDblp1.extractAuthors(organizations);
            }
        });
        dblpThread.start();
        return "Data Provider DBLP are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String getDataFromGoogleScholarProvidersService(final String[] organizations) {

        if (scholarThread != null && scholarThread.isAlive()) {
            return "Process is executing.";
        }
        scholarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                googleProviderService.extractAuthors(organizations);
            }
        });
        scholarThread.start();
        return "Data Provider Google Scholar are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String GetDataFromProvidersServiceDspace() {
        Thread DspaceProvider = new Thread(dspaceProviderService);
        DspaceProvider.start();
        return "Data Provider Dspace are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String createReport(String hostname, String realPath, String name, String type, List<String> params) {
        return reportService.createReport(hostname, realPath, name, type, params);
    }

    @Override
    public String getSearchQuery(String textSearch) {
        return queriesService.getSearchQuery(textSearch);
    }

    @Override
    public String DetectLatindexPublications() {
        String startProcess = LatindexImpl.startProcess();

        return startProcess;
    }

    @Override
    public String organizationListExtracted() {
        try {
            List<Provider> prov = getProviders();
            Map<String, String> mprov = new HashMap();
            for (Provider p : prov) {

                mprov.put(p.Graph, p.Name);

            }
            String queryOrg = queriesService.getExtractedOrgList(mprov);
            List<Map<String, Value>> response;

            response = sparqlService.query(QueryLanguage.SPARQL, queryOrg);
            return com.listmapTojson(response);
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public String organizationListEnrichment() {
        try {
            List<Provider> prov = getProviders();
            Map<String, String> mprov = new HashMap();
            for (Provider p : prov) {

                mprov.put(p.Graph, p.Name);

            }
            String queryOrg = queriesService.getOrgEnrichmentProvider(mprov);
            List<Map<String, Value>> response1;

            String queryd = queriesService.getOrgDisambiguationResult(mprov);
            log.info(queryd);
            List<Map<String, Value>> response2;

            response1 = sparqlService.query(QueryLanguage.SPARQL, queryOrg);
            response2 = sparqlService.query(QueryLanguage.SPARQL, queryd);
            log.info("RESP");
            log.info(response2.toString());
            for (Map<String, Value> m1 : response1) {
                for (Map<String, Value> m2 : response2) {
                    if (m1.containsKey("org") && m2.containsKey("org")) {
                        if (m1.get("org").equals(m2.get("org"))) {
                            //m1.putAll(m2);
                            // String log;
                            for (Map.Entry<String, Value> e2 : m2.entrySet()) {
                                m1.put(e2.getKey() + "l", e2.getValue());
                            }
                        }
                    }
                }
            }

            return com.listmapTojson(response1);
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public List<Provider> getProviders() throws MarmottaException {
        List<Provider> Providers = new ArrayList();
        String queryProviders = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT DISTINCT ?uri ?name ?graph ?main WHERE { "
                + "  GRAPH ?graph { "
                + "  ?uri a <" + REDI.PROVIDER.toString() + "> . "
                + "  ?uri rdfs:label ?name  ."
                + "  ?uri <http://ucuenca.edu.ec/ontology#main> ?main"
                + "  }}order  by desc (?main)";

        List<Map<String, Value>> response = sparqlService.query(QueryLanguage.SPARQL, queryProviders);
        for (Map<String, Value> prov : response) {
            Provider p = new Provider(prov.get("name").stringValue().replace(" ", ""), prov.get("graph").stringValue(), sparqlService);
            Providers.add(p);
        }
        return Providers;
    }

    @Override
    public String runDisambiguationProcess(String[] orgs) {
        String startProcess = DisambiguationImpl.startDisambiguation(orgs);
        return startProcess;
    }

    @Override
    public String CentralGraphProcess() {
        String startProcess = DisambiguationImpl.startMerge();
        return startProcess;
    }

    @Override
    public String getDataFromScieloProvidersService(final String[] organizations) {
        if (scieloThread != null && scieloThread.isAlive()) {
            return "Process is executing.";
        }
        scieloThread = new Thread(new Runnable() {
            @Override
            public void run() {
                providerServiceScielo.extractAuthors(organizations);
            }
        });
        scieloThread.start();
        return "Data Provider DBLP are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String runDisambiguationProcess() {
        String startProcess = DisambiguationImpl.startDisambiguation();
        return startProcess;
    }

  
    
    @Override
    public String getCollaboratorsData(String uri) {
        String describeAuthor = "PREFIX dct: <http://purl.org/dc/terms/> "
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT DISTINCT  ?author (SAMPLE (?names) as ?name) (SAMPLE (?lastname) as ?lname)  ?subject  (SAMPLE (?slabel) as ?label) (COUNT (DISTINCT  ?pub) as ?npub) ?orgname ?img WHERE {   "
                + "  GRAPH  <" + con.getCentralGraph() + "> {  "
                + "    VALUES ?author {<" + uri + "> } . "
                + "    ?author foaf:name ?names . "
                + "    OPTIONAL { ?author  foaf:familyName ?lastname .} "
                + "    ?author foaf:publications ?pub .   "
                + "    OPTIONAL { ?author foaf:img ?img . }"
                + "    ?pub dct:subject ?subject . "
                + "    ?subject rdfs:label ?slabel . "
                + "    ?author  <http://schema.org/memberOf>  ?org . "
                + "   GRAPH <" + con.getOrganizationsGraph() + "> { "
                + "     ?org <" + REDI.NAME.toString() + "> ?orgname "
                + "   }  "
                + "   }  "
                + "}GROUP BY ?author ?subject ?orgname ?img HAVING ( ?npub > 1  )  ORDER BY DESC (?npub)";
        try {
            List<Map<String, Value>> response = sparqlService.query(QueryLanguage.SPARQL, describeAuthor);
            String imgbase = "";
            String authorLName = "";
            if (!response.isEmpty()) {
                String authorName = response.get(0).get("name").stringValue();
                if (response.get(0).containsKey("lname")) {
                    authorLName = response.get(0).get("lname").stringValue();
                }
                String authorOrg = response.get(0).get("orgname").stringValue();
                if (response.get(0).containsKey("img")) {
                    imgbase = response.get(0).get("img").stringValue();
                }

                double maxScoreAuthor = 0;
                double maXScoreCoauthor = 0;
                String authorKeywords = "";

                LinkedHashMap<String, collaborator> cMap = new LinkedHashMap();

                for (Map<String, Value> author : response) {
                    String subject = author.get("subject").stringValue();
                    if (isStopWord(author.get("label").stringValue())) {
                        continue;
                    }
                    if (maxScoreAuthor == 0) {
                        maxScoreAuthor = Integer.parseInt(author.get("npub").stringValue());
                    }

                    authorKeywords = author.get("label").stringValue() + "," + authorKeywords;
                    double authorSubjectS = (double) Integer.parseInt(author.get("npub").stringValue());

                    String querySubjectCo = "PREFIX dct: <http://purl.org/dc/terms/> "
                            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "SELECT DISTINCT  ?author (SAMPLE (?names) as ?name)  (SAMPLE (?lastname) as ?lname) (SAMPLE (?slabel) as ?label) (COUNT (DISTINCT  ?pub) as ?npub)  ?orgname  ?img "
                            + "WHERE {   "
                            + "  GRAPH  <" + con.getCentralGraph() + "> {      "
                            + "    VALUES ?subject { <" + subject + "> } .\n"
                            + "    ?pub dct:subject ?subject . "
                            + "    ?subject rdfs:label ?slabel . "
                            + "    ?author foaf:publications ?pub  .   "
                            + "    ?author foaf:name ?names . "
                            + "OPTIONAL { ?author  foaf:familyName ?lastname .}"
                            + "OPTIONAL { ?author  foaf:img  ?img . } "
                            + "    ?author  <http://schema.org/memberOf>  ?org . "
                            + "   GRAPH <" + con.getOrganizationsGraph() + "> { "
                            + "     ?org <" + REDI.NAME.toString() + "> ?orgname . "
                            + "   } . "
                            + "   filter ( ?author !=  <" + uri + "> ) "
                            + "   }  "
                            + "}GROUP BY ?author   ?orgname  ?img  HAVING ( ?npub > 1  )  ORDER BY DESC (?npub)";

                    List<Map<String, Value>> responseSubAuthor = sparqlService.query(QueryLanguage.SPARQL, querySubjectCo);

                    for (Map<String, Value> coauthor : responseSubAuthor) {
                        String couri = coauthor.get("author").stringValue();
                        String coName = coauthor.get("name").stringValue();
                        String lName = "";
                        if (coauthor.containsKey("lname")) {
                            lName = coauthor.get("lname").stringValue();
                        }
                        String coOrg = coauthor.get("orgname").stringValue();
                        int coScore = Integer.parseInt(coauthor.get("npub").stringValue());
                        String imgUri = "";
                        if (coauthor.containsKey("img")) {
                            imgUri = coauthor.get("img").stringValue();
                        }
                        double Score = authorSubjectS / (double) maxScoreAuthor;

                        if (!cMap.containsKey(couri)) {
                            collaborator c = new collaborator(uri, couri, coName, lName, Score * coScore, isCoauthor(uri, couri), isClusterPartner(uri, couri), coOrg, getSubjectAuthor(couri));
                            c.setImgUri(imgUri);
                            cMap.put(couri, c);
                            if (maXScoreCoauthor < c.calcScore()) {
                                maXScoreCoauthor = c.calcScore();
                            }
                        }

                    }

                }

                collaborator base = new collaborator(uri, uri, authorName, authorLName, authorOrg, getSubjectAuthor(uri));
                base.setImgUri(imgbase);
                return coauthorsToJson(base, orderCoauthors(cMap, 15), maXScoreCoauthor);

            }
        } catch (MarmottaException | org.json.JSONException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            log.debug(ex.getMessage());
        }
        log.debug("No data found for related authors: " + uri);
        return "{\"Error\":\"No Data\"}";
    }

    public String getSubjectAuthor(String coUri) throws MarmottaException {
        String querySubject = "PREFIX dct: <http://purl.org/dc/terms/> "
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
                + "SELECT ?author (GROUP_CONCAT (DISTINCT ?lsubject ;separator= \", \") as ?lsubjects) WHERE { "
                + "SELECT ?subject  (SAMPLE ( ?slabel ) as ?lsubject)  (COUNT (?pub) as ?npub)    { "
                + " VALUES ?author { <" + coUri + "> } .  \n"
                + "   ?author     foaf:publications ?pub . "
                + "?pub dct:subject ?subject . "
                + "?subject rdfs:label ?slabel } GROUP BY ?subject ORDER BY DESC (?npub) limit 10 "
                + " } GROUP BY ?author";

        List<Map<String, Value>> response = sparqlService.query(QueryLanguage.SPARQL, querySubject);
        if (!response.isEmpty()) {
            return response.get(0).get("lsubjects").stringValue();
        } else {
            return "";
        }
    }

    public String coauthorsToJson(collaborator base, LinkedHashMap<String, collaborator> cMap, Double maxScore) throws org.json.JSONException {
        List<Map<String, String>> lnodes = new ArrayList();

        Map newm = new HashMap();
        newm.put("id", base.getUri());
        newm.put("group", base.getOrganization());
        newm.put("label", base.getcName());
        newm.put("img", base.getImgUri());
        newm.put("lastname", base.getLastName());
        newm.put("subject", base.getSubjects());
        lnodes.add(newm);

        List<Map<String, String>> llinks = new ArrayList();
        for (Map.Entry<String, collaborator> e : cMap.entrySet()) {
            newm = new HashMap();
            newm.put("id", e.getValue().getUri());
            newm.put("group", e.getValue().getOrganization());
            newm.put("label", e.getValue().getcName());
            newm.put("lastname", e.getValue().getLastName());
            newm.put("img", e.getValue().getImgUri());
            newm.put("subject", e.getValue().getSubjects());
            lnodes.add(newm);

            Map newlink = new HashMap();
            newlink.put("source", base.getUri());
            newlink.put("target", e.getValue().getUri());
            newlink.put("distance", 300 - (300 * e.getValue().calcScore()) / maxScore + "");
            newlink.put("coauthor", e.getValue().isCoauthor().toString());
            llinks.add(newlink);
        }

        return mergeJSON(listmapTojson(lnodes, "nodes"), listmapTojson(llinks, "links"), "nodes", "links");

    }

    @Deprecated
    public String getCollaboratorsData(String uri, String nada) {

        try {

            String querySubrel = "PREFIX dct: <http://purl.org/dc/terms/> "
                    + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
                    + "SELECT  (COUNT (DISTINCT  ?pub) as ?sc) (COUNT (DISTINCT  ?opub) as ?nc) ?s ?co  ?orgname (SAMPLE(?names) as ?name)  "
                    + " WHERE {  "
                    + "  GRAPH  <" + con.getCentralGraph().replace("http:", "https:") + "> { "
                    + "  VALUES ?author {<" + uri + "> } . "
                    + "  ?author foaf:publications ?pub . "
                    + "  ?pub dct:subject ?s . "
                    + "  ?opub dct:subject ?s . "
                    + "  ?co  foaf:publications ?opub  . "
                    + " filter ( ?co !=  ?author ) .  "
                    + "  ?co foaf:name ?names . "
                    + "  ?co  <http://schema.org/memberOf>  ?org . "
                    + "   GRAPH <https://redi.cedia.edu.ec/context/organization> { "
                    + "     ?org <http://ucuenca.edu.ec/ontology#name> ?orgname "
                    + "   } "
                    + "} } GROUP BY ?s ?co ?clo ?orgname HAVING ( ?nc > 1  && ?sc > 1 ) ORDER BY DESC (?sc) DESC(?nc)";

            List<Map<String, Value>> response = sparqlService.query(QueryLanguage.SPARQL, querySubrel);
            int maxvalue = 0;
            Double maxScore = 0.0;
            if (response.size() > 0) {
                maxvalue = Integer.parseInt(response.get(0).get("sc").stringValue());
                LinkedHashMap<String, collaborator> cMap = new LinkedHashMap();
                for (Map<String, Value> author : response) {
                    int sc = Integer.parseInt(author.get("sc").stringValue());
                    int nc = Integer.parseInt(author.get("nc").stringValue());
                    String org = author.get("orgname").stringValue();
                    String co = author.get("co").stringValue();
                    //  author.get("s").stringValue();
                    //  author.get("co").stringValue();
                    double Score = sc / maxvalue;

                    //c.setSubjectScore((int) (Score * nc));
                    if (!cMap.containsKey(co)) {
                        collaborator c = new collaborator(uri, co, author.get("name").stringValue(), "", Score * nc, isCoauthor(uri, co), isClusterPartner(uri, co), org, "");
                        cMap.put(co, c);
                        if (maxScore < c.calcScore()) {
                            maxScore = c.calcScore();
                        }
                    }
                }
                List<Map<String, String>> lnodes = new ArrayList();

                Map newm = new HashMap();
                newm.put("id", uri);
                newm.put("group", "2");
                lnodes.add(newm);

                List<Map<String, String>> llinks = new ArrayList();
                for (Map.Entry<String, collaborator> e : cMap.entrySet()) {
                    newm = new HashMap();
                    newm.put("id", e.getValue().getUri());
                    newm.put("group", e.getValue().getOrganization());
                    newm.put("org", e.getValue().getOrganization());
                    newm.put("label", e.getValue().getcName());
                    lnodes.add(newm);

                    Map newlink = new HashMap();
                    newlink.put("source", uri);
                    newlink.put("target", e.getValue().getUri());
                    newlink.put("distance", 300 - (300 * e.getValue().calcScore()) / maxScore + "");
                    newlink.put("coauthor", e.getValue().isCoauthor().toString());
                    llinks.add(newlink);
                    // newm.put(e, e)
                }

                //    listmapTojson (lnodes , "nodes");
                //    listmapTojson (llinks , "links");
                //  Provider p = new Provider(prov.get("name").stringValue().replace(" ", ""), prov.get("graph").stringValue(), sparqlService , Boolean.parseBoolean(prov.get("main").stringValue()));
                // Providers.add(p);
                // return listmapTojson (lnodes , "nodes").toString().replace("{", uri)+","+listmapTojson (llinks , "links").toString().replace("", "");
                return mergeJSON(listmapTojson(lnodes, "nodes"), listmapTojson(llinks, "links"), "nodes", "links");
                //  mergeJSON (listmapTojson (lnodes , "nodes") ,listmapTojson (llinks , "links") );
            }
            return "";
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        } catch (org.json.JSONException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Boolean isCoauthor(String uri, String candidate) throws MarmottaException {
        String queryisCoauthor = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "ASK  FROM <" + con.getCentralGraph() + ">   { "
                + "  <" + uri + ">  foaf:publications ?pub  . "
                + "  <" + candidate + ">   foaf:publications ?pub   "
                + "}";
        return sparqlService.ask(QueryLanguage.SPARQL, queryisCoauthor);
    }

    public Boolean isClusterPartner(String uri, String candidate) throws MarmottaException {
        String queryC = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "ASK  FROM <" + con.getClusterGraph() + ">    {\n"
                + "   ?p  <http://ucuenca.edu.ec/ontology#hasPerson> <" + uri + ">  .\n"
                + "   ?c foaf:publications ?p . "
                + "   ?c foaf:publications ?op . "
                + "  ?op <http://ucuenca.edu.ec/ontology#hasPerson> <" + candidate + ">   \n"
                + "}";

        return sparqlService.ask(QueryLanguage.SPARQL, queryC);
    }

  
    public JSONArray listmapTojson(List<Map<String, String>> list, String nameobject) throws org.json.JSONException {
        JSONObject jsonh1 = new JSONObject();

        JSONArray jsonArr = new JSONArray();
        for (Map<String, String> map : list) {
            JSONObject jsonObj = new JSONObject();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                jsonObj.put(key, value);
            }
            jsonArr.put(jsonObj);
        }

        //return jsonArr.toString();
        return jsonArr;
        //return jsonh1.put(nameobject, jsonArr);
    }

    public String mergeJSON(JSONArray j1, JSONArray j2, String name1, String name2) throws org.json.JSONException {
        JSONObject jsonGlobal = new JSONObject();
        jsonGlobal.put(name1, j1);
        jsonGlobal.put(name2, j2);
        return jsonGlobal.toString();
    }

    public Boolean isStopWord(String keyword) {
        LinkedList l = new LinkedList();
        l.add("ecuador");
        l.add("venezuela");
        l.add("south america");
        l.add("latin america");

        return l.contains(keyword.toLowerCase());
    }

    private LinkedHashMap<String, collaborator> orderCoauthors(LinkedHashMap<String, collaborator> cMap, int number) {

        List<Map.Entry<String, collaborator>> list = new LinkedList<>(cMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, collaborator>>() {
            @Override
            public int compare(Map.Entry<String, collaborator> o1, Map.Entry<String, collaborator> o2) {
                Double result = o2.getValue().calcScore() - o1.getValue().calcScore();
                return result.intValue();
            }
        });

        LinkedHashMap<String, collaborator> result = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<String, collaborator> entry : list) {
            result.put(entry.getKey(), entry.getValue());
            if (count < number) {
                count++;
            } else {
                break;
            }

        }

        return result;

    }

    @Override
    public String getAuthorDataProfile(String uri) {

        String Prefix = "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix vcard: <http://www.w3.org/2006/vcard/ns#> "
                + "prefix scoro: <http://purl.org/spar/scoro/> "
                + "prefix schema: <http://schema.org/> "
                + "prefix uc: <http://ucuenca.edu.ec/ontology#> ";

       // uri = "https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_SAQUICELA_GALARZA_____VICTOR_HUGO_";
        try {

            String numpubquery = Prefix + "Select (count( distinct ?pub) as ?tot) where { "
                    + "GRAPH   <"+con.getCentralGraph()+"> {"
                    + "   <" + uri + ">  foaf:publications ?pub  }"
                    + "}";

            String querytopics = Prefix + 
                    "Select (GROUP_CONCAT( DISTINCT ?topicl ;  SEPARATOR = \"|\") as ?topicls )  where { "
                    + "GRAPH   <"+con.getCentralGraph()+"> { "
                    + "   <" + uri + ">  foaf:topic_interest [rdfs:label ?topicl]                                                                                            	}\n"
                    + "}";

            String queryCluster = Prefix
                    + "Select distinct ?lc where { "
                    + "GRAPH   <"+con.getClusterGraph()+"> {"
                    + "     ?pubc uc:hasPerson <" + uri + "> ."
                    + "             ?c   foaf:publications     ?pubc .  "
                    + "                       ?c      rdfs:label ?lc ."
                    + "                      FILTER (lang(?lc) = 'en')       "
                    + "           } "
                    + "}";

            String metaAuthor = Prefix
                    + "SELECT   ?names  ?orgnames ?members ?orcids ?imgs ?emails ?homepages ?citations ?hindexs ?i10indexs ?afs ?scs  where { "
                    + "GRAPH <"+con.getCentralGraph()+"> { "
                    + " { "
                    + "    select (GROUP_CONCAT( DISTINCT ?name ;  SEPARATOR = \"|\") as ?names)  "
                    + "   (GROUP_CONCAT( DISTINCT ?orcid ;  SEPARATOR = \"|\") as ?orcids  )  "
                    + "   (GROUP_CONCAT( DISTINCT ?img ;  SEPARATOR = \"|\") as ?imgs  )  "
                    + "   (GROUP_CONCAT( DISTINCT ?email ;  SEPARATOR = \"|\") as ?emails ) "
                    + "   (GROUP_CONCAT( DISTINCT ?homepage ;  SEPARATOR = \"|\") as ?homepages  ) "
                    + "   (GROUP_CONCAT( DISTINCT ?citation ;  SEPARATOR = \"|\") as ?citations  )           	"
                    + "   (GROUP_CONCAT( DISTINCT ?hindex  ;  SEPARATOR = \"|\") as ?hindexs  ) "
                    + "   (GROUP_CONCAT( DISTINCT ?i10index  ;  SEPARATOR = \"|\") as ?i10indexs  )  "
                    + "   (GROUP_CONCAT( DISTINCT ?sc  ;  SEPARATOR = \"|\") as ?scs  ) "
                    + "   (GROUP_CONCAT( DISTINCT ?af  ;  SEPARATOR = \"|\") as ?afs  ) "
                    + "   { "
                    + "    <"+uri+">   foaf:name ?name . "
                    + "    OPTIONAL { <"+uri+"> schema:memberOf ?member . } "
                    + "     OPTIONAL { "
                    + "     ?member foaf:name ?afname . "
                    + "     BIND(CONCAT(STR(?member), ';' , STR(?afname) ) AS ?af)  "
                    + "     } "
                    + "   OPTIONAL { "
                    + "    <"+uri+"> scoro:hasORCID ?orcid "
                    + "  } "
                    + "  OPTIONAL{  "
                    + "    <"+uri+"> foaf:img  ?img   "
                    + "  } "
                    + "       OPTIONAL { "
                    + "    <"+uri+"> vcard:hasEmail ?email "
                    + "  } "
                    + "   OPTIONAL { "
                    + "     <"+uri+">  foaf:homepage  ?homepage "
                    + "  } "
                    + "   OPTIONAL { "
                    + "     <"+uri+">  uc:citationCount  ?citation "
                    + "      }  "
                    + "   OPTIONAL { "
                    + "     <"+uri+">       uc:h-index ?hindex "
                    + "       }  "
                    + "     OPTIONAL { "
                    + "    <"+uri+">       uc:i10-index ?i10index "
                    + "       }  "
                    + "    OPTIONAL { "
                    + "      <"+uri+"> foaf:holdsAccount ?sc  "
                    + "       } "
                    + "        } "
                    + "  } "
                    + "    GRAPH <"+con.getOrganizationsGraph()+"> { "
                    + "    select (GROUP_CONCAT( DISTINCT ?orgname ;  SEPARATOR = \"|\")  as ?orgnames ) {  "
                    + "    ?member uc:name ?orgname . "
                    + "    GRAPH <"+con.getCentralGraph()+"> { "
                    + "    <"+uri+"> schema:memberOf ?member "
                    + "      }  "
                    + "      } "
                    + "      } "
                    + "  }  "
                    + "} ";
            
            List<Map<String, Value>> responseAuthor = sparqlService.query(QueryLanguage.SPARQL, metaAuthor);
            AuthorProfile a = new AuthorProfile ();
            if (!responseAuthor.isEmpty()) {
            a = proccessAuthor (responseAuthor);
            
           
            List<Map<String, Value>> responsetopics = sparqlService.query(QueryLanguage.SPARQL, querytopics);
            if ( responsetopics.size() > 0 && !responsetopics.get(0).isEmpty()){
            String topics = responsetopics.get(0).get("topicls").stringValue();
            a.setTopics(getRelevantTopics (topics.split("\\|")));
            }
            
            List<Map<String, Value>> responseCluster = sparqlService.query(QueryLanguage.SPARQL, queryCluster);
            if (!responseCluster.isEmpty()){
               String lc = responseCluster.get(0).get("lc").stringValue();
                a.setCluster(lc.split("\\|"));
            }
            
            List<Map<String, Value>> responseNpub = sparqlService.query(QueryLanguage.SPARQL, numpubquery);
            if (responsetopics.size() > 0 && !responsetopics.get(0).isEmpty()) {
              String num = responseNpub.get(0).get("tot").stringValue();
              a.setNpub(num);
            }
            }
            return objecttoJson(a);
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "{Status:Error}";
    }

    private AuthorProfile proccessAuthor(List<Map<String, Value>> responseAuthor) {
        AuthorProfile a = new AuthorProfile ();
        Map<String,Value> author =  responseAuthor.get(0);
        
        if (author.containsKey("names")){
        a.setName( getUniqueName (author.get("names").stringValue()));
        }
        
         if (author.containsKey("orgnames")){
        a.setOrg(author.get("orgnames").stringValue().split("\\|"));
        }
         
        if (author.containsKey("orcids"))
        {
        a.setOrcid( getUniqueOrcid(author.get("orcids").stringValue()));
        }
         if (author.containsKey("imgs"))
        {
        a.setImg( author.get("imgs").stringValue().split("\\|")[0]);
        }
         if (author.containsKey("emails"))
        {
        a.setEmails(author.get("emails").stringValue().split("\\|"));
        } 
         
         if (author.containsKey("homepages")) {
          a.setHomepages(author.get("homepages").stringValue().split("\\|"));
         }
         
         if (author.containsKey("citations")) {
          a.setCitation(getMaxValue(author.get("citations").stringValue().split("\\|"))+"");
         }
            
         if (author.containsKey("homepages")) {
          a.setHomepages(author.get("homepages").stringValue().split("\\|"));
         }
         
            if (author.containsKey("hindexs")) {
          a.setHindex(getMaxValue (author.get("hindexs").stringValue().split("\\|"))+"");
         }
               if (author.containsKey("i10indexs")) {
          a.setI10(getMaxValue (author.get("i10indexs").stringValue().split("\\|"))+"");
         }
               
               if (author.containsKey("afs")) {
          a.setOtheraf(getListOrg (author.get("afs").stringValue().split("\\|")));
         }
               
               if (author.containsKey("scs")) {
          a.setOtherProfile(getListOtherProfile (author.get("scs").stringValue().split("\\|")));
         }
       
       return a;
    }
    
    private String getUniqueName (String names) 
    {    int tokenmax = 0;
         int lengthmax = 0; 
         String candidate = "";
         String [] listNames = names.split("\\|");
         
         for (String name :listNames) {
          int  tokens = name.split(" ").length;
           int  length = name.length();
           if (tokens >= tokenmax &&  length > lengthmax) {
               tokenmax = tokens;
               lengthmax = length;
           candidate = name;
           }
            
         }  
        return candidate;
    }
    
    private String getUniqueOrcid (String orcids) {
        
        return orcids.split("\\|")[0];
    
    }

    private int getMaxValue(String [] split) {
         //String [] citations = split.split("|");
         int citasmax = 0;
         for (String c : split) {
                if (citasmax < Integer.parseInt(c.trim())) {
                  citasmax = Integer.parseInt(c.trim());
                }
         }
        return citasmax;
    }

    private  String [] getListOrg(String[] org) {
        List<String>  afiliations = new ArrayList<String> () ;
        for (String af :org) {
        String uri = af.split(";")[0];
        String name = af.split(";")[1];
        if (!uri.contains(con.getBaseURI())) {
           afiliations.add(name);
        }
        }
       return afiliations.toArray(new String[0]);
    }
    
    private String [] getListOtherProfile (String [] scs){
        List<String>  otherprof = new ArrayList<String> () ;
        for (String prof :scs){
        if (!prof.contains(con.getBaseURI())) {
           otherprof.add(prof);
        }
        }
     return otherprof.toArray(new String[0]);
    }
    
    private String objecttoJson (Object o) {
     ObjectMapper mapper = new ObjectMapper();
        try {        
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }    
    private String[] getRelevantTopics(String[] split) {
         Map <String,Integer>  aux = new HashMap ();
        for ( String topic  : split ) {
            for ( String comparetopic : split ) {
                  if (comparetopic.contains(topic)){
                      if (aux.containsKey(topic)) {
                          aux.put(topic, aux.get(topic)+1);
                      }else {
                          aux.put(topic, 0);
                      }
                  }
            }
         }
        
        List<String> ordertopic =  orderMap (aux);
       
        
        return ordertopic.size()>5 ? ordertopic.subList(0, 5).toArray(new String[0]) : ordertopic.toArray(new String[0]);
    }
    
    private List<String> orderMap (Map <String,Integer>  aux) {
     
        Comparator<Entry<String, Integer>> valueComparator = new Comparator<Entry<String,Integer>>() {
            
            @Override
            public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
                int v1 = e1.getValue();
                int v2 = e2.getValue();
                return  v2 - v1 ;
            }
        };
        
        // Sort method needs a List, so let's first convert Set to List in Java
        List<Entry<String, Integer>> listOfEntries = new ArrayList<Entry<String, Integer>>(aux.entrySet());
        
        // sorting HashMap by values using comparator
        Collections.sort(listOfEntries, valueComparator);
        List <String> auxlist = new ArrayList ();
        for (Entry<String, Integer> e:listOfEntries) {
            auxlist.add(  e.getKey());
        }
        return auxlist;
    }
}
