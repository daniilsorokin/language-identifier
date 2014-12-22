package de.nlptools.languageid.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class represents a frequency distribution of text elements.
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class FDistribution extends HashMap<String, Double> {

    /**
     * Adds a value to an existing value or creates a new one 
     * if there was no previous value for the given text element.
     * 
     * @param key text element
     * @param addValue update with this value
     */
    public void update(String key, Double addValue){
        double value = this.containsKey(key) ? this.get(key) : 0.0;
        this.put(key, value + addValue);
    }
    
    /**
     * Updates the values in the current frequency distribution using the pairs
     * of the text elements and values in the given map.
     * 
     * @param addValues a Map that contains pairs of text elements and frequencies
     */
    public void updateAll(HashMap<String, Double> addValues) {
        for (Entry<String, Double> addValue : addValues.entrySet()) {
            this.update(addValue.getKey(), addValue.getValue());
        }
    }
    
    /**
     * Returns a list of text elements from the current frequency distribution
     * sorted by frequency
     * 
     * @return a sorted list of text elements
     */
    public List<String> getSortedKeys(){
        List<String> keys = new ArrayList<>(this.keySet());
        MapComparator comparator = new MapComparator(this);
        Collections.sort(keys, comparator);
        return keys;
    }
    
    private class MapComparator implements Comparator<String>{
        HashMap<String, Double> base;
        
        public MapComparator(HashMap<String, Double> base){
            this.base = base;
        }
        
        @Override
        public int compare(String t1, String t2) {
            return (base.get(t2)).compareTo(base.get(t1));
        }
    }
}
