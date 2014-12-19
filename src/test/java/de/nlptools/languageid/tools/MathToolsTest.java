/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.nlptools.languageid.tools;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dsorokin
 */
public class MathToolsTest {
    
    public MathToolsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of cosine method, of class MathTools.
     */
    @Test
    public void testCosine() {
        System.out.println("Test \"cosine\" method");
        double[] a = {0.0,0.0,1.0};
        double[] b = {0.0,0.0,-1.0};
        double expResult = -1.0;
        double result = MathTools.cosine(a, b);
        assertEquals(expResult, result, 0.00001);

        a = new double[]{0.0,2.0,0.0};
        b = new double[]{2.0,0.0,0.0};
        expResult = 0.0;
        result = MathTools.cosine(a, b);
        assertEquals(expResult, result, 0.00001);

        a = new double[]{2.0,2.0,2.0};
        b = new double[]{1.0,0.0,0.5};
        expResult = 0.77459;
        result = MathTools.cosine(a, b);
        assertEquals(expResult, result, 0.00001);
    }
    
}
