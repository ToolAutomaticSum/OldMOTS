#!/bin/bash

cd $ROUGE_HOME
cd data/WordNet-2.0-Exceptions/
rm WordNet-2.0.exc.db # only if exist
perl buildExeptionDB.pl . exc WordNet-2.0.exc.db

cd ..
rm WordNet-2.0.exc.db # only if exist
ln -s WordNet-2.0-Exceptions/WordNet-2.0.exc.db WordNet-2.0.exc.db
