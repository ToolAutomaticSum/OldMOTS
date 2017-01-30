set output "scriptGnu0.gif"
set style data histogram 
set yrange[0:0.004] 
set style fill solid border -1
set xlabel "Sentence position in a paragraph"
set key off 
set title "Score of the best topic by position of sentences in a paragraph"
set style histogram cluster gap 1
set xrange[0:12] 
set ylabel "Best topic score"
set boxwidth 0.9
set key off
plot "data.dat" using 2 title "Paragraph N°1", "" using 3 lc 3 title "Paragraph N°3", "" using 4 lc 4 title "Paragraph N°4", "" using 5 lc 5 title "Paragraph N°5", "" using 6 lc 6 title "Paragraph N°7", "" using 7 lc 7 title "Paragraph N°8", "" using 8 lc 8 title "Paragraph N°9", "" using 9 lc 9 title "Paragraph N°10", "" using 10 lc 10 title "Paragraph N°11", "" using 11 lc 11 title "Paragraph N°14", "" using 12 lc 12 title "Paragraph N°15", "" using 13 lc 13 title "Paragraph N°17", "" using 14 lc 14 title "Paragraph N°18", "" using 15 lc 15 title "Paragraph N°19", "" using 16 lc 16 title "Paragraph N°20", "" using 17 lc 17 title "Paragraph N°23", "" using 18 lc 18 title "Paragraph N°24", "" using 19 lc 19 title "Paragraph N°25", "" using 20 lc 20 title "Paragraph N°26", "" using 21 lc 21 title "Paragraph N°27", "" using 22 lc 22 title "Paragraph N°28", "" using 23 lc 23 title "Paragraph N°29", "" using 24 lc 24 title "Paragraph N°30", "" using 25 lc 25 title "Paragraph N°32", "" using 26 lc 26 title "Paragraph N°33", "" using 27 lc 27 title "Paragraph N°34", "" using 28 lc 28 title "Paragraph N°36", "" using 29 lc 29 title "Paragraph N°37", "" using 30 lc 30 title "Paragraph N°38", "" using 31 lc 31 title "Paragraph N°39", "" using 32 lc 32 title "Paragraph N°43", "" using 33 lc 33 title "Paragraph N°44", "" using 34 lc 34 title "Paragraph N°45", "" using 35 lc 35 title "Paragraph N°47", "" using 36 lc 36 title "Paragraph N°48", "" using 37 lc 37 title "Paragraph N°49", "" using 38 lc 38 title "Paragraph N°50", "" using 39 lc 39 title "Paragraph N°52", "" using 40 lc 40 title "Paragraph N°53", "" using 41 lc 41 title "Paragraph N°54", "" using 42 lc 42 title "Paragraph N°55", "" using 43 lc 43 title "Paragraph N°56", "" using 44 lc 44 title "Paragraph N°57", "" using 45 lc 45 title "Paragraph N°58", "" using 46 lc 46 title "Paragraph N°59", "" using 47 lc 47 title "Paragraph N°61", "" using 48 lc 48 title "Paragraph N°62"
