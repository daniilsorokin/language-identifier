package de.nlptools.languageid.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class DocumentReaderTest {
    public static final String ENCODING = "utf-8";
    private static String[] files = new String[]{"test-folder/en_dummy1", 
                                                 "test-folder/en_dummy2", 
                                                 "test-folder/de_dummy3"};
    private static String metafile = "metafile.txt";
    private static String folder = "test-folder";
    private static String content = "This is a test document.";

    @BeforeClass
    public static void setUpClass() {
        File folderFile = new File(folder);
        folderFile.mkdir();
        for(String file : files){
            try(BufferedWriter out = new BufferedWriter
                    (new OutputStreamWriter(new FileOutputStream(file), ENCODING))) {
                out.write(content);
            } catch (IOException ex) {
                Logger.getLogger(DocumentReaderTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try(BufferedWriter out = new BufferedWriter
                            (new OutputStreamWriter(new FileOutputStream(metafile), ENCODING))) {
            for (String file : files) {
                if(file.contains("en"))
                    out.write(file + "," + "en");
                else 
                    out.write(file + "," + "de");
                out.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(DocumentReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException {
        for(String file : files){
            Files.delete(Paths.get(file));
        }
        Files.delete(Paths.get(folder));
        Files.delete(Paths.get(metafile));
    }

    /**
     * Test of readContentFromFile method, of class DocumentReader.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testReadContentFromFile() throws IOException {
        System.out.println("Test readContentFromFile() method.");
        File file = new File(files[0]);
        String result = DocumentReader.readContentFromFile(file);
        assertEquals(content, result);
    }

    /**
     * Test of getDocumentLanguageFromFileName method, of class DocumentReader.
     */
    @Test
    public void testGetDocumentLanguageFromFileName() {
        System.out.println("Test getDocumentLanguageFromFileName() method.");
        String name = "bg_testfilename.txt";
        String expResult = "bg";
        String result = DocumentReader.getDocumentLanguageFromFileName(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of readDatasetFromFolder method, of class DocumentReader.
     */
    @Test
    public void testReadDatasetFromFolder() {
        System.out.println("Test readDatasetFromFolder() method.");
        String[] documents = new String[]{content, content, content};
        String[] langs = new String[]{"en", "en", "de"};
        Dataset expResult = new Dataset(documents, langs);
        Dataset result = DocumentReader.readDatasetFromFolder(folder);
        assertEquals(expResult, result);
    }

    /**
     * Test of readDatasetFromMetaFile method, of class DocumentReader.
     */
    @Test
    public void testReadDatasetFromMetaFile() {
        System.out.println("Test readDatasetFromMetaFile() method.");
        String[] documents = new String[]{content, content, content};
        String[] langs = new String[]{"en", "en", "de"};
        Dataset expResult = new Dataset(documents, langs);
        Dataset result = DocumentReader.readDatasetFromMetaFile(metafile);
        assertEquals(expResult, result);
    }
    
}
