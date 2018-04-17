#!/bin/sh

if [ -z $JAVA_HOME ];then
	echo "You need to define \$JAVA_HOME to your folder containing your jdk."
	exit 0
fi

#if [ -z $ROUGE_HOME ];then
	#echo "You need to define \$ROUGE_HOME to your folder containing rouge.1.5.5.pl."
	#exit 0
#fi

if [ -z $STANFORD_NLP_HOME ];then
	echo "You need to define \$STANFORD_NLP_HOME to your folder containing stanford models."
	exit 0
fi

if [ -z $CORPUS_DATA ]; then
	echo "You might define \$CORPUS_DATA to your folder containing TAC/DUC corpus."
fi

#if [ ! -f "lib/ROUGE-1.5.5" ];then 
	#ln -s $ROUGE_HOME lib/
#fi

if [ ! -f "lib/stanford-corenlp-3.8.0.jar" ]; then
	ln -s $STANFORD_NLP_HOME/stanford-corenlp-\d\.\d\.\d.jar "lib/stanford-corenlp-3.8.0.jar"
fi

if [ ! -f "lib/stanford-corenlp-3.8.0-models-english.jar" ];then
	ln -s $STANFORD_NLP_HOME/stanford-english-corenlp-\d\.\d\.\d-models.jar "lib/stanford-corenlp-3.8.0-models-english.jar"
fi

if [ -n $(command -v jep) ]; then
	echo -n "MOTS need the installation of python package jep. Use 'pip install jep'"
	exit 0
fi

mvn install
#tar xvf "mots.tar" "MOTS"
if [ -z $CORPUS_DATA ]; then
	sed -i "s/\$CORPUS_DATA/$CORPUS_DATA/g" conf/*.xml
fi

if [ ! -f "target/conf" ]; then
	ln -s conf target/conf
fi

if [ ! -f "target/lib" ]; then
	ln -s lib target/lib
fi
echo "Installation of MOTS succeed in the target directory !"
exit 1 
