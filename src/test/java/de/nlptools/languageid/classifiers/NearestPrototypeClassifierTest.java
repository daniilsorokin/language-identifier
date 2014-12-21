/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.nlptools.languageid.classifiers;

import de.nlptools.languageid.io.DocumentReader;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Даня
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
     * Test of build method, of class NearestPrototypeClassifier.
     */
    @Test
    public void testBuild() {

    }

    /**
     * Test of predict method, of class NearestPrototypeClassifier.
     */
    @Test
    public void testPredict() {

    }
    
    /**
     * Test of saveModel method, of class NearestPrototypeClassifier.
     */
    @Test
    public void testSaveModel() {

    }

    /**
     * Test of loadModel method, of class NearestPrototypeClassifier.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testLoadModel() throws IOException {
        System.out.println("loadModel");
        String fileName = "NP.model";
        NearestPrototypeClassifier classifier = new NearestPrototypeClassifier();
        classifier.loadModel(fileName);
        String testFile = "de_240125.txt";
        String content = DocumentReader.readContentFromFile(new File(testFile));
        String prediction = classifier.predict(content);
        String expectedPrediction = "de";
        assertEquals(prediction, expectedPrediction);
    }
    
}
