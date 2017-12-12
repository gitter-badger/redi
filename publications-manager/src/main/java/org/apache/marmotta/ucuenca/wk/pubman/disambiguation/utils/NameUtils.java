/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.disambiguation.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author cedia
 */
public class NameUtils {
    
    
    public static List<String> bestName (List<List<String>> options){
    
        int selection = -1;
        int selectionScore = -1;
        for (int i=0; i<options.size(); i++){
            List<String> get = options.get(i);
            
            
            int v1 =get.size();
            int v2=0;
            for(String n : get){
                v2+=n.length();
            }
            
            int score=v1*v2;
            
            if (score > selectionScore){
                selectionScore=score;
                selection=i;
            }
        }
        return options.get(selection);
    }
    
}
