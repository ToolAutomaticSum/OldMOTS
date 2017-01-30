set output "scriptGnuDocument.gif"
set style data histogram 
set yrange[0:0.00023] 
set style fill solid border -1
set xlabel "Sentence position in a paragraph"
set xrange[0:63] 
set key outside right 
set title "Score of the best topic by position of sentences in a paragraph"
set style histogram cluster gap 1
set ylabel "Best topic score"
set boxwidth 1
set key right bottom
plot "data.dat" using 2 title "Topic N°1", "" using 3 lc 3 title "Topic N°2", "" using 4 lc 4 title "Topic N°3", "" using 5 lc 5 title "Topic N°4"
