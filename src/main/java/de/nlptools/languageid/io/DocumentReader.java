package de.nlptools.languageid.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains static methods to read documents from files.
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class DocumentReader {
    
    public static final String ENCODING = "utf-8";
    
    /**
     * Read the complete content of a document into a string.
     * 
     * @param file a file object
     * @return file content as a string
     * @throws IOException if there is a problem reading the given file
     */
    public static String readContentFromFile(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()), ENCODING);
        content = content.replace('\n', ' ')
                        .replace('\t', ' ');
        return content;
    }
    
    /**
     * Extracts an ISO language code from the file name. The file format should 
     * be in form LangIsoCode_DocumentID. The described format is used in 
     * the training and test data.
     * 
     * @param fileName The name of the file in question
     * @return an ISO language code
     */
    public static String getDocumentLanguageFromFileName(String fileName){
        int index = fileName.indexOf('_');
        if (index != -1 && index < 5){
            return fileName.substring(0, index);
        } else {
            return null;
        }
    }
    
    
    /**
     * Reads all documents from a folder. The methods also checks if the language 
     * labels are included in the document names. It returns a Dataset object
     * with an array that contains the contents of the documents and the array
     * of language labels if they were found.
     * 
     * @param dir a directory to read from
     * @return a Dataset object
     */
    public static Dataset readDatasetFromFolder(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.isDirectory()) return null;
        File[] files = dirFile.listFiles();
        ArrayList<String> documentsList = new ArrayList<>();
        ArrayList<String> labelsList = new ArrayList<>();
        for (File file : files) {
            try {
                String content = DocumentReader.readContentFromFile(file);
                documentsList.add(content);
                String lang = DocumentReader.getDocumentLanguageFromFileName(file.getName());
                if (lang != null) labelsList.add(lang);
            } catch (IOException ex) {
                Logger.getLogger(DocumentReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String[] documents = documentsList.toArray(new String[documentsList.size()]);
        String[] labels = {};
        if ( labelsList.size() == documentsList.size() )
            labels = labelsList.toArray(new String[labelsList.size()]);
        return new Dataset(documents, labels);
    }  
}
