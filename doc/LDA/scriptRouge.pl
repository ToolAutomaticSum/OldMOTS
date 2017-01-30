#!/usr/bin/perl -w
$cmd="./ROUGE-1.5.5.pl -e data -c 95 -2 -1 -U -r 1000 -n 4 -w 1.2 -a LDA/settings.xml"print $cmd,"\n";
system($cmd);
