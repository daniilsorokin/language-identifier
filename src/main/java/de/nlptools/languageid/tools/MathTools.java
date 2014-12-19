package de.nlptools.languageid.tools;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class MathTools {
    
    /**
     * Computes the cosine similarity between two vectors of the same size.
     * 
     * @param a first vectors
     * @param b second vector
     * @return Cosine similarity value
     */
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
