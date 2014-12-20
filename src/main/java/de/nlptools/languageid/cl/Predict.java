package de.nlptools.languageid.cl;

import static de.nlptools.languageid.cl.Train.readClArgs;
import de.nlptools.languageid.classifiers.EvaluationResult;
import de.nlptools.languageid.classifiers.IClassifier;
import de.nlptools.languageid.classifiers.LiblinearClassifier;
import de.nlptools.languageid.classifiers.NearestPrototypeClassifier;
import de.nlptools.languageid.io.Dataset;
import de.nlptools.languageid.io.DocumentReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class Predict {
    
    public static void main(String[] args) {
        HashMap<String, String> parameters = readClArgs(args);
        if (parameters == null || parameters.size() != 2) {
            System.out.println("Unknown set of option.");
            Train.printHelp();
            System.exit(1);
        }
        
        String modelFile = parameters.get(Predict.MODEL_FILE);
        String testSet = parameters.get(Predict.TEST_SET);
        
        String classifierType = Train.DEFAULT_CLASSIFIER;
        try(BufferedReader in = new BufferedReader
            (new InputStreamReader(new FileInputStream(modelFile), 
                    NearestPrototypeClassifier.ENCODING))){
            String firstLine = in.readLine();
            if (firstLine != null){ 
                if(firstLine.trim().equals(NearestPrototypeClassifier.MODEL_NAME))
                    classifierType = Train.NP_CLASSIFIER;
                else
                    classifierType = Train.LIBLINEAR_CLASSIFIER;
            }
        } catch (IOException ex) {
            Logger.getLogger(Predict.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        IClassifier classifier = null;
        if (classifierType.equals("Liblinear")){
            try {
                Class.forName( "de.bwaldvogel.liblinear.Linear" );
                classifier = new LiblinearClassifier();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(LiblinearClassifier.class.getName())
                        .log(Level.SEVERE, "Liblinear is not found! This "
                                + "classifier makes use of an external LibLinear library. "
                                + "\n Please check that the liblinear-1.94.jar is in the classpath.");
                System.exit(1);
            } catch ( NumberFormatException ex){
                Logger.getLogger(LiblinearClassifier.class.getName())
                        .log(Level.SEVERE, "C parameter must be a number.");
                System.exit(1);
            }
        } else if (classifierType.equals("NP")) {
            classifier = new NearestPrototypeClassifier();
        } else {
            Train.printHelp();
            System.exit(1);
        }
        
        System.out.println("Loading the model.");
        classifier.loadModel(modelFile);
        System.out.println("Processing the documents.");
        File testFile = new File(testSet);
        if (testFile.isFile()) {
            try {
                String content = DocumentReader.readContentFromFile(testFile);
                String prediction = classifier.predict(content);
                System.out.println("Document language: " + prediction);
            } catch (IOException ex) {
                Logger.getLogger(Predict.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (testFile.isDirectory()) {
            Dataset test = DocumentReader.readDatasetFromFolder(testSet);
            if (test.hasLabels()){
                EvaluationResult results = classifier.evaluate(test.getDocuments(), test.getLabels());
                double accuracy = results.getAccuracy();
                System.out.println("Accuracy: " + accuracy);
            } else {
                System.out.println("Document languages:");
                for (String document : test.getDocuments()) {
                    String prediction = classifier.predict(document);
                    System.out.println(prediction);
                }
            }
        }
    }
    
    public static final String MODEL_FILE = "modelFile";
    public static final String TEST_SET = "testSet";
    
    public static void printHelp(){
        System.out.printf("Usage: de.nlptools.languageid.cl.Predict modelFile testSet %n"
                + "modelFile : name of the model file. %n"
                + "documentSet : a folder containing the documents or a meta file %n"
                + "         with a list of file names or single file name. %n"
                + "         If the documentSet contains gold labels the tool %n"
                + "         perfomes evaluation with the given model. %n");
    }
}
