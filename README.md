language-identifier
===================

This file describes a java library for automatic language identification.
The tool can identify 68 different languages.

**Author**: Daniil Sorokin

**License**: MIT

https://github.com/daniilsorokin/language-identifier


Language identification method
------------------------------

The language identification method is based on the article by Cavnar and Trankle (1994) 
with the some modifications proposed in Baldwin and Lui (2010) and Lui and Baldwin (2011).

The language identification task is viewed as a supervised classification problem: 
an algorithm has to assign a language label to a document based on the previous
observations. The implemented method uses the statistics about the bigrams to 
identify the language of a document. Baldwin and Lui (2010) test different ngrams 
for this task and show that the bigrams is a good first choice. 

The language identifier tool implements two approaches: a simple nearest prototype
approach (NP) and an approach that uses linear SVMs (Liblinear). 

The NP classifier constructs language prototypes for each language it encounters 
in the training data. A prototype is an average frequency distribution over bigrams
for a particular language. In order to identify the language of an unlabeled document
the NP classifier compares the frequency distribution over bigrams for that documents
with the prototypes using the cosine similarity. 

The Liblinear classifier computes frequency distributions over bigrams for each
document in the training set and then uses them to train a linear SVM classifier.
This approaches employs an external Liblinear library (Fan et al. 2008).

In both cases the amount of the considered bigrams is limited to 10000 most frequent
(this number was determined by the author on a separate development set).

###Evaluation

In order to evaluate the tool, the Wikipedia dataset from Baldwin and Lui (2010)
was taken. Baldwin and Lui (2010) note that the Wikipedia dataset was the most 
difficult in their experiments. To train the classifiers and to select the parameters 
a different Wikipedia dataset from Lui and Baldwin (2011) was used (the Wikipedia A 
partition is used for training and the Wikipedia B partition for the development). 

**Results**

Development set: Wikipedia dataset from Baldwin and Lui (2011)

Test set:        Wikipedia dataset from Baldwin and Lui (2010)

NP classifier accuracy on the development set:        0.841

NP classifier accuracy on the test set:               0.767

Liblinear classifier accuracy on the development set: 0.960

Liblinear classifier accuracy on the test set:        0.783


These numbers are comparable with the results reported by Baldwin and Lui (2010) on the Wikipedia dataset.


### References
* W. B. Cavnar and J. M. Trenkle. “N-Gram-Based Text Categorization.” Proceedings of the Third Symposium on Document Analysis and Information Retrieval, 1994.
* T. Baldwin and M. Lui. “Language Identification: The Long and the Short of the Matter.” Human Language Technologies: The 11th Annual Conference of the North American Chapter of the Association for Computational Linguistics, 2010. 229–237.
* M. Lui and T. Baldwin. “Cross-Domain Feature Selection for Language Identification.” Proceedings of the 5th International Joint Conference on Natural Language Processing, 2011. 553–561.
* R.-E. Fan, K.-W. Chang, C.-J. Hsieh, X.-R. Wang, and C.-J. Lin. LIBLINEAR: A library for large linear classification Journal of Machine Learning Research 9(2008), 1871-1874.


Usage comment
-------------

The package includes pre-trained model for the NP classifiers (`NP.model`) and
the Liblinear classifier (the liblinear model consists of two files: the svm 
model `Liblinear.model` and the list of the selected bigrams `Liblinear.model.sb`).

Both models are able to identify 68 different languages.

The NP classifier doesn't depend on any external library!

In order to use the Liblinear classifier you have make sure that
the Java implementation of the Liblinear library (http://liblinear.bwaldvogel.de/)
is in the classpath.

The tool always assumes that the encoding of the input is UTF-8.

Command line use
----------------

To train an NP model: 
    java -cp language-identifier.jar de.nlptools.languageid.cl.Train -t NP -m NP.model [training_set]

To train a liblinear model: 
    java -cp language-identifier.jar:liblinear-1.94.jar de.nlptools.languageid.cl.Train -t Liblinear -m Liblinear.model [training_set]

To test a model: 
    java -cp language-identifier.jar de.nlptools.languageid.cl.Predict -m NP.model [test_set]

To predict a label of an unknown document: 
    java -cp language-identifier.jar de.nlptools.languageid.cl.Predict -m NP.model [document]

To predict a label of an unknown document using the Liblinear model: 
    java -cp language-identifier.jar:liblinear-1.94.jar de.nlptools.languageid.cl.Predict -m Liblinear.model [document]


Library use
-----------

        Dataset traing = DocumentReader.readDatasetFromFolder(metaTrain);
        
        NearestPrototypeClassifier classifier = new NearestPrototypeClassifier();
        classifier.build(train.getDocuments(), train.getLabels(), 10000);

        Dataset test = DocumentReader.readDatasetFromFolder(metaTest);
        EvaluationResult results = classifier.evaluate(test.getDocuments(), test.getLabels());
        double accuracy = results.getAccuracy();


File format for training and testing
------------------------------------

The format for training and testing data is the same as in Baldwin and Lui (2010).
The dataset should be a list of documents contained in one folder, each document name 
should start with an ISO language code separated from the rest of the name
with an underscore (e.g. `de_mydocument.txt`).
