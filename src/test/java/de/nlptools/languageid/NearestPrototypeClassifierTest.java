package de.nlptools.languageid;

import de.nlptools.languageid.NearestPrototypeClassifier;
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
public class NearestPrototypeClassifierTest {
    
    public NearestPrototypeClassifierTest() {
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
     * Test of cosine method, of class NearestPrototypeClassifier.
     */
    @org.junit.Test
    public void testCosine() {
        System.out.println("Test \"cosine\" method");
        double[] a = {0.0,0.0,1.0};
        double[] b = {0.0,0.0,-1.0};
        double expResult = -1.0;
        double result = NearestPrototypeClassifier.cosine(a, b);
        assertEquals(expResult, result, 0.00001);

        a = new double[]{0.0,2.0,0.0};
        b = new double[]{2.0,0.0,0.0};
        expResult = 0.0;
        result = NearestPrototypeClassifier.cosine(a, b);
        assertEquals(expResult, result, 0.00001);

        a = new double[]{2.0,2.0,2.0};
        b = new double[]{1.0,0.0,0.5};
        expResult = 0.77459;
        result = NearestPrototypeClassifier.cosine(a, b);
        assertEquals(expResult, result, 0.00001);
    }
    
}
