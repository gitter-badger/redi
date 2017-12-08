/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.disambiguation;

import java.util.List;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;

/**
 *
 * @author cedia
 */
public class Provider {
    public String Name;
    public String Graph;
    
    private SparqlService sparql ;
    private QueriesService queries ;
    
    
    
    public List<Person> getAuthors(){
        return null;
    }
    
    
    public List<Person> getCandidates(String URI){
        return null;
    }
    
    public void FillData(List<Person> lsa){
        
    }
    
    
}
