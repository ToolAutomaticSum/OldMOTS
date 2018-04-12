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

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Valentin Nyzam** - [ValNyz](https://github.com/ValNyz)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the under GPL3 License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Thanks to Aurélien Bossard, my PhD supervisor.
