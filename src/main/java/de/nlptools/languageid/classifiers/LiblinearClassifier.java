package de.nlptools.languageid.classifiers;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import de.nlptools.languageid.io.Dataset;
import de.nlptools.languageid.io.DocumentReader;
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
 * This class implements a classifier for the language identification task based
 * on the liblinear library. Liblinear is an implementation of a linear SVM 
 * algorithm.
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
*/
public class LiblinearClassifier implements IClassifier{

    private String[] selectedBigrams;
    private Model model;
    private ArrayList<String> languageIndex;
    private double c;

    /**
     * Create new empty liblinear classifier for language detection in
     * textual documents.
     */
    public LiblinearClassifier() {
        this.model = null;
        this.languageIndex = null;
        this.selectedBigrams = null;
        this.c = 1.0;
    }
    
    /**
     * Sets the cost parameter for the SVM classification.
     * 
     * @param c cost parameter
     */
    public void setC(double c){
        this.c = c;
    }
    
    /**
     * Builds a classifier on the provided training data.
     * 
     * @param documents a list of documents for training
     * @param labels a list of gold language labels for the training documents
     * @param bigramVectorSize amount of features to use for classification
     */    
    @Override
    public void build(String[] documents, String[] labels, int bigramVectorSize) {
        FDistribution bigramDist = new FDistribution();

        ArrayList<HashMap<String, Double>> documentVectors = new ArrayList<>(documents.length);
        HashSet<String> languages = new HashSet<>();
        for (int i = 0; i < documents.length; i++) {
                String document = documents[i];
                HashMap<String, Double> docNgramDist = 
                        DocumentTools.getDocumentBigramFDistribution(document);
                String lang = labels[i];
                documentVectors.add(docNgramDist);
                languages.add(lang);
                bigramDist.updateAll(docNgramDist);
        }
        List<String> bigrams = bigramDist.getSortedKeys();
        selectedBigrams = bigrams.subList(0, bigramVectorSize)
                .toArray(new String[bigramVectorSize]);
        
        languageIndex = new ArrayList<>(languages);
        Problem problem = new Problem();
        problem.l = documents.length;
        problem.n = bigramVectorSize;
        problem.y = new double[documents.length];
        problem.x = new Feature[documents.length][];
        
        for (int i = 0; i < documents.length; i++) {
            String lang = labels[i];
            int langId = languageIndex.indexOf(lang);
            problem.y[i] = langId;
            HashMap<String, Double> docNgramDist = documentVectors.get(i);
            List<Feature> vector = new ArrayList<>();
            for (int j = 0; j < bigramVectorSize; j++) {
                String ngram = selectedBigrams[j];
                Double value = docNgramDist.get(ngram);
                if(value != null) vector.add(new FeatureNode(j + 1, value));
            }
            problem.x[i] = vector.toArray(new Feature[vector.size()]);
        }
        SolverType solver = SolverType.L2R_L2LOSS_SVC_DUAL;
        Parameter parameter = new Parameter(solver, this.c, 0.1);
        model = Linear.train(problem, parameter);
    }
    
    /**
     * Predicts the language for a given document.
     * 
     * @param document document to process
     * @return predicted language
     */    
    @Override
    public String predict(String document) {
        HashMap<String, Double> docNgramDist = DocumentTools.getDocumentBigramFDistribution(document);
        List<Feature> vector = new ArrayList<>();
        for (int j = 0; j < selectedBigrams.length; j++) {
            String bigram = selectedBigrams[j];
            Double value = docNgramDist.get(bigram);
            if(value != null) vector.add(new FeatureNode(j + 1, value));
        }
        Feature[] liblinearVector = vector.toArray(new Feature[vector.size()]);
        Double prediction = Linear.predict(model, liblinearVector);
        String predictedLanguage = languageIndex.get(prediction.intValue());
        return predictedLanguage;
    }
    
    /**
     * Evaluates the classifier on the give test set. 
     * 
     * @param testDocuments list of the documents for testing
     * @param goldLabels list of the gold labels
     * @return the result of the evaluation
     */
    @Override
    public EvaluationResult evaluate(String[] testDocuments, String[] goldLabels){
        int correctlyClassified = 0;
        int numberOfDocuments = testDocuments.length;
        for (int i = 0; i < testDocuments.length; i++) {
            String document = testDocuments[i];
            String goldLang = goldLabels[i];
            String predicted = this.predict(document);
            if (predicted.equals(goldLang)) correctlyClassified++;
        }
        double accuracy = (double) correctlyClassified / (double) numberOfDocuments;
        return new EvaluationResult(accuracy);
    }
    
    /**
     * Stores the classifier model in a file.
     * 
     * @param fileName the name of the file name to store the model
     */    
    @Override
    public void saveModel(String fileName){
        try {
            this.model.save(new File(fileName));
        } catch (IOException ex) {
            Logger.getLogger(LiblinearClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Loads a classifier model from file.
     * 
     * @param fileName the model file
     */    
    @Override
    public void loadModel(String fileName){
        try {
            this.model = Model.load(new File(fileName));
        } catch (IOException ex) {
            Logger.getLogger(LiblinearClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
