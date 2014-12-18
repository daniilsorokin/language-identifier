package de.nlptools.languageid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class FDistribution extends HashMap<String, Integer> {

    public void update(String key, Integer addValue){
        int value = this.containsKey(key) ? this.get(key) : 0;
        this.put(key, value + addValue);
    }
    
    public void updateAll(HashMap<String, Integer> addValues) {
        for (Entry<String, Integer> addValue : addValues.entrySet()) {
            this.update(addValue.getKey(), addValue.getValue());
        }
    }
    
    public List<String> getSortedKeys(){
        List<String> keys = new ArrayList<>(this.keySet());
        MapComparator comparator = new MapComparator(this);
        Collections.sort(keys, comparator);
        return keys;
    }
    
    private class MapComparator implements Comparator<String>{
        HashMap<String, Integer> base;
        
        public MapComparator(HashMap<String, Integer> base){
            this.base = base;
        }
        
        @Override
        public int compare(String t1, String t2) {
            return (base.get(t2)).compareTo(base.get(t1));
        }
    }
}
