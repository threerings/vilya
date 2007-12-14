# a handy gnuplot instruction set for plotting percentiler dumps
set ylabel 'Count'
set y2label 'Percentile'
set y2tics
set grid
plot 'pctile.data' using 1:3 axes x1y1 title 'Count' with histeps, \
     'pctile.data' using 1:2 axes x1y2 title 'Pecentile' with lines;
