package de.nlptools.languageid.classifiers;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import de.nlptools.languageid.tools.FDistribution;
import de.nlptools.languageid.tools.DocumentTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
*/
public class LiblinearClassifier {
    
    public static void main(String[] args) {
        try {
            Class.forName( "de.bwaldvogel.liblinear.Linear" );
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LiblinearClassifier.class.getName())
                        .log(Level.SEVERE, "Liblinear is not found! This "
                        + "classifier makes use of an external LibLinear library. "
                        + "\n Please check that the liblinear-1.94.jar is in the classpath.");
            return;
        } 
        
        
        String dir = "/home/dsorokin/Downloads/ijcnlp2011-langid/";
        File[] domainFiles = new File(dir + "wikiraw/domain/").listFiles();

        LiblinearClassifier classifier = new LiblinearClassifier();
        System.out.println("Building the classifier.");
        classifier.build(domainFiles);

        File[] langFiles = new File(dir + "wikiraw/lang/").listFiles();
        System.out.println("Classifying lang documents.");
        int correctlyClassified = 0;
        int numberOfDocuments = langFiles.length;
        for (File file : langFiles) {
            String goldLang = DocumentTools.getDocumentLanguageFromFileName(file);
            String predicted = classifier.predict(file);
            if (predicted.equals(goldLang)) correctlyClassified++;
        }
        double accuracy = (double) correctlyClassified / (double) numberOfDocuments;
        System.out.println("Accuracy: " + accuracy);

    }

    private int featureVectorSize = 3000;
    private String[] selectedBigrams;
    private String defaultLang = "en";
    
    private Model model;
    private ArrayList<String> languageIndex;

    public LiblinearClassifier() {
    }
    
    
    public void build(File[] documents){
        FDistribution bigramDist = new FDistribution();

        HashMap<String, HashMap<String, Double>> documentVectors = new HashMap<>();
        HashSet<String> languages = new HashSet<>();
        for (File file : documents) {
            try {
                HashMap<String, Double> docNgramDist = 
                        DocumentTools.getDocumentBigramFDistribution(file);
                String lang = 
                        DocumentTools.getDocumentLanguageFromFileName(file);
                documentVectors.put(file.getName(),docNgramDist);
                languages.add(lang);
                bigramDist.updateAll(docNgramDist);
            } catch (IOException ex) {
                Logger.getLogger(LiblinearClassifier.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        
        List<String> bigrams = bigramDist.getSortedKeys();
        selectedBigrams = bigrams.subList(0, featureVectorSize)
                .toArray(new String[featureVectorSize]);
        
        languageIndex = new ArrayList<>(languages);
        Problem problem = new Problem();
        problem.l = documents.length;
        problem.n = featureVectorSize;
        problem.y = new double[documents.length];
        problem.x = new Feature[documents.length][];
        
        for (int i = 0; i < documents.length; i++) {
            String lang = DocumentTools.getDocumentLanguageFromFileName(documents[i]);
            int langId = languageIndex.indexOf(lang);
            problem.y[i] = langId;
            HashMap<String, Double> docNgramDist = documentVectors.get(documents[i].getName());
            List<Feature> vector = new ArrayList<>();
            for (int j = 0; j < featureVectorSize; j++) {
                String ngram = selectedBigrams[j];
                Double value = docNgramDist.get(ngram);
                if(value != null) vector.add(new FeatureNode(j + 1, value));
            }
            problem.x[i] = vector.toArray(new Feature[vector.size()]);
        }
        
        SolverType solver = SolverType.L2R_L2LOSS_SVC_DUAL;
        double cost = 128.0;
        double eps = 0.1;
        
        Parameter parameter = new Parameter(solver, cost, eps);
        model = Linear.train(problem, parameter);
    }
    
    public String predict(File document){
        try {
            HashMap<String, Double> docNgramDist = DocumentTools.getDocumentBigramFDistribution(document);
            List<Feature> vector = new ArrayList<>();
            for (int j = 0; j < featureVectorSize; j++) {
                String bigram = selectedBigrams[j];
                Double value = docNgramDist.get(bigram);
                if(value != null) vector.add(new FeatureNode(j + 1, value));
            }
            Feature[] liblinearVector = vector.toArray(new Feature[vector.size()]);
            Double prediction = Linear.predict(model, liblinearVector);
            String predictedLanguage = languageIndex.get(prediction.intValue());
            return predictedLanguage;
        } catch (IOException ex) {
            Logger.getLogger(LiblinearClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return defaultLang;
    }
    
}
