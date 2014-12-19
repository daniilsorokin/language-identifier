package de.nlptools.languageid.classifiers;

/**
 *
 * @author dsorokin
 */
public class EvaluationResult {
    
    private double accuracy;
    private double microPrecision;
    private double microRecall;
    private double microFscore;

    public EvaluationResult(double accuracy, double microPrecision, double microRecall, double microFscore) {
        this.accuracy = accuracy;
        this.microPrecision = microPrecision;
        this.microRecall = microRecall;
        this.microFscore = microFscore;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getMicroFscore() {
        return microFscore;
    }

    public double getMicroPrecision() {
        return microPrecision;
    }

    public double getMicroRecall() {
        return microRecall;
    }
}
