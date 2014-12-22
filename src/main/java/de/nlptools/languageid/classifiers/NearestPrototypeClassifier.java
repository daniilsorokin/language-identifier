package de.nlptools.languageid.classifiers;

import de.nlptools.languageid.tools.FDistribution;
import de.nlptools.languageid.tools.DocumentTools;
import de.nlptools.languageid.tools.MathTools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a simple nearest prototype classifier for the language
 * identification tasks. It constructs language prototype vectors that contain 
 * average bigram frequencies computed over the documents in the corresponding 
 * language. For an unlabeled documents the classifier compares the document 
 * vector to the language prototype vectors using the cosine similarity and 
 * chooses the most similar language prototype.
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class NearestPrototypeClassifier implements IClassifier {
    
    public static final String ENCODING = "utf-8";
    public static final String MODEL_NAME = "LanguagePrototypeClassifierModel";    
    private static final String DEFAULT_LANG = "en";
    

    private HashMap<String, double[]> languagePrototypes;
    private String[] selectedBigrams;
    
    /**
     * Create new empty nearest prototype classifier for language detection in
     * textual documents.
     */
    public NearestPrototypeClassifier() {
        this.languagePrototypes = null;
        this.selectedBigrams = null;
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
        FDistribution docsPerLanguage = new FDistribution();
        HashMap<String, FDistribution> languageFDistributions = new HashMap<>();
        
        /* Iterate over documents in the training set. */
        for (int i = 0; i < documents.length; i++) {
            String document = documents[i];
            // Extract a frequency distribution over bigrams for each document
            HashMap<String, Double> docNgramDist =
                    DocumentTools.getDocumentBigramFDistribution(document);
            String lang = labels[i];
            docsPerLanguage.update(lang, 1.0);
            // Add the frequencies to the vector of the corresponding language
            if (!languageFDistributions.containsKey(lang))
                languageFDistributions.put(lang, new FDistribution());
            languageFDistributions.get(lang).updateAll(docNgramDist);
            // Add the frequencies to the overall distribution
            bigramDist.updateAll(docNgramDist);
        }
        
        /* Sort the overall bigram distribution by frequency and select the top N. */
        List<String> bigrams = bigramDist.getSortedKeys();
        if(bigrams.size() > bigramVectorSize)
            selectedBigrams = bigrams.subList(0, bigramVectorSize).toArray(new String[bigramVectorSize]);
        else
            selectedBigrams = bigrams.toArray(new String[bigrams.size()]);
        
        languagePrototypes = new HashMap<>();
        /* Iterate over languages and for each language retain frequencies only 
           for the selected bigrams. Divide the frequencies by the number of
           documents. */
        for (String lang : docsPerLanguage.keySet()) {            
            double[] languageVector = new double[selectedBigrams.length];
            FDistribution languageFDist = languageFDistributions.get(lang);
            double numDocs = docsPerLanguage.get(lang);
            for (int i = 0; i < selectedBigrams.length; i++) {
                String ngram = bigrams.get(i);
                Double value = languageFDist.get(ngram);
                languageVector[i] = value != null ? value / numDocs : 0.0;
            }
            languagePrototypes.put(lang, languageVector);
        }
    }
    
    /**
     * Predicts the language for a given document.
     * 
     * @param document document to process
     * @return predicted language
     */
    @Override
    public String predict(String document) {
        if (languagePrototypes == null) {
            Logger.getLogger(NearestPrototypeClassifier.class.getName()).log(Level.SEVERE, "No model found.");
            return DEFAULT_LANG;
        }
        /* Create a bigram vector for the document. */
        HashMap<String, Double> docNgramDist = DocumentTools.getDocumentBigramFDistribution(document);
        double[] documentVector = new double[selectedBigrams.length];
        for (int i = 0; i < selectedBigrams.length; i++) {
            String bigram = selectedBigrams[i];
            Double value = docNgramDist.containsKey(bigram) ? docNgramDist.get(bigram) : 0.0;
            documentVector[i] = value;
        }
        /* Compare the vector with the language prototypes. */
        String predicted = DEFAULT_LANG;
        double maxCosine = -2;
        for (Map.Entry<String, double[]> entry : languagePrototypes.entrySet()) {
            double cosine = MathTools.cosine(documentVector, entry.getValue());
            if(cosine > maxCosine) {
                maxCosine = cosine;
                predicted = entry.getKey();
            }
        }
        return predicted;
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
        try(BufferedWriter out = new BufferedWriter
            (new OutputStreamWriter(new FileOutputStream(fileName), ENCODING))) {
            out.write(NearestPrototypeClassifier.MODEL_NAME);
            out.newLine();
            for (String selectedBigram : selectedBigrams) {
                out.write(selectedBigram + "\t");
            }
            out.newLine();
            for (Map.Entry<String, double[]> languageEntry : languagePrototypes.entrySet()) {
                out.write(languageEntry.getKey());
                for (double value : languageEntry.getValue()) {
                    out.write("\t" + value);
                }
                out.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(NearestPrototypeClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Loads a classifier model from file.
     * 
     * @param fileName the model file
     */
    @Override
    public void loadModel(String fileName){
        try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), ENCODING))){
            in.readLine(); // Skip the model name
            String line = in.readLine().trim();
            selectedBigrams = line.split("\t");
            languagePrototypes = new HashMap<>();
            while ((line = in.readLine()) != null){
                line = line.trim();
                String[] values = line.split("\t");
                if (values.length - 1 == selectedBigrams.length){
                    double[] languageVector = new double[selectedBigrams.length];
                    String lang = values[0];
                    for (int i = 0; i < selectedBigrams.length; i++) {
                        Double value = Double.parseDouble(values[i + 1]);
                        languageVector[i] = value;
                    }
                    languagePrototypes.put(lang, languageVector);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NearestPrototypeClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}