package de.nlptools.languageid.classifiers;

import de.nlptools.languageid.io.Dataset;
import de.nlptools.languageid.io.DocumentReader;
import de.nlptools.languageid.tools.FDistribution;
import de.nlptools.languageid.tools.DocumentTools;
import de.nlptools.languageid.tools.MathTools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class NearestPrototypeClassifier implements IClassifier {
        
    public static void main(String[] args) {
        String dir = "C:\\Users\\Даня\\Downloads\\ijcnlp2011-langid\\ijcnlp2011-langid\\ijcnlp2011-langid\\";
        Dataset train = DocumentReader.readDatasetFromFolder(dir + "wikiraw\\domain\\");
        
        NearestPrototypeClassifier classifier = new NearestPrototypeClassifier();
        System.out.println("Building the classifier.");
        classifier.build(train.getDocuments(), train.getLabels(), 10000);

        Dataset test = DocumentReader.readDatasetFromFolder(dir + "wikiraw\\lang\\");
        System.out.println("Classifying lang documents.");
        EvaluationResult results = classifier.evaluate(test.getDocuments(), test.getLabels());
        double accuracy = results.getAccuracy();
        System.out.println("Accuracy: " + accuracy);
    }
    
    public static final String ENCODING = "utf-8";
    public static final String MODEL_NAME = "LanguagePrototypeClassifierModel";
    private static final String DEFAULT_LANG = "en";
    private HashMap<String, double[]> languagePrototypes;
    private String[] selectedBigrams;
    
    
    public NearestPrototypeClassifier() {
        this.languagePrototypes = null;
        this.selectedBigrams = null;
    }
    
    @Override
    public void build(String[] documents, String[] labels, int featureVectorSize) {
        FDistribution bigramDist = new FDistribution();
        FDistribution docsPerLanguage = new FDistribution();
        HashMap<String, FDistribution> languageFDistributions = new HashMap<>();
        
        for (int i = 0; i < documents.length; i++) {
            String document = documents[i];
            HashMap<String, Double> docNgramDist =
                    DocumentTools.getDocumentBigramFDistribution(document);
            String lang = labels[i];
            docsPerLanguage.update(lang, 1.0);
            if (!languageFDistributions.containsKey(lang))
                languageFDistributions.put(lang, new FDistribution());
            languageFDistributions.get(lang).updateAll(docNgramDist);
            bigramDist.updateAll(docNgramDist);
        }
        
        List<String> bigrams = bigramDist.getSortedKeys();
        selectedBigrams = bigrams.subList(0, featureVectorSize).toArray(new String[featureVectorSize]);
        languagePrototypes = new HashMap<>();
        for (String lang : docsPerLanguage.keySet()) {            
            double[] languageVector = new double[featureVectorSize];
            FDistribution languageFDist = languageFDistributions.get(lang);
            double numDocs = docsPerLanguage.get(lang);
            for (int i = 0; i < featureVectorSize && i < bigrams.size(); i++) {
                String ngram = bigrams.get(i);
                Double value = languageFDist.get(ngram);
                languageVector[i] = value != null ? value / numDocs : 0.0;
            }
            languagePrototypes.put(lang, languageVector);
        }
    }
    
    /**
     * 
     * @param document
     * @return 
     */
    @Override
    public String predict(String document) {
        if (languagePrototypes == null) {
            Logger.getLogger(NearestPrototypeClassifier.class.getName()).log(Level.SEVERE, "No model found.");
            return DEFAULT_LANG;
        }
        HashMap<String, Double> docNgramDist = DocumentTools.getDocumentBigramFDistribution(document);
        double[] documentVector = new double[selectedBigrams.length];
        for (int i = 0; i < selectedBigrams.length; i++) {
            String bigram = selectedBigrams[i];
            Double value = docNgramDist.containsKey(bigram) ? docNgramDist.get(bigram) : 0.0;
            documentVector[i] = value;
        }
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
        return new EvaluationResult(accuracy, 0.0, 0.0, 0.0);
    } 
    

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