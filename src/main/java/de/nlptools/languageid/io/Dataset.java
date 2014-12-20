package de.nlptools.languageid.io;

/**
 * Dataset object contains along the content of the documents the list of
 * the corresponding language labels. 
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class Dataset {
    
    private String[] documents;
    private String[] labels;

    public Dataset(String[] documents, String[] labels) {
        this.documents = documents;
        this.labels = labels;
    }

    public String[] getDocuments() {
        return documents;
    }

    public String[] getLabels() {
        return labels;
    }
    
    public boolean hasLabels(){
        return labels == null || labels.length == 0;
    }
}
