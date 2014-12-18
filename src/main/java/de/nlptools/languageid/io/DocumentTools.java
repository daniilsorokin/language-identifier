package de.nlptools.languageid.io;

import de.nlptools.languageid.FDistribution;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class DocumentTools {
    
    public static final String ENCODING = "utf-8";
    
    /**
     * Extracts an ISO language code from the file name. The file format should 
     * be in form LangIsoCode_DocumentID. The described format is used in 
     * the training and test data.
     * 
     * @param file File object representing the file in question
     * @return an ISO language code
     */
    public static String getDocumentLanguageFromFileName(File file){
        String fileName = file.getName();
        return fileName.substring(0, fileName.indexOf('_'));
    }
    
    /**
     * Computes a frequency distribution of bigrams in a given docuemnts.
     * 
     * @param file File object that contains the document to process
     * @return unordered HashMap where the key is a bigram and the value is 
     * its frequency in the given document 
     * @throws IOException If the content of the file can't be read
     */
    public static HashMap<String, Integer> getDocumentBigramFDistribution(File file) throws IOException {
        FDistribution bigramDist = new FDistribution();
        String content = new String(Files.readAllBytes(file.toPath()), ENCODING);
        char previousChar = content.charAt(0);
        for (int i = 1; i < content.length(); i++) {
            char currentChar = content.charAt(i);
            String bigram = new String(new char[]{ previousChar, currentChar });
            bigramDist.update(bigram, 1);
//            int bigramCount = bigramDist.containsKey(bigram) ? bigramDist.get(bigram) : 0;
//            bigramDist.put(bigram, bigramCount + 1);
            previousChar = currentChar;
        }
        return bigramDist;
    }
}
