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
 *
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
*/
public class LiblinearClassifier implements IClassifier{
    
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
        Dataset train = DocumentReader.readDatasetFromFolder(dir + "wikiraw/domain/");

        LiblinearClassifier classifier = new LiblinearClassifier();
        classifier.setC(128.0);
        System.out.println("Building the classifier.");
        classifier.build(train.getDocuments(), train.getLabels(), 3000);

        System.out.println("Classifying lang documents.");
        Dataset test = DocumentReader.readDatasetFromFolder(dir + "wikiraw/lang/");
        System.out.println("Classifying lang documents.");
        EvaluationResult results = classifier.evaluate(test.getDocuments(), test.getLabels());
        double accuracy = results.getAccuracy();
        System.out.println("Accuracy: " + accuracy);
    }

    private String[] selectedBigrams;
    private Model model;
    private ArrayList<String> languageIndex;
    private double c;

    public LiblinearClassifier() {
        this.model = null;
        this.languageIndex = null;
        this.selectedBigrams = null;
        this.c = 1.0;
    }
    
    public void setC(double c){
        this.c = c;
    }
    
    @Override
    public void build(String[] documents, String[] labels, int featureVectorSize) {
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
        selectedBigrams = bigrams.subList(0, featureVectorSize)
                .toArray(new String[featureVectorSize]);
        
        languageIndex = new ArrayList<>(languages);
        Problem problem = new Problem();
        problem.l = documents.length;
        problem.n = featureVectorSize;
        problem.y = new double[documents.length];
        problem.x = new Feature[documents.length][];
        
        for (int i = 0; i < documents.length; i++) {
            String lang = labels[i];
            int langId = languageIndex.indexOf(lang);
            problem.y[i] = langId;
            HashMap<String, Double> docNgramDist = documentVectors.get(i);
            List<Feature> vector = new ArrayList<>();
            for (int j = 0; j < featureVectorSize; j++) {
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
        try {
            this.model.save(new File(fileName));
        } catch (IOException ex) {
            Logger.getLogger(LiblinearClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void loadModel(String fileName){
        try {
            this.model = Model.load(new File(fileName));
        } catch (IOException ex) {
            Logger.getLogger(LiblinearClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
