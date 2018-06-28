[![Build Status](https://travis-ci.org/ToolAutomaticSum/MOTS.svg?branch=master)](https://travis-ci.org/ToolAutomaticSum/MOTS)
[![Docs](https://img.shields.io/badge/java-docs-yellow.svg)](https://toolautomaticsum.github.io/MOTS/)
[![Wiki](https://img.shields.io/badge/MOTS-wiki-orange.svg)](https://github.com/ToolAutomaticSum/MOTS/wiki)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# MOTS

MOTS (MOdular Tool for Summarization) is a summarization system, written in Java. It is as modular as possible, and is intended to provide an architecture to implement and test new summarization methods, as well as to ease comparison with already implemented methods, in an unified framework. This system is the first completely modular system for automatic summarization  and already allows to summarize using more than a hundred combinations of modules. The need for such a system is important. Indeed, several evaluation campaigns exist in AS field, but summarization algorithms are not easy to compare due to the large variety of pre and post-processings they use.

## Getting Started

* [Javadoc](https://toolautomaticsum.github.io/MOTS/) available.
* We provide an example corpus from TAC2009 in /src/main/resources and its associated human summaries.

### Prerequisites

* [Maven](https://maven.apache.org/) - Dependency Manager
* glpk-utils in order to use ILP (sudo apt-get install glpk-utils)
* At least python3 in order to use WordEmbeddings, make sure to have an updated version of pip3 (sudo pip3 install --upgrade pip)
* [gensim](https://radimrehurek.com/gensim/) in order to use WordEmbeddings (pip3 install gensim --user)
* [jep](https://github.com/ninia/jep) in order to use WordEmbeddings (pip3 install jep --user)


### Installing

* You might define $CORPUS_DATA to your DUC/TAC folder.
* Install ROUGE :
	* Define $ROUGE_HOME to your ROUGE installation folder (or to ./ROUGE-1.5.5/RELEASE-1.5.5).
	* Install XML::DOM module in order to use ROUGE perl script. (sudo cpan install XML::DOM).
	* Run ./install_rouge.sh
	or :
		* Define $ROUGE_EVAL_HOME to $ROUGE_HOME/data.
		* Recreate database :
		```
		cd data/WordNet-2.0-Exceptions/
		rm WordNet-2.0.exc.db # only if exist
		perl buildExeptionDB.pl . exc WordNet-2.0.exc.db

		cd ..
		rm WordNet-2.0.exc.db # only if exist
		ln -s WordNet-2.0-Exceptions/WordNet-2.0.exc.db WordNet-2.0.exc.db
		```
* Run install.sh script.

### Usage

MOTS is a command line tool than can be used like this :

```
./MOTS mots.X.Y.Z.jar -c <config_file> -m <multicorpus_file> -v <OPTIONAL>
```

MOTS script encapsulate some environnement variable needed for the execution of WordEmbeddings. If you don't use WordEmbeddings you could launch via :

```
java -jar mots.X.Y.Z.jar -c <config_file> -m <multicorpus_file> -v <OPTIONAL>
```

Example config file and multicorpus file are provided in /conf but should be adapted to your setup.

## Go Deeper

Each summarization process is defined in a configuration file and the test corpus is defined in a multicorpus configuration file.

### Process configuration

Example for LexRank_MMR configuration file :
```
<CONFIG>
	<TASK ID="1">
		<LANGUAGE>english</LANGUAGE>
		<OUTPUT_PATH>doc/output</OUTPUT_PATH>
		<MULTITHREADING>true</MULTITHREADING>		
		<PREPROCESS NAME="GenerateTextModel">
			<OPTION NAME="StanfordNLP">true</OPTION>
			<OPTION NAME="StopWordListFile">$CORPUS_DATA/stopwords/englishStopWords.txt</OPTION>
		</PREPROCESS>
		<PROCESS>
			<OPTION NAME="CorpusIdToSummarize">all</OPTION>
			<OPTION NAME="ReadStopWords">false</OPTION>
			<INDEX_BUILDER NAME="TF_IDF.TF_IDF">
			</INDEX_BUILDER>
			<CARACTERISTIC_BUILDER NAME="vector.TfIdfVectorSentence">
			</CARACTERISTIC_BUILDER>
			<SCORING_METHOD NAME="graphBased.LexRank">
				<OPTION NAME="DampingParameter">0.15</OPTION>
				<OPTION NAME="GraphThreshold">0.1</OPTION>
				<OPTION NAME="SimilarityMethod">JaccardSimilarity</OPTION>
			</SCORING_METHOD>
			<SUMMARIZE_METHOD NAME="MMR">
				<OPTION NAME="CharLimitBoolean">true</OPTION>
				<OPTION NAME="Size">200</OPTION>
				<OPTION NAME="SimilarityMethod">JaccardSimilarity</OPTION>
				<OPTION NAME="Lambda">0.6</OPTION>
			</SUMMARIZE_METHOD>
		</PROCESS>
		<ROUGE_EVALUATION>
			<ROUGE_MEASURE>ROUGE-1	ROUGE-2	ROUGE-SU4</ROUGE_MEASURE>
			<MODEL_ROOT>models</MODEL_ROOT>
			<PEER_ROOT>systems</PEER_ROOT>
		</ROUGE_EVALUATION>
	</TASK>
</CONFIG>
```

* <b>\<CONFIG\></b> is the root node.
	* <b>\<TASK\></b> represent a summarization task. You could do multiple in a simple run. At start, stick with one.
		* <b>\<LANGUAGE\></b> is the input's document language for preprocessing goal. (english / french for now) 
		* <b>\<OUTPUT_PATH\></b> is the forlder's output path of the system. It is used to save preprocessed documents, ROUGE xml generated file, old score, ...
		* <b>\<MULTITHREADING\></b> (boolean) launch the system in a mutltithreading way or not.
		* <b>\<PREPROCESS\></b> is the preprocess step for the system. The preprocess java class to use is pass by the name variable. Here it's GenerateTextModel. It also needs two <b>\<OPTION\></b> :
			* <b>\<OPTION NAME="StanfordNLP"\></b> (boolean), true if you want to use StanfordNLP pipeline and tool to do the preprocessing.
			* <b>\<OPTION NAME="StopWordListPath"\></b> (String), path of the stopwords list you want to use.
		* <b>\<PROCESS\></b> is the main step of the system. It should have at least one <b>\<SUMMARIZE_METHOD\></b> node and two <b>\<OPTION\></b> It often has an <b>\<INDEX_BUILDER\></b> node and a <b>\<CARACTERISTIC_BUILDER\></b> node :
			* <b>\<OPTION NAME="CorpusIdToSummarize"\></b> (String as a list of int separated by \t), the list of CorpusId to summarize from the MultiCorpus configuration file. "all" will do summarization for all corpus.
			* <b>\<OPTION NAME="ReadStopWords"\></b> (boolean), state if the system count stopwords as part of the texts or not.
			* <b>\<INDEX_BUILDER\></b> is the step where the system generate a computer friendly representation of each text's textual unit. (TF-IDF, Bigram, WordEmbeddings, ...)
			* <b>\<CARATERISTIC_BUILDER\></b> is the sentence caracteristic generation step based on the textual unit index building.
			* <b>\<SCORING_METHOD\></b> weights each sentences. 
			* <b>\<SUMMARIZE_BUILDER\></b> generate a summary usually by ranking sentence based on their score.
		* <b>\<ROUGE_EVALUATION\></b> is the ROUGE evaluation step. For detail, look at ROUGE readme in /lib/ROUGE folder.
			* <b>\<ROUGE_MEASURE\></b> (String as a list of int separated by \t), represent the list of ROUGE measure you want to use.
			* <b>\<MODEL_ROOT\></b> is the model's folder name for ROUGE xml input files. 
			* <b>\<PEER_ROOT\></b> is the peer's folder name for ROUGE xml input files. 

The <b>\<PROCESS\></b> node is the system's core and you should look for more detail in the javadoc and the source code of the different INDEX_BUILDER, CARACTERISTIC_BUILDER, SCORING_METHOD and SUMMARIZE_METHOD class.

### Multicorpus configuration

```
<?xml version="1.0" encoding="UTF-8"?>
<CONFIG>
	<TASK ID="1">
		<MULTICORPUS ID="0">
			<CORPUS ID="0">
				<INPUT_PATH>$CORPUS_DATA/TAC2009/UpdateSumm09_test_docs_files/D0901A/D0901A-A</INPUT_PATH>
				<DOCUMENT ID="0">.*</DOCUMENT>
				<SUMMARY_PATH>$CORPUS_DATA/TAC2009/UpdateSumm09_eval/ROUGE/models</SUMMARY_PATH>
				<SUMMARY ID="0">D0901-A.*</SUMMARY>
			</CORPUS>
		</MULTICORPUS>
	</TASK>
</CONFIG>
```

For now, all ID are useless and could be avoided.

* <b>\<CONFIG\></b> is the root node.
	* <b>\<TASK\></b> represent a summarization task. You could do multiple in a simple run. At start, stick with one.
		* <b>\<MULTICORPUS\></b> is a list of <b>\<CORPUS\></b>
			* <b>\<CORPUS\></b> can be one or more documents. The system will generate one summary per corpus.
				* <b>\<INPUT_PATH\></b> is the folder containing the corpus' documents.
				* <b>\<DOCUMENT\></b> is the regex for the documents you want to load. You could use multiple <b>\<DOCUMENT\></b> node.
				* <b>\<SUMMARY_PATH\></b> is the human summaries folder path.
				* <b>\<SUMMARY\></b> is the regex for human summary file associating to this corpus. You could use multiple <b>\<SUMMARY\></b> node.
				 
## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Valentin Nyzam** - [ValNyz](https://github.com/ValNyz)

## License

This project is licensed under the GPL3 License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Thanks to Aurélien Bossard, my PhD supervisor.
