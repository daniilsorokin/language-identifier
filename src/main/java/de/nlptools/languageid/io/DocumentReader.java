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
        return fileName.substring(0, fileName.indexOf('_'));
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
                labelsList.add(lang);
            } catch (IOException ex) {
                Logger.getLogger(DocumentReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String[] documents = documentsList.toArray(new String[documentsList.size()]);
        String[] labels = labelsList.toArray(new String[labelsList.size()]);
        return new Dataset(documents, labels);
    }
    
    
    public static void loadMetaFile(String file) throws IOException {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(file), "UTF-8"))){
            TreeMap<String,Set<String>> fileDictionary = new TreeMap<>();
            String line;
            while((line = in.readLine()) != null){
                String[] columns = line.trim().split("\t");
                if(fileDictionary.containsKey(columns[2])) {
                    fileDictionary.get(columns[2]).add(columns[0]);
                } else {
                    HashSet<String> tmp = new HashSet<>();
                    tmp.add(columns[0]);
                    fileDictionary.put(columns[2], tmp);
                }
            }
        }
    }

    public static Map<String, String> loadListFromMetaFile(String file) {
        TreeMap<String,String> fileDictionary = new TreeMap<>();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(file), "UTF-8"))){
            String line;
            while((line = in.readLine()) != null){
                String[] columns = line.trim().split("\t");
                fileDictionary.put(columns[0], columns[2]);
            }
        } catch (IOException ex) {
            Logger.getLogger(DocumentReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileDictionary;
    }    
    
}
