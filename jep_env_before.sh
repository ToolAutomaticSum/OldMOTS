#!/bin/bash

if [ -z $JEP_HOME ]; then
	echo "You need to define \$JEP_HOME to your folder containing your jep python package."
	exit 1
fi

array=($(echo $(ldd $JEP_HOME/libjep.so | grep python) | tr ' ' '\n'))
preload=${array[2]}
if [ -z $preload ]; then
	preload=${array[0]}
fi
echo "Running with :
LD_PRELOAD=$preload
LD_LIBRARY_PATH=$JEP_HOME
"

export LD_PRELOAD=$preload
export LD_LIBRARY_PATH=$JEP_HOME
exit 0
