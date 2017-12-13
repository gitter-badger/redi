/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.disambiguation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author cedia
 */
public class Provider {

    public String Name;
    public String Graph;

    private SparqlService sparql;

    public Provider(String Name, String Graph, SparqlService sparql) {
        this.Name = Name;
        this.Graph = Graph;
        this.sparql = sparql;
    }

    public List<Person> getAuthors() throws MarmottaException {
        String qry = "select distinct ?a \n"
                + "{\n"
                + "	graph <" + Graph + "> {\n"
                + "  		?a a <http://xmlns.com/foaf/0.1/Person>\n"
                + "	}\n"
                + "}";
        List<Map<String, Value>> persons = sparql.query(QueryLanguage.SPARQL, qry);
        List<Person> lsp = new ArrayList<>();
        for (Map<String, Value> row : persons) {
            Person p = new Person();
            p.Origin = this;
            p.URI = row.get("a").stringValue();
            lsp.add(p);
        }
        return lsp;
    }

    public List<Person> getCandidates(String URI) throws MarmottaException {
        String qry = "select distinct ?a {\n"
                + "	graph <"+Graph+"> {\n"
                + "  		?q <http://www.w3.org/2002/07/owl#oneOf> <"+URI+"> .\n"
                + "      	?a <http://www.w3.org/2002/07/owl#oneOf> ?q .\n"
                + "	}\n"
                + "}";
        List<Map<String, Value>> persons = sparql.query(QueryLanguage.SPARQL, qry);
        List<Person> lsp = new ArrayList<>();
        for (Map<String, Value> row : persons) {
            Person p = new Person();
            p.Origin = this;
            p.URI = row.get("a").stringValue();
            lsp.add(p);
        }
        return lsp;
    }

    public void FillData(List<Person> lsa) {

    }

}
