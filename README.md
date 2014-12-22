language-identifier
===================

This file describes a java library for language identification.

**Author**: Daniil Sorokin

**License**: MIT

https://github.com/daniilsorokin/language-identifier


Language identification method
------------------------------

The language identification method is base on the article by Cavnar and Trankle (1994) 
with the modifications proposed in Baldwin and Lui (2010) and Lui and Baldwin (2011).

The language identification task is viewed as a supervised classification problem: 
an algorithm has to assign a language label to a document based on the previous
observations. The implemented method uses the statistics about the bigrams to 
identify the language of a document. Baldwin and Lui (2010) test different ngrams 
for this task and show that the bigrams is a good first choice for this task. 

The language identifier implements two approaches: a simple nearest prototype
approach (NP) and an approach that uses linear SVMs (Liblinear). 

The NP classifier constructs language prototypes for each language it encounters 
in the training data. A prototype is an average frequency distribution over bigrams
for a particular language. In order to identify the language of an unlabeled document
the NP classifier compares the frequency distribution over bigrams for that documents
with the prototypes using the cosine similarity. 

The Liblinear classifier computes frequency distributions over bigrams for each
document in the training set and then uses them to train a linear svm classifier.
This approaches employs an external Liblinear library (Fan et al. 2008).

In both cases the amount of the considered bigrams is limited to 10000 most frequent
(this number was determined on a separate development set).

###Evaluation

In order to evaluate the tool the Wikipedia dataset from Baldwin and Lui (2010)
was taken. Baldwin and Lui (2010) not that the Wikipedia dataset was the most 
difficult in their experiments. To train the classifiers and to select the parameters 
a different Wikipedia dataset from Lui and Baldwin (2011) was used (the Wikipedia A 
partition is used for training and Wikipedia B partition for the development). 

NP classifier accuracy on the Wikipedia dataset from Baldwin and Lui (2010):



Liblinear classifier accuracy on the Wikipedia dataset from Baldwin and Lui (2010):



### References
* W. B. Cavnar and J. M. Trenkle. “N-Gram-Based Text Categorization.” Proceedings of the Third Symposium on Document Analysis and Information Retrieval, 1994.
* T. Baldwin and M. Lui. “Language Identification: The Long and the Short of the Matter.” Human Language Technologies: The 11th Annual Conference of the North American Chapter of the Association for Computational Linguistics, 2010. 229–237.
* M. Lui and T. Baldwin. “Cross-Domain Feature Selection for Language Identification.” Proceedings of the 5th International Joint Conference on Natural Language Processing, 2011. 553–561.
* R.-E. Fan, K.-W. Chang, C.-J. Hsieh, X.-R. Wang, and C.-J. Lin. LIBLINEAR: A library for large linear classification Journal of Machine Learning Research 9(2008), 1871-1874.


Usage comment
-------------

The package includes pre-trained model for the NP and Liblinear classifiers.

The NP classifier doesn't depend on any external library!

In order to use the Liblinear classifier you have to download the Java implementation
of the Liblinear library here: http://liblinear.bwaldvogel.de/
Make sure it is in the classpath before running the program!

The tool always assumes that the encoding of the input is UTF-8.

Command line use
----------------

To train an NP model: 
    java -cp language-identifier.jar de.nlptools.languageid.cl.Train -m NP.model [training_set]

To train a liblinear model: 
    java -cp language-identifier.jar:liblinear-1.94.jar de.nlptools.languageid.cl.Train -t Liblinear -m Liblinear.model [training_set]

To test a model: 
    java -cp language-identifier.jar de.nlptools.languageid.cl.Predict -m NP.model [test_set]

To predict a label of an unknown document: 
    java -cp language-identifier.jar de.nlptools.languageid.cl.Predict -m NP.model [document]


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

We use the same format for training and testing data as Baldwin and Lui (2010).
The dataset should be either a list of documents contained in one folder, each 
document should start with an ISO language code separated from the rest of the name
with an underscore (eg. `de_mydocument.txt`).
