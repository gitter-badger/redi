/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.disambiguation;

import java.util.List;

/**
 *
 * @author cedia
 */
public class Person {

    public String URI;
    public List<List<String>> Name;
    public List<List<String>> Coauthors;
    public List<String> Publications;
    public List<String> Affiliations;
    public List<String> Keywords;

    
    public void enrich(Person p){
        
        Name.addAll(p.Name);
        Coauthors.addAll(p.Coauthors);
        Publications.addAll(p.Publications);
        Affiliations.addAll(p.Affiliations);
        Keywords.addAll(p.Keywords);
    
    }
    
    
    public boolean checkName(Person p) {

        return true;
    }

    public boolean checkCoauthors(Person p) {

        return true;
    }

    public boolean checkPublications(Person p) {

        return true;
    }

    public boolean checkAffiliations(Person p) {

        return true;
    }
    
    public boolean checkKeywords(Person p) {

        return true;
    }

}
