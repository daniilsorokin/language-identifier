package de.nlptools.languageid.io;

import java.util.Arrays;

/**
 * Dataset object contains along the content of the documents the list of
 * the corresponding language labels. 
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class Dataset {
    
    private String[] documents;
    private String[] labels;

    /**
     * Creates a new Dataset object with the given documents and labels. The labels
     * array may be null or empty.
     * 
     * @param documents
     * @param labels 
     */
    public Dataset(String[] documents, String[] labels) {
        this.documents = documents;
        this.labels = labels;
    }

    /**
     * @return array of the documents
     */
    public String[] getDocuments() {
        return documents;
    }

    /**
     * @return array of language labels
     */
    public String[] getLabels() {
        return labels;
    }
    
    /**
     * checks if the dataset contains the information about the language labels.
     * 
     * @return true if there are labels for the stored documents, false otherwise
     */
    public boolean hasLabels(){
        return labels == null || labels.length == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Dataset other = (Dataset) obj;
        if (!Arrays.deepEquals(this.documents, other.documents)) {
            return false;
        }
        if (!Arrays.deepEquals(this.labels, other.labels)) {
            return false;
        }
        return true;
    }
    
    
}
