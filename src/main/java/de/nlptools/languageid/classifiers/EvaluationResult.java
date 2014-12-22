package de.nlptools.languageid.classifiers;

/**
 * The class is used to store results of a classifier evaluation.
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class EvaluationResult {
    
    private double accuracy;

    public EvaluationResult(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getAccuracy() {
        return accuracy;
    }
}
