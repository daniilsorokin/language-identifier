package de.nlptools.languageid.tools;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class MathToolsTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("*** MathTool class testing ***");
    }

    /**
     * Test of cosine method, of class MathTools.
     */
    @Test
    public void testCosine1() {
        System.out.println("Test cosine() method, Run 1");
        double[] a = {0.0,0.0,1.0};
        double[] b = {0.0,0.0,-1.0};
        double expResult = -1.0;
        double result = MathTools.cosine(a, b);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of cosine method, of class MathTools.
     */
    @Test
    public void testCosine2() {
        System.out.println("Test cosine() method, Run 2");
        double[] a = new double[]{0.0,2.0,0.0};
        double[] b = new double[]{2.0,0.0,0.0};
        double expResult = 0.0;
        double result = MathTools.cosine(a, b);
        assertEquals(expResult, result, 0.00001);
    }
    
    /**
     * Test of cosine method, of class MathTools.
     */
    @Test
    public void testCosine3() {
        System.out.println("Test cosine() method, Run 3");
        double[] a = new double[]{2.0,2.0,2.0};
        double[] b = new double[]{1.0,0.0,0.5};
        double expResult = 0.77459;
        double result = MathTools.cosine(a, b);
        assertEquals(expResult, result, 0.00001);
    }
    
}
