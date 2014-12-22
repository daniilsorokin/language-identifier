package de.nlptools.languageid.classifiers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class LiblinearClassifierTest {

    /**
     * Test of loadModel method, of class LiblinearClassifier.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testSaveAndLoadModel() throws IOException {
        System.out.println("Test saveModel() and loadModel() methods.");
        String contentEn = "This is really a test document.";
        String contentDe = "Das ist wirklich ein Dokument.";
        String[] documents = new String[]{contentEn, contentEn, contentDe};
        String[] langs = new String[]{"en", "en", "de"};        
        String modelFileName = "test.model";
        
        NearestPrototypeClassifier classifier1 = new NearestPrototypeClassifier();
        classifier1.build(documents, langs, 1000);
        classifier1.saveModel(modelFileName);
        
        NearestPrototypeClassifier classifier2 = new NearestPrototypeClassifier();
        classifier2.loadModel(modelFileName);
        
        String prediction1 = classifier2.predict(contentEn);        
        String expectedPrediction1 = "en";
        assertEquals(prediction1, expectedPrediction1);

        String prediction2 = classifier2.predict(contentDe);        
        String expectedPrediction2 = "de";
        assertEquals(prediction2, expectedPrediction2);
        
        Files.delete(Paths.get(modelFileName));
    }
}
