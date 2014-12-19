package de.nlptools.languageid.tools;

import java.util.HashMap;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class DocumentTools {
    
    /**
     * Computes a frequency distribution of bigrams in a given document.
     * 
     * @param content the content of the document to process
     * @return unordered HashMap where the key is a bigram and the value is 
     * its frequency in the given document 
     */
    public static HashMap<String, Double> getDocumentBigramFDistribution(String content)  {
        FDistribution bigramDist = new FDistribution();
        char previousChar = content.charAt(0);
        for (int i = 1; i < content.length(); i++) {
            char currentChar = content.charAt(i);
            String bigram = new String(new char[]{ previousChar, currentChar });
            bigramDist.update(bigram, 1.0);
            previousChar = currentChar;
        }
        return bigramDist;
    }
}
