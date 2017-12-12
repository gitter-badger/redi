/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.disambiguation;

import java.util.ArrayList;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.commons.util.ModifiedJaccardMod;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.utils.NameUtils;

/**
 *
 * @author cedia
 */
public class Person {

    private final double thresholdName = 0.9;

    public Provider Origin;
    public String URI;
    public List<List<String>> Name;
    public List<List<String>> Coauthors;
    public List<String> Publications;
    public List<String> Affiliations;
    public List<String> Topics;

    public boolean check(Person p) {
        if (URI == null) {
            return true;
        }

        return true;
    }

    public Boolean checkName(Person p) {
        if (Name.isEmpty() || p.Name.isEmpty()) {
            return null;
        }
        List<String> name1 = NameUtils.bestName(Name);
        List<String> name2 = NameUtils.bestName(p.Name);
        double sim = -1;
        int tipo = 0;
        String nf1 = "";
        String nf2 = "";
        String nl1 = "";
        String nl2 = "";
        String nc1 = "";
        String nc2 = "";
        if (name1.size() == 1) {
            if (name2.size() == 1) {
                tipo = 1;
                nc1 = name1.get(0);
                nc2 = name2.get(0);
            } else {
                tipo = 2;
                nc2 = name1.get(0);
                nf1 = name2.get(0);
                nl1 = name2.get(1);
            }
        } else {
            if (name2.size() == 1) {
                tipo = 2;
                nc2 = name2.get(0);
                nf1 = name1.get(0);
                nl1 = name1.get(1);
            } else {
                tipo = 3;
                nf1 = name1.get(0);
                nl1 = name1.get(1);
                nf2 = name2.get(0);
                nl2 = name2.get(1);
            }
        }
        ModifiedJaccardMod metric = new ModifiedJaccardMod();
        switch (tipo) {
            case 1:
                metric.prioritizeWordOrder = false;
                sim = metric.distanceName(nc1, nc2);
                break;
            case 2:
                metric.prioritizeWordOrder = false;
                sim = metric.distanceName(nf1 + " " + nl1, nc2);
                break;
            case 3:
                metric.prioritizeWordOrder = true;
                double sim1 = metric.distanceName(nl1, nl2);
                metric.prioritizeWordOrder = false;
                double sim2 = metric.distanceName(nf1, nf2);
                sim = (sim1 + sim2) / 2;
                break;
        }
        return sim >= thresholdName;
    }

    public Boolean checkCoauthors(Person p) {
        if (Coauthors.isEmpty() || p.Coauthors.isEmpty()) {
            return null;
        }

        return true;
    }

    public Boolean checkPublications(Person p) {
        if (Publications.isEmpty() || p.Publications.isEmpty()) {
            return null;
        }

        return true;
    }

    public Boolean checkAffiliations(Person p) {

        if (Affiliations.isEmpty() || p.Affiliations.isEmpty()) {
            return null;
        }

        return true;
    }

    public Boolean checkTopics(Person p) {
        if (Affiliations.isEmpty() || p.Affiliations.isEmpty()) {
            return null;
        }

        return true;
    }

    public Person enrich(Person p) {
        Person newPersonClon = new Person();
        if (URI == null) {
            newPersonClon.Origin = p.Origin;
            newPersonClon.URI = p.URI + "";
        } else {
            newPersonClon.Origin = Origin;
            newPersonClon.URI = URI + "";
        }
        newPersonClon.Name = new ArrayList<>();
        if (Name != null) {
            for (List<String> i : Name) {
                newPersonClon.Name.add(new ArrayList<>(i));
            }
        }
        for (List<String> i : p.Name) {
            newPersonClon.Name.add(new ArrayList<>(i));
        }
        newPersonClon.Coauthors = new ArrayList<>();
        if (Coauthors != null) {
            for (List<String> i : Coauthors) {
                newPersonClon.Coauthors.add(new ArrayList<>(i));
            }
        }
        for (List<String> i : p.Coauthors) {
            newPersonClon.Coauthors.add(new ArrayList<>(i));
        }
        if (Publications != null) {
            newPersonClon.Publications = new ArrayList<>(Publications);
        }
        newPersonClon.Publications.addAll(new ArrayList<>(p.Publications));
        if (Affiliations != null) {
            newPersonClon.Affiliations = new ArrayList<>(Affiliations);
        }
        newPersonClon.Affiliations.addAll(new ArrayList<>(p.Affiliations));
        if (Topics != null) {
            newPersonClon.Topics = new ArrayList<>(Topics);
        }
        newPersonClon.Topics.addAll(new ArrayList<>(p.Topics));
        return newPersonClon;
    }

}
