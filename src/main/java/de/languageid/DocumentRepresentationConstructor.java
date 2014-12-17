package de.languageid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dsorokin
 */
public class DocumentRepresentationConstructor {
    
    public static void main(String[] args) {
            String dir = "/home/dsorokin/Downloads/naacl2010-langid/";
            Map<String,String> files = loadListFromMetaFile(dir + "EuroGOV.meta");
            HashMap<String, Integer> ngramDist= new HashMap<>();
            int counter = 0;
            HashMap<String, HashMap> documentVectors = new HashMap<>();
            for (String fileName : files.keySet()) {
                HashMap<String, Integer> docNgramDist= new HashMap<>();
                try {
                    String content = new String(Files.readAllBytes(Paths.get(dir + "EuroGOV/" + fileName)), "utf-8");
                    char previousChar = content.charAt(0);
                    for (int i = 1; i < content.length(); i++) {
                        char currentChar = content.charAt(i);
                        String bigram = new String(new char[]{previousChar, currentChar});
                        int bigramCount = docNgramDist.containsKey(bigram) ? docNgramDist.get(bigram) : 0;
                        docNgramDist.put(bigram, bigramCount + 1);
                        previousChar = currentChar;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DocumentRepresentationConstructor.class.getName()).log(Level.SEVERE, null, ex);
                }
                documentVectors.put(fileName,docNgramDist);
                counter++;
                if(counter % 500 == 0) System.out.println("Documents processed: " + counter);
                for (String ngram : docNgramDist.keySet()) {
                    int ngramCount = ngramDist.containsKey(ngram) ? ngramDist.get(ngram) : 0;
                    ngramDist.put(ngram, ngramCount + docNgramDist.get(ngram));
                }
            }
            
            System.out.println("Unique ngrams: " + ngramDist.size());
            List<Entry<String,Integer>> ngrams = new ArrayList<>(ngramDist.entrySet());
            Collections.sort(ngrams, new Comparator<Entry<String,Integer>>() {
                @Override
                public int compare(Entry<String,Integer> t1, Entry<String,Integer> t2) {
                    return (t2.getValue()).compareTo(t1.getValue());
                }
            });
            for (int i = 0; i < 10; i++) {
                System.out.print(ngrams.get(i).getKey() + ":"  + ngrams.get(i).getValue() + ", ");
            }
            ArrayList<String> languageIndex = new ArrayList<>(new HashSet<>(files.values()));
            System.out.println("\nLangIndex: " + languageIndex.size() + languageIndex);
            System.out.println("Saving vectors to file.");
            try(BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(dir + "EuroGOV-full-document-vectors.wholeset"), "utf-8"))){
                for (Entry<String, String> file : files.entrySet()) {
                    Integer langId = languageIndex.indexOf(file.getValue());
                    out.write(langId.toString());
                    HashMap<String, Integer> docNgramDist = documentVectors.get(file.getKey());
                    for (int i = 0; i < ngrams.size(); i++) {
                        String ngram = ngrams.get(i).getKey();
                        Integer value = docNgramDist.get(ngram);
                        if(value != null) out.write(" " + (i+1) + ":" + value);
                    }
                    out.newLine();
                }
            } catch(IOException ex) {
                Logger.getLogger(DocumentRepresentationConstructor.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            Logger.getLogger(DocumentRepresentationConstructor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileDictionary;
    }

}
