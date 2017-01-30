#!/usr/bin/perl -w
use Cwd;
$curdir=getcwd;
$ROUGE="../ROUGE-1.5.5.pl";
chdir("sample-test");
$cmd="$ROUGE -e ../data -c 95 -2 -1 -U -r 1000 -n 4 -w 1.2 -a ../sample-test/ROUGE-test.xml > ../sample-output/test.out";
print $cmd,"\n";
system($cmd);
chdir($curdir);
