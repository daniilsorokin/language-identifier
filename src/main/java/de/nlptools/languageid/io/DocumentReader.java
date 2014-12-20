package de.nlptools.languageid.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class DocumentReader {
    
    public static final String ENCODING = "utf-8";
    
    /**
     * 
     * @param file
     * @return
     * @throws IOException 
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
     * 
     * @param dir
     * @return 
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

    public static Dataset readDatasetFromMetaFile(String metaFileName) {
        File metaFile = new File(metaFileName);
        if (!metaFile.isFile()) return null;
        ArrayList<String> filesList = new ArrayList<>();        
        ArrayList<String> labelsList = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(metaFile), "UTF-8"))){
            String line;
            while((line = in.readLine()) != null){
                String[] columns = line.trim().split(",");
                filesList.add(columns[0]);
                if (columns.length > 1)
                    labelsList.add(columns[1]);
            }
        } catch (IOException ex) {
            Logger.getLogger(DocumentReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<String> documentsList = new ArrayList<>();
        for (String file : filesList) {
            try {
                String content = DocumentReader.readContentFromFile(new File(file));
                documentsList.add(content);
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
