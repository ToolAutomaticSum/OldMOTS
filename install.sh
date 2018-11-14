#!/bin/bash

if [ -z $JAVA_HOME ];then
	read -p "\$JAVA_HOME isn't set. Do you want to set it automatically to your .profile file ? (y|n) " answer
	case $answer in
   	    [Yy]* )
		if [ -x $(command -v javac) ]; then
			echo "export JAVA_HOME=\"$(dirname $(dirname $(readlink -f $(which javac))))\"" >> $HOME/.profile
			source $HOME/.profile
		else
			echo "Please install Java 8 JDK (sudo apt-get install openjdk-8-jdk)."
		fi
    	    ;;
       	    * )
		echo "Please set the \$JAVA_HOME variable."
       		exit 1
    	    ;;
	esac
fi

#if [ -z $CORPUS_DATA ]; then
	#echo "You might define \$CORPUS_DATA to your folder containing TAC/DUC corpus."
#fi

if [ ! -x $(command -v jep) ]; then
	echo -n "MOTS need the installation of python package jep. Use 'pip3 install jep --user'"
	exit 1
fi

if [ -z $JEP_HOME ]; then
	echo "export JEP_HOME=\"$(python3 -m site --user-site)/jep\"" >> $HOME/.profile
	source $HOME/.profile	
fi


if [ ! -x $(command -v jep) ]; then
	echo "Please install Maven (sudo apt-get install maven)."
else
	source $(pwd)/jep_env_before
	mvn install
	source $(pwd)/jep_env_after
fi

#if [ -n $CORPUS_DATA ]; then
	#echo Update TAC/DUC multicorpus configuration file to your \$CORPUS_DATA folder.
	#sed -i "s/\$CORPUS_DATA/$CORPUS_DATA/g" conf/*.xml
#fi

echo "Installation of MOTS succeed in the target directory !"
exit 0 
