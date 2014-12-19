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
 * @author dsorokin
 */
public class NearestNeighbourClassifier {
    
    public static void main(String[] args) {
        String dir = "/home/dsorokin/Downloads/ijcnlp2011-langid/";
        File[] domainFiles = new File(dir + "wikiraw/domain/").listFiles();
        
        FDistribution bigramDist= new FDistribution();
        int counter = 0;
        HashMap<String, HashMap> documentVectors = new HashMap<>();
        FDistribution docsPerLanguage = new FDistribution();
        System.out.println("Processing domain files.");
        for (File file : domainFiles) {
            HashMap<String, Double> docNgramDist = new HashMap<>();
            try {
                docNgramDist = DocumentTools.getDocumentBigramFDistribution(file);
            } catch (IOException ex) {
                Logger.getLogger(NearestNeighbourClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            String lang = DocumentTools.getDocumentLanguageFromFileName(file);
            docsPerLanguage.update(lang, 1.0);
//            int documentCount = languages.containsKey(lang) ? languages.get(lang) : 0;
//            languages.put(lang, documentCount + 1);
            documentVectors.put(file.getName(),docNgramDist);
            counter++;
            if(counter % 500 == 0) System.out.println("Documents processed: " + counter);
            bigramDist.updateAll(docNgramDist);
//            for (String ngram : docNgramDist.keySet()) {
//                int ngramCount = bigramDist.containsKey(ngram) ? bigramDist.get(ngram) : 0;
//                bigramDist.put(ngram, ngramCount + docNgramDist.get(ngram));
//            }
        }
        
        System.out.println("Unique ngrams: " + bigramDist.size());
        List<String> ngrams = bigramDist.getSortedKeys();
//        List<Map.Entry<String,Integer>> ngrams = new ArrayList<>(bigramDist.entrySet());
//        Collections.sort(ngrams, new Comparator<Map.Entry<String,Integer>>() {
//            @Override
//            public int compare(Map.Entry<String,Integer> t1, Map.Entry<String,Integer> t2) {
//                return (t2.getValue()).compareTo(t1.getValue());
//            }
//        });
        for (int i = 0; i < 10; i++) {
            System.out.print(ngrams.get(i) + ":"  + bigramDist.get(ngrams.get(i)) + ", ");
        }
        System.out.println("\nLangIndex: " + docsPerLanguage.size() + docsPerLanguage);
        
        int featureVectorSize = 3000;
        
        HashMap<String, double[]> languageVectors = new HashMap<>();
        for (String lang : docsPerLanguage.keySet()) {
            languageVectors.put(lang, new double[featureVectorSize]);
        }
        
        System.out.println("Merging data into language prototype vectors");
        for (File file : domainFiles) {
            String lang = file.getName().substring(0, file.getName().indexOf('_'));
            double[] languagevector = languageVectors.get(lang);
            HashMap<String, Integer> docNgramDist = documentVectors.get(file.getName());
            for (int i = 0; i < featureVectorSize && i < ngrams.size(); i++) {
                String ngram = ngrams.get(i);
                Integer value = docNgramDist.get(ngram);
                if(value != null) languagevector[i] =  languagevector[i] + value;
            }
        }
        for (String lang : docsPerLanguage.keySet()) {
            double[] languagevector = languageVectors.get(lang);
            double docCount = docsPerLanguage.get(lang);
            for (int i = 0; i < featureVectorSize; i++) {
                double value = languagevector[i];
                languagevector[i] = value / docCount;
            }
        }
//        System.out.println("Example language vectors: " + Arrays.toString(languageVectors.get("en")));

        counter = 0;
        File[] langFiles = new File(dir + "wikiraw/lang/").listFiles();
        System.out.println("Processing lang documents.");
        for (File file : langFiles) {
            HashMap<String, Double> docNgramDist = new HashMap<>();
            try {
                docNgramDist = DocumentTools.getDocumentBigramFDistribution(file);
            } catch (IOException ex) {
                Logger.getLogger(NearestNeighbourClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            documentVectors.put(file.getName(),docNgramDist);
            counter++;
            if(counter % 500 == 0) System.out.println("Documents processed: " + counter);
        }
        
        System.out.println("Classify lang documents.");
        int correctlyClassified = 0;
        int numberOfDocuments = langFiles.length;
        for (File file : langFiles) {
            String goldLang = file.getName().substring(0, file.getName().indexOf('_'));
            HashMap<String, Integer> docNgramDist = documentVectors.get(file.getName());
            double[] documentVector = new double[featureVectorSize];
            for (int i = 0; i < featureVectorSize && i < ngrams.size(); i++) {
                String ngram = ngrams.get(i);
                Double value = docNgramDist.containsKey(ngram) ? docNgramDist.get(ngram) : 0.0;
                documentVector[i] = value;
            }
            String predicted = "";
            double maxCosine = -2;
            for (Map.Entry<String, double[]> entry : languageVectors.entrySet()) {
                double cosine = cosine(documentVector, entry.getValue());
                if(cosine > maxCosine) {
                    maxCosine = cosine;
                    predicted = entry.getKey();
                }
            }
            if (predicted.equals(goldLang)) correctlyClassified++;
        }
        double accuracy = (double) correctlyClassified / (double) numberOfDocuments;
        System.out.println("Accuracy: " + accuracy);

    }

    public NearestNeighbourClassifier() {
    }
    
    public void build(File[] documents){
        FDistribution bigramDist = new FDistribution();
        HashMap<String, HashMap> documentVectors = new HashMap<>();
        FDistribution docsPerLanguage = new FDistribution();
        for (File file : documents) {
            HashMap<String, Double> docNgramDist = new HashMap<>();
            try {
                docNgramDist = DocumentTools.getDocumentBigramFDistribution(file);
            } catch (IOException ex) {
                Logger.getLogger(NearestNeighbourClassifier.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            String lang = DocumentTools.getDocumentLanguageFromFileName(file);
            docsPerLanguage.update(lang, 1.0);
            documentVectors.put(file.getName(),docNgramDist);
            bigramDist.updateAll(docNgramDist);
        }        
        
        List<String> ngrams = bigramDist.getSortedKeys();
        int featureVectorSize = 3000;
        
        HashMap<String, double[]> languageVectors = new HashMap<>();
        for (String lang : docsPerLanguage.keySet()) {
            languageVectors.put(lang, new double[featureVectorSize]);
        }
        
        System.out.println("Merging data into language prototype vectors");
        for (File file : documents) {
            String lang = file.getName().substring(0, file.getName().indexOf('_'));
            double[] languagevector = languageVectors.get(lang);
            HashMap<String, Integer> docNgramDist = documentVectors.get(file.getName());
            for (int i = 0; i < featureVectorSize && i < ngrams.size(); i++) {
                String ngram = ngrams.get(i);
                Integer value = docNgramDist.get(ngram);
                if(value != null) languagevector[i] =  languagevector[i] + value;
            }
        }
        for (String lang : docsPerLanguage.keySet()) {
            double[] languagevector = languageVectors.get(lang);
            double docCount = docsPerLanguage.get(lang);
            for (int i = 0; i < featureVectorSize; i++) {
                double value = languagevector[i];
                languagevector[i] = value / docCount;
            }
        }
        
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
}
