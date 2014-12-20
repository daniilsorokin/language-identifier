package de.nlptools.languageid.classifiers;

/**
 * Classifier interface.
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public interface IClassifier {
    
    public void build(String[] documents, String[] labels, int featureVectorSize);
    public String predict(String document);
    public EvaluationResult evaluate(String[] testDocuments, String[] goldLabels);
    public void saveModel(String fileName);
    public void loadModel(String fileName);
}
