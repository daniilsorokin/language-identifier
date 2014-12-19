package de.nlptools.languageid.classifiers;

import de.nlptools.languageid.tools.FDistribution;
import de.nlptools.languageid.tools.DocumentTools;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class NearestPrototypeClassifier {
    
    public static void main(String[] args) {
        String dir = "/home/dsorokin/Downloads/ijcnlp2011-langid/";
        File[] domainFiles = new File(dir + "wikiraw/domain/").listFiles();
        
        NearestPrototypeClassifier classifier = new NearestPrototypeClassifier();
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
    
    
    public static double cosine(double[] a, double[] b) {
        if(a.length != b.length) throw new RuntimeException();
        double amagn = 0.0, bmagn = 0.0, abproduct = 0.0;
        for (int i = 0; i < a.length; i++) {
            amagn += a[i]*a[i];
            bmagn += b[i]*b[i];
            abproduct += a[i]*b[i];
        }
        amagn = Math.sqrt(amagn);
        bmagn = Math.sqrt(bmagn);
        double cosine = abproduct / (amagn * bmagn);
        return cosine;
    }    

    private HashMap<String, double[]> languagePrototypes;
    private int featureVectorSize = 10000;
    private String[] selectedBigrams;
    private String defaultLang = "en";
    
    public NearestPrototypeClassifier() {
    }
    
    /**
     * 
     * @param documents 
     */
    public void build(File[] documents) {
        FDistribution bigramDist = new FDistribution();
        FDistribution docsPerLanguage = new FDistribution();
        HashMap<String, FDistribution> languageFDistributions = new HashMap<>();
        
        for (File file : documents) {
            try {
                HashMap<String, Double> docNgramDist = 
                        DocumentTools.getDocumentBigramFDistribution(file);
                String lang = 
                        DocumentTools.getDocumentLanguageFromFileName(file);
                docsPerLanguage.update(lang, 1.0);
                if (!languageFDistributions.containsKey(lang))
                    languageFDistributions.put(lang, new FDistribution());
                languageFDistributions.get(lang).updateAll(docNgramDist);
                bigramDist.updateAll(docNgramDist);
            } catch (IOException ex) {
                Logger.getLogger(NearestPrototypeClassifier.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
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
    public String predict(File document) {
        try {
            HashMap<String, Double> docNgramDist = DocumentTools.getDocumentBigramFDistribution(document);
            double[] documentVector = new double[featureVectorSize];
            for (int i = 0; i < featureVectorSize; i++) {
                String bigram = selectedBigrams[i];
                Double value = docNgramDist.containsKey(bigram) ? docNgramDist.get(bigram) : 0.0;
                documentVector[i] = value;
            }
            String predicted = "";
            double maxCosine = -2;
            for (Map.Entry<String, double[]> entry : languagePrototypes.entrySet()) {
                double cosine = cosine(documentVector, entry.getValue());
                if(cosine > maxCosine) {
                    maxCosine = cosine;
                    predicted = entry.getKey();
                }
            }
            return predicted;
        } catch (IOException ex) {
            Logger.getLogger(NearestPrototypeClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return defaultLang;
    }
}
