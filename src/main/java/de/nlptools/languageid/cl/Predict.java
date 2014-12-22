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
 * A class to use a classifier from the command line.
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class Predict {
    
    public static void main(String[] args) {
        HashMap<String, String> parameters = readClArgs(args);
        if (parameters == null || parameters.size() < 1) {
            System.out.println("Unknown set of option.");
            Predict.printHelp();
            System.exit(1);
        }
        
        String testSet = parameters.get(Train.DOCS_SET);
        String classifierType = Train.DEFAULT_CLASSIFIER;
        String modelFile = parameters.containsKey(Train.MODEL_FILE) ?
                parameters.get(Train.MODEL_FILE) : classifierType + ".model";
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
            }
        } else if (classifierType.equals("NP")) {
            classifier = new NearestPrototypeClassifier();
        } else {
            Predict.printHelp();
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
    
    public static void printHelp(){
        System.out.printf("Usage: de.nlptools.languageid.cl.Predict [options] documentsSet %n"
                + "documentSet : a folder name that contains the documents %n"
                + "              or a single document %n"
                + "Options:%n"
                + "    -m modelFile : the name of the model file (default: NP.model)%n");
    }
}
