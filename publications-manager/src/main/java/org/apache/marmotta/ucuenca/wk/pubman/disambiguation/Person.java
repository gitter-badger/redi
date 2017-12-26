/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.disambiguation;

import java.util.ArrayList;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.utils.AffiliationUtils;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.utils.NameUtils;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.utils.PublicationUtils;

/**
 *
 * @author cedia
 */
public class Person {

    public static final double thresholdName = 0.9;
    public static final double thresholdCAName = 0.9;
    public static final double thresholdTitle = 0.95;
    public static final double thresholdAff = 0.99;
    public static final int thresholdCoauthors = 2;
    public static final int thresholdPublications = 1;
    public static final int thresholdAffiliation = 1;

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

        Boolean checkName = checkName(p);
        if (checkName != null && checkName == true) {
            Boolean checkAffiliations = checkAffiliations(p);
            Boolean checkCoauthors = checkCoauthors(p);
            Boolean checkPublications = checkPublications(p);
            Boolean checkTopics = checkTopics(p);

            if (checkCoauthors == null && checkPublications == null && checkTopics == null
                    && checkAffiliations != null && checkAffiliations == true) {
                return true;
            }

            int c = 0;
            if (checkAffiliations != null && checkAffiliations == true) {
                c++;
            }
            if (checkPublications != null && checkPublications == true) {
                c++;
            }
            if (checkCoauthors != null && checkCoauthors == true) {
                c++;
            }
            if (checkTopics != null && checkTopics == true) {
                c++;
            }
            return c >= 2;

        }
        return false;
    }

    public Boolean checkName(Person p) {
        if (Name.isEmpty() || p.Name.isEmpty()) {
            return null;
        }
        List<String> name1 = NameUtils.bestName(Name);
        List<String> name2 = NameUtils.bestName(p.Name);
        double sim = NameUtils.compareName(name1, name2);
        System.out.println("COMPA" + name1 + "+" + name2 + "+" + sim);
        return sim >= thresholdName;
    }

    public Boolean checkCoauthors(Person p) {
        if (Coauthors.isEmpty() || p.Coauthors.isEmpty()) {
            return null;
        }
        List<List<String>> name1 = NameUtils.uniqueName(Coauthors);
        List<List<String>> name2 = NameUtils.uniqueName(p.Coauthors);
        List<List<String>> uname1 = new ArrayList<>();
        List<List<String>> uname2 = new ArrayList<>();
        int co = 0;
        for (List<String> n1 : name1) {
            for (List<String> n2 : name2) {
                if (!uname1.contains(n1) && !uname2.contains(n2)) {
                    double sim =NameUtils.compareName(n1, n2);
                    System.out.println("COMPA" + n1 + "+" + n2 + "+"+sim);
                    if (sim >= thresholdCAName) {
                        co++;
                        uname1.add(n1);
                        uname2.add(n2);
                    }
                }
            }
        }
        return co >= thresholdCoauthors;
    }

    public Boolean checkPublications(Person p) {
        if (Publications.isEmpty() || p.Publications.isEmpty()) {
            return null;
        }
        List<String> name1 = PublicationUtils.uniqueTitle(Publications);
        List<String> name2 = PublicationUtils.uniqueTitle(p.Publications);
        List<String> uname1 = new ArrayList<>();
        List<String> uname2 = new ArrayList<>();
        int co = 0;
        for (String n1 : name1) {
            for (String n2 : name2) {
                if (!uname1.contains(n1) && !uname2.contains(n2)) {
                    double sim= PublicationUtils.compareTitle(n1, n2);
                    System.out.println("COMPA" + n1 + "+" + n2 + "+"+sim);
                    if ( sim >= thresholdTitle) {
                        co++;
                        uname1.add(n1);
                        uname2.add(n2);
                    }
                }
            }
        }
        return co >= thresholdPublications;
    }

    public Boolean checkAffiliations(Person p) {
        if (Affiliations.isEmpty() || p.Affiliations.isEmpty()) {
            return null;
        }
        List<String> name1 = AffiliationUtils.uniqueTitle(Affiliations);
        List<String> name2 = AffiliationUtils.uniqueTitle(p.Affiliations);
        List<String> uname1 = new ArrayList<>();
        List<String> uname2 = new ArrayList<>();
        int co = 0;
        for (String n1 : name1) {
            for (String n2 : name2) {
                if (!uname1.contains(n1) && !uname2.contains(n2)) {
                    double sim=AffiliationUtils.compareTitle(n1, n2);
                    System.out.println("COMPA" + n1 + "+" + n2 + "+"+sim);
                    if ( sim>= thresholdAff) {
                        co++;
                        uname1.add(n1);
                        uname2.add(n2);
                    }
                }
            }
        }
        return co >= thresholdAffiliation;
    }

    public Boolean checkTopics(Person p) {
        if (Affiliations.isEmpty() || p.Affiliations.isEmpty()) {
            return null;
        }

        return null;
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
        } else {
            newPersonClon.Publications = new ArrayList<>();
        }
        newPersonClon.Publications.addAll(new ArrayList<>(p.Publications));
        if (Affiliations != null) {
            newPersonClon.Affiliations = new ArrayList<>(Affiliations);
        } else {
            newPersonClon.Affiliations = new ArrayList<>();
        }
        newPersonClon.Affiliations.addAll(new ArrayList<>(p.Affiliations));
        if (Topics != null) {
            newPersonClon.Topics = new ArrayList<>(Topics);
        } else {
            newPersonClon.Topics = new ArrayList<>();
        }
        newPersonClon.Topics.addAll(new ArrayList<>(p.Topics));
        return newPersonClon;
    }

}
