columns_to_plot="2 3 6 7 10 11"

dplots=PLOT_MED_`date +%s`
mkdir $dplots

for f in `ls elabMED_topk*ec.txt`; do  #Precision and Recall
	fplot=PLOT_$f;
	echo "set encoding utf8" > $fplot
	echo "set terminal postscript enhanced eps monochrome 26"  >> $fplot;
	echo "set datafile missing '-'" >> $fplot;
	echo "set key bottom right" >> $fplot;
	#set yrange [0:1.0]
	#set xrange [0.4:1.0]
	echo $f | grep "Prec"
	if [[ $? == 0 ]]; then 
		title="Precision"
	else
		title="Recall"
	fi
	echo "set ylabel '$title'" >> $fplot;
	echo " set xlabel '{/Symbol D}' " >> $fplot; 
#	echo "set grid" >> $fplot; 
	echo "set output '$f.eps'" >> $fplot

#	echo "set style line 1 " >> $fplot

echo "set style data linespoint" >> $fplot
#echo "set key autotitle columnheader" >> $fplot
echo "set key font \", 20\"" >> $fplot
echo "plot '$f' using 1:2  pt 1 ps 2 title '{/Symbol a}=0, SC1', ''  using 1:3 pt 3 ps 2 title '{/Symbol a}=0, SC2', '' using 1:6 pt 2 ps 2 title '{/Symbol a}=0.30, SC1' , '' using 1:7 pt 4 ps 2 title '{/Symbol a}=0.30, SC2' , '' using 1:10 pt 5 ps 2  title '{/Symbol a}=0.60, SC1', '' using 1:11 pt 6 ps 2 title '{/Symbol a}=0.60, SC2' " >> $fplot;

	gnuplot $fplot;

	epstopdf $f.eps

	newfeps=`echo $f.eps | sed -e 's/\.txt//'`
	newfpdf=`echo $f.pdf | sed -e 's/\.txt//'`

	mv $f.eps $dplots/$newfeps
	mv $f.pdf $dplots/$newfpdf
done
