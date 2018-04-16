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
	echo -n "MOTS need the python installation of python package jep. Continue (y/n) ? "
	read rep
	case $rep in 
		y|Y) ;;
		n|N) exit 1
	esac

	python3=$(command -v python3)
	if [ -z $python3 ]; then 
		python2=$(command -v python2)
		if [ -z $python2 ]; then
			echo "Need at least python2.7. Please install python package."
			exit 0
		else
			python2path=$(python2 -c "import sys; print sys.executable")
			if [ -z $(command -v pip) ]; then
				echo "Please install pip or jep and gensim python packages." 
				exit 0
			else
				pip install --user jep
				if [ -z $(echo $JEP_HOME) ]; then
					echo JEP_HOME=\"$python2path/jep\" >> $HOME/.profile
					echo LD_LIBRARY_PATH=$JEP_HOME >> $HOME/.profile
					echo LD_PRELOAD=\"/$(ldconfig -p | grep libpython2.7.so.1.0 | cut -d'/' -f2-)\" >> $HOME/.profile
				fi
			fi
		fi
	else
		python3path=$(python3 -c "import sys; print(sys.executable)")
		if [ -z $(command -v pip3) ]; then
			echo "Please install pip3 or jep and gensim python packages."
			exit 0 
		else
			pip3 install --user jep
			echo JEP_HOME=\"$python3path/jep\" >> $HOME/.profile
			echo LD_LIBRARY_PATH=$JEP_HOME >> $HOME/.profile
			echo LD_PRELOAD=\"/$(ldconfig -p | grep libpython3.5m.so.1.0 | cut -d'/' -f2-)\" >> $HOME/.profile
		fi
	fi 
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
