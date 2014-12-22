package de.nlptools.languageid.tools;

import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class DocumentToolsTest {
    
    /**
     * Test of getDocumentBigramFDistribution method, of class DocumentTools.
     */
    @Test
    public void testGetDocumentBigramFDistribution() {
        System.out.println("Test getDocumentBigramFDistribution() method.");
        String content = "This is a document.";
        HashMap<String, Double> expResult = new HashMap<>();
        expResult.put("Th", 1.0);
        expResult.put("hi", 1.0);
        expResult.put("is", 2.0);
        expResult.put("s ", 2.0);
        expResult.put(" i", 1.0);
        expResult.put(" a", 1.0);
        expResult.put("a ", 1.0);
        expResult.put(" d", 1.0);
        expResult.put("do", 1.0);
        expResult.put("oc", 1.0);
        expResult.put("cu", 1.0);
        expResult.put("um", 1.0);
        expResult.put("me", 1.0);
        expResult.put("en", 1.0);
        expResult.put("nt", 1.0);
        expResult.put("t.", 1.0);
        HashMap<String, Double> result = DocumentTools.getDocumentBigramFDistribution(content);
        assertEquals(expResult, result);
    }
    
}
