/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

        
        
        Provider a0 = null;
        Provider a1 = null;
        Provider a2 = null;
        List<Provider> Providers = new ArrayList();
        Providers.add(a0);
        Providers.add(a1);
        Providers.add(a2);

        Disambiguate(Providers, 0, Lists.newArrayList(""));
    }

    public void Disambiguate(List<Provider> list, int level, List<String> used) {
        Provider prOrg = list.get(0);
        List<Person> lsOrg = prOrg.getAuthors();
        prOrg.FillData(lsOrg);
        for (int i = 0; i < lsOrg.size(); i++) {
            Provider prX = list.get(i);
            Person PersonAG = lsOrg.get(i);
            List<Person> candidates = prX.getCandidates(PersonAG.URI);
            prX.FillData(candidates);
            for (int j = 0; j < candidates.size(); j++) {
                //blocking Name
                Person PersonPrX = candidates.get(j);
                if (PersonAG.checkName(PersonPrX)) {
                    if (PersonAG.checkAffiliations(PersonPrX)
                            || PersonAG.checkPublications(PersonPrX)
                            || PersonAG.checkCoauthors(PersonPrX)
                            || PersonAG.checkKeywords(PersonPrX)) {
                        PersonAG.enrich(PersonPrX);
                        MergeAuthor(prX.Graph, PersonPrX.URI, PersonAG.URI);
                    }

                }
            }
        }
    }

    public void MergeAuthor(String Graph, String URIP, String URIO) {

    }

    public void MergePublication(String Graph, String URI) {

    }

    public void MergeJournal(String Graph, String URI) {

    }
}
