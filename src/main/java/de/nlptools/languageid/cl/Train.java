package de.nlptools.languageid.cl;

import de.nlptools.languageid.classifiers.IClassifier;
import de.nlptools.languageid.classifiers.LiblinearClassifier;
import de.nlptools.languageid.classifiers.NearestPrototypeClassifier;
import de.nlptools.languageid.io.Dataset;
import de.nlptools.languageid.io.DocumentReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to train a classifier from the command line.
 * 
 * @author Daniil Sorokin <daniil.sorokin@uni-tuebingen.de>
 */
public class Train {

    public static void main(String[] args) {
        HashMap<String, String> parameters = readClArgs(args);
        if (parameters == null) {
            System.out.println("Unknown set of option.");
            Train.printHelp();
            System.exit(1);
        }

        IClassifier classifier = null;
        String classifierType = parameters.containsKey(Train.CLASSIFIER) ?
                parameters.get(Train.CLASSIFIER) : DEFAULT_CLASSIFIER;
        if (classifierType.equals("Liblinear")){
            try {
                Class.forName( "de.bwaldvogel.liblinear.Linear" );
                LiblinearClassifier liblinear = new LiblinearClassifier();
                if(parameters.containsKey(Train.C_PARAMETER)){
                    double cost = Double.parseDouble(parameters.get(Train.C_PARAMETER));
                    liblinear.setC(cost);
                }
                classifier = liblinear;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(LiblinearClassifier.class.getName())
                        .log(Level.SEVERE, "Liblinear is not found! This "
                                + "classifier makes use of an external LibLinear library. "
                                + "\n Please check that the liblinear-1.94.jar is in the classpath.");
                System.exit(1);
            } catch ( NumberFormatException ex){
                Logger.getLogger(LiblinearClassifier.class.getName())
                        .log(Level.SEVERE, "C parameter must be a number.");
                System.exit(1);
            }
        } else if (classifierType.equals("NP")) {
            classifier = new NearestPrototypeClassifier();
        } else {
            System.out.println("Wrong classifier name.");
            Train.printHelp();
            System.exit(1);
        }
        
        System.out.println("Reading the data.");
        String trainingSet = parameters.get(Train.DOCS_SET);
        Dataset train = DocumentReader.readDatasetFromFolder(trainingSet);
        
        
        int numFeatures = 10000;
        if(parameters.containsKey(Train.NUM_FEATURES))
            numFeatures = Integer.parseInt(parameters.get(Train.NUM_FEATURES));

        System.out.println("Building the classifier.");
        classifier.build(train.getDocuments(), train.getLabels(), numFeatures);

        String modelFile = parameters.containsKey(Train.MODEL_FILE) ?
                parameters.get(Train.MODEL_FILE) : classifierType + ".model";

        System.out.println("Saving the model.");
        classifier.saveModel(modelFile);
    }
    
    public static final String NP_CLASSIFIER = "NP";
    public static final String LIBLINEAR_CLASSIFIER = "Liblinear";
    public static final String DEFAULT_CLASSIFIER = NP_CLASSIFIER;
    
    public static final String NUM_FEATURES = "numFeatures";
    public static final String CLASSIFIER = "classifier";
    public static final String C_PARAMETER = "cParameter";
    public static final String MODEL_FILE = "modelFile";
    public static final String DOCS_SET = "documentsSet";
    
    /**
     * Read the command line arguments.
     * 
     * @param args command line arguments
     * @return parameters as a map
     */
    public static HashMap<String, String> readClArgs(String[] args){
        if (args.length == 0) return null;
        HashMap<String, String> parameters = new HashMap<>();
        int i = 0;
        char dash = args[i].charAt(0);
        while(dash == '-' && i < args.length) {
            char flag = args[i].charAt(1);
            switch(flag) {
                case 't':
                    parameters.put(CLASSIFIER, args[i+1]);
                    break;
                case 'c':
                    parameters.put(C_PARAMETER, args[i+1]);
                    break;
                case 'm':
                    parameters.put(MODEL_FILE, args[i+1]);
                    break;
                case 'n':
                    parameters.put(NUM_FEATURES, args[i+1]);
                    break;
                default:
                    return null;
            }
            i += 2;
            dash = args[i].charAt(0);
        }        
        if (args.length <= i) return null;
        parameters.put(DOCS_SET, args[i]);
        return parameters;
    }
    
    public static void printHelp(){
        System.out.printf("Usage: de.nlptools.languageid.cl.Train [options] trainingSet %n"
                + "trainingSet :  a folder name that contains the documents %n"
                + "               (see README for the training set format description)."
                + "Options: %n"
                + "    -t type      : type of the classifier: %n"
                + "              NP -- nearest prototype classifier (default) %n"
                + "       Liblinear -- SVM based classifier (requires the external Liblinear library) %n%n"
                + "    -m modelFile : the name of the file to save the model (default: [classifier_type].model) %n"
                + "    -c cost      : cost parameter for the Liblinear classifier (default: 1.0) %n"
                + "    -n number    : number of bigrams to select for document representation (default: 10000) %n"
        );
    }
    
}
