set output "scriptGnuDocument.gif"
set style data histogram 
set autoscale y2
set autoscale y
set ytics border out scale 1,0.5 nomirror
set ylabel "Score Topic n°36, 22 and 30"
set y2tics border out scale 1,0.5 nomirror
set y2label "Score Topic n°33"
set tics out
set style fill solid border -1
set xlabel "Paragraph position in a document"
set xrange[-0.5:45.5]
set xtics 2
set key off
set title "Average paragraph score by position in the document"
set style histogram cluster gap 1
set boxwidth 1
plot "data.dat" using 2 title "Topic N°33" axes x1y2, "" using 3 lc 3 title "Topic N°36" axes x1y1, "" using 4 lc 4 title "Topic N°22" axes x1y1, "" using 5 lc 5 title "Topic N°30" axes x1y1