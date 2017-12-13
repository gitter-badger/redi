/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DisambiguationService;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Provider;

/**
 *
 * @author cedia
 */
@ApplicationScoped
public class DisambiguationServiceImpl implements DisambiguationService {

    @Inject
    private QueriesService queriesService;

    @Inject
    private ConstantService constantService;

    @Inject
    private SparqlService sparqlService;

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
        } catch (MarmottaException ex) {
            Logger.getLogger(DisambiguationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Init(List<Provider> list) throws MarmottaException {
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

    public void Disambiguate(List<List<Person>> Candidates, int level, Person P) {
        if (level >= Candidates.size()) {
            return;
        }
        List<Person> get = Candidates.get(level);
        for (Person p : get) {
            if (P.check(p)) {
                Person enrich = P.enrich(p);
                Disambiguate(Candidates, level + 1, enrich);
                registerSameAs(P.URI, p.URI);
            } else {
                Disambiguate(Candidates, level + 1, P);
            }
        }
    }
    
    
    public void registerSameAs(String URIO, String URIP){
        
    
    }
}
