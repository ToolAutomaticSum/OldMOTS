# MOTS

MOTS (MOdular Tool for Summarization) is a summarization system, written in Java. It is as modular as possible, and is intended to provide an architecture to implement and test new summarization methods, as well as to ease comparison with already implemented methods, in an unified framework. This system is the first completely modular system for automatic summarization  and already allows to summarize using more than a hundred combinations of modules. The need for such a system is important. Indeed, several evaluation campaigns exist in AS field, but summarization algorithms are not easy to compare due to the large variety of pre and post-processings they use.

## Getting Started

[Javadoc](https://toolautomaticsum.github.io/Tool/) available.

### Prerequisites

* [Stanford Core NLP library](https://stanfordnlp.github.io/CoreNLP/)
* [Stanford Core NLP English Model](https://stanfordnlp.github.io/CoreNLP/)
* At least python 2.7 in order to use WordEmbeddings
* [gensim](https://radimrehurek.com/gensim/) in order to use WordEmbeddings


### Installing

* Define $JAVA_HOME to your java home folder.
* Define $STANFORD_NLP_HOME to your STANFORD library's installation folder.
* You might define $CORPUS_DATA to your DUC/TAC folder.
* Run install.sh script.

### Usage

MOTS is a command line tool than can be used like this :
Say what the step will be

```
java -jar mots.X.Y.Z.jar -c <config_file> -m <multicorpus_file>
```

Example config file and multicorpus file are provided in /conf but should be adapted to your setup.

## Go Deeper

Each summarization process is defined in a configuration file and the test corpus is defined in a multicorpus configuration file.

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
			<ROUGE-MEASURE>ROUGE-1	ROUGE-2	ROUGE-SU4</ROUGE-MEASURE>
			<ROUGE-PATH>lib/ROUGE-1.5.5/RELEASE-1.5.5</ROUGE-PATH>
			<MODEL-ROOT>models</MODEL-ROOT>
			<PEER-ROOT>systems</PEER-ROOT>
		</ROUGE_EVALUATION>
	</TASK>
</CONFIG>
```

* \<CONFIG\> is the root node.
	* \<TASK\> represent a summarization task. You could do multiple in a simple run. At start, stick with one.
		* \<LANGUAGE\> is the input's document language for preprocessing goal. USELESS AT THE MOMENT.
		* \<OUTPUT_PATH\> is the forlder's output path of the system. It is used to save preprocessed documents, ROUGE xml generated file, old score, ...
		* \<MULTITHREADING\> (boolean) launch the system in a mutltithreading way or not.
		* <PREPROCESS> is the preprocess step for the system. The preprocess java class to use is pass by the name variable. Here it's GenerateTextModel. It also needs two <OPTION> :
			* <OPTION NAME="StanfordNLP"> (boolean), true if you want to use StanfordNLP pipeline and tool to do the preprocessing.
			* <OPTION NAME="StopWordListPath"> (String), path of the stopwords list you want to use.
		<PROCESS> is the main step of the system. It should have at least one <SUMMARIZE_METHOD> node and two <OPTION>. It often has an <INDEX_BUILDER> node and a <CARACTERISTIC_BUILDER> node :
			* <OPTION NAME="CorpusIdToSummarize"> (String as a list of int separated by \t), the list of CorpusId to summarize from the MultiCorpus configuration file. "all" will do summarization for all corpus.
			* <OPTION NAME="ReadStopWords"> (boolean), state if the system count stopwords as part of the texts or not.
			* <INDEX_BUILDER> is the step where the system generate a computer friendly representation of each text's textual unit. (TF-IDF, Bigram, WordEmbeddings, ...)
			* <CARATERISTIC_BUILDER> is the sentence caracteristic generation step based on the textual unit index building.
			* <SCORING_METHOD> weight each sentences. 
			* <SUMMARIZE_BUILDER> generate a summary usually by ranking sentence based on their score.
		* <ROUGE_EVALUATION> is the ROUGE evaluation step. For detail, look at ROUGE readme in /lib/ROUGE folder.
			* <ROUGE_MEASURE>  represent the list of ROUGE measure you want to use.
			* <MODEL_ROOT> is the model's folder name for ROUGE xml input files. 
			* <PEER_ROOT> is the peer's folder name for ROUGE xml input files. 

The <PROCESS> node is the system's core and you should look for more detail in the javadoc and the source code of the different INDEX_BUILDER, CARACTERISTIC_BUILDER, SCORING_METHOD and SUMMARIZE_METHOD.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Valentin Nyzam** - [ValNyz](https://github.com/ValNyz)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the under GPL3 License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Thanks to Aurélien Bossard, my PhD supervisor.
