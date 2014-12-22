package de.nlptools.languageid.cl;

import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class TrainTest {

    /**
     * Test of readClArgs method, of class Train.
     */
    @Test
    public void testReadClArgs1() {
        System.out.println("Test readClArgs() method, Run 1");
        String[] args = new String[]{"-m","model.out","train.in", };
        HashMap<String, String> expResult = new HashMap<>();
        expResult.put(Train.DOCS_SET, "train.in");
        expResult.put(Train.MODEL_FILE, "model.out");
        HashMap<String, String> result = Train.readClArgs(args);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of readClArgs method, of class Train.
     */
    @Test
    public void testReadClArgs2() {
        System.out.println("Test readClArgs() method, Run 2");
        String[] args = new String[]{"-t", "Liblinear", "-c", "5", "train.in"};
        HashMap<String, String> expResult = new HashMap<>();
        expResult.put(Train.CLASSIFIER, "Liblinear");
        expResult.put(Train.C_PARAMETER, "5");
        expResult.put(Train.DOCS_SET, "train.in");
        HashMap<String, String> result = Train.readClArgs(args);
        assertEquals(expResult, result);
    }    

    /**
     * Test of readClArgs method, of class Train.
     */
    @Test
    public void testReadClArgs3() {
        System.out.println("Test readClArgs() method, Run 3");
        String[] args = new String[]{};
        HashMap<String, String> result = Train.readClArgs(args);
        assertEquals(null, result);
    }     
}
