package de.nlptools.languageid.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class FDistributionTest {
    
    /**
     * Test of update method, of class FDistribution.
     */
    @Test
    public void testUpdate() {
        System.out.println("Test update() method.");
        String key = "testKey";
        FDistribution instance = new FDistribution();
        instance.update(key, 1.0);
        instance.update(key, 2.3);
        instance.update(key, 5.0);
        instance.update(key, 2.2);
        assertEquals(10.5, instance.get(key), 0.01);
    }

    /**
     * Test of updateAll method, of class FDistribution.
     */
    @Test
    public void testUpdateAll() {
        System.out.println("Test updateAll() method.");
        String key1 = "key1", key2 = "key2", key3 = "key3";
        HashMap<String, Double> values = new HashMap<>();
        values.put(key1, 0.67);
        values.put(key2, 2.0);
        FDistribution instance = new FDistribution();
        instance.put(key3, 1.0);
        instance.put(key2, 1.4);
        instance.updateAll(values);
        FDistribution expectedMap = new FDistribution();
        expectedMap.put(key1, 0.67);
        expectedMap.put(key2, 3.4);
        expectedMap.put(key3, 1.0);
        assertEquals(expectedMap, instance);
    }

    /**
     * Test of getSortedKeys method, of class FDistribution.
     */
    @Test
    public void testGetSortedKeys() {
        System.out.println("Test getSortedKeys() method.");
        FDistribution instance = new FDistribution();
        String key1 = "key1", key2 = "key2", key3 = "key3";
        instance.put(key3, 1.0);
        instance.put(key1, 1.4);
        instance.put(key2, 0.4);
        List<String> expResult = Arrays.asList(new String[]{key1, key3, key2});
        List<String> result = instance.getSortedKeys();
        assertEquals(expResult, result);
    }
    
}
