package de.nlptools.languageid.classifiers;

import de.nlptools.languageid.io.Dataset;
import de.nlptools.languageid.io.DocumentReader;
import de.nlptools.languageid.tools.FDistribution;
import de.nlptools.languageid.tools.DocumentTools;
import de.nlptools.languageid.tools.MathTools;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class NearestPrototypeClassifier implements IClassifier{
    
    public static void main(String[] args) {
        String dir = "/home/dsorokin/Downloads/ijcnlp2011-langid/";
        Dataset train = DocumentReader.readDatasetFromFolder(dir + "wikiraw/domain/");
        
        NearestPrototypeClassifier classifier = new NearestPrototypeClassifier(10000);
        System.out.println("Building the classifier.");
        classifier.build(train.getDocuments(), train.getLabels());

        Dataset test = DocumentReader.readDatasetFromFolder(dir + "wikiraw/lang/");
        System.out.println("Classifying lang documents.");
        EvaluationResult results = classifier.evaluate(test.getDocuments(), test.getLabels());
        double accuracy = results.getAccuracy();
        System.out.println("Accuracy: " + accuracy);
    }

    private HashMap<String, double[]> languagePrototypes;
    private int featureVectorSize;
    private String[] selectedBigrams;
    private String defaultLang;
    
    public NearestPrototypeClassifier(int numBiGrams) {
        this.languagePrototypes = null;
        this.selectedBigrams = null;
        this.featureVectorSize = numBiGrams;
        this.defaultLang = "en";
    }
    
    @Override
    public void build(String[] documents, String[] labels) {
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
        HashMap<String, Double> docNgramDist = DocumentTools.getDocumentBigramFDistribution(document);
        double[] documentVector = new double[featureVectorSize];
        for (int i = 0; i < featureVectorSize; i++) {
            String bigram = selectedBigrams[i];
            Double value = docNgramDist.containsKey(bigram) ? docNgramDist.get(bigram) : 0.0;
            documentVector[i] = value;
        }
        String predicted = defaultLang;
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
}
