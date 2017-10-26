#!/bin/sh

if [ -z $ROUGE_HOME ];then
	echo "You need to define \$ROUGE_HOME"
	exit 1
fi

if [ -z $STANFORD_NLP_HOME ];then
	echo "You need to define \$STANFORD_NLP_HOME"
	exit 1
fi

if [ ! -f "lib/ROUGE-1.5.5" ];then 
	ln -s $ROUGE_HOME lib/
fi

if [ ! -f "lib/stanford-corenlp-3.8.0.jar" ];then
	ln -s "$STANFORD_NLP_HOME/stanford-corenlp-3.8.0.jar" "lib/stanford-corenlp-3.8.0.jar"
fi

if [ ! -f "lib/stanford-corenlp-3.8.0-models-english.jar" ];then
	ln -s $STANFORD_NLP_HOME/stanford-english-corenlp-*-models.jar "lib/stanford-corenlp-3.8.0-models-english.jar"
fi

echo "Installation of stanford lib and model succeed !"
exit 0
