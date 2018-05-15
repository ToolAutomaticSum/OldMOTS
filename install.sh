#!/bin/sh

if [ -z $JAVA_HOME ];then
	echo "You need to define \$JAVA_HOME to your folder containing your jdk."
	exit 0
fi

if [ -z $CORPUS_DATA ]; then
	echo "You might define \$CORPUS_DATA to your folder containing TAC/DUC corpus."
fi

if [ ! -x $(command -v jep) ]; then
	echo -n "MOTS need the installation of python package jep. Use 'pip install jep'"
	exit 0
fi

if [ -z $JEP_HOME ]; then
	echo "You need to define \$JEP_HOME to your folder containing your jep python package. It should be /usr/lib/pythonX.Y/site-packages/jep or $HOME/.local/lib/pythonX.Y/site-packages/jep"
	exit 0
fi

mvn install
#tar xvf "mots.tar" "MOTS"
if [ -n $CORPUS_DATA ]; then
	echo Update TAC/DUC multicorpus configuration file to your \$CORPUS_DATA folder.
	sed -i "s/\$CORPUS_DATA/$CORPUS_DATA/g" conf/*.xml
fi

echo "Installation of MOTS succeed in the target directory !"
exit 1 
