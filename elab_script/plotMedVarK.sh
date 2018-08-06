columns_to_plot="2 3 6 7 10 11"

dplots=PLOT_MED_VarK`date +%s`
mkdir $dplots

for f in `ls elabMED_alpha*ec.txt`; do  #Precision and Recall
	fplot=PLOT_$f;
	echo "set encoding utf8" > $fplot
	echo "set terminal postscript enhanced eps monochrome 26"  >> $fplot;
	echo "set datafile missing '-'" >> $fplot;
	echo "set key bottom right" >> $fplot;
	echo $f | grep "Prec"
	if [[ $? == 0 ]]; then 
		title="Precision"
	else
		title="Recall"
	fi
	echo "set ylabel '$title'" >> $fplot;
	echo " set xlabel '{/Symbol D}' " >> $fplot; 
	echo "set output '$f.eps'" >> $fplot

echo "set style data linespoint" >> $fplot
echo "set key font \", 16\"" >> $fplot
echo "plot '$f' using 1:6 pt 1 ps 2 title 'k=30, SC1' , '' using 1:7 pt 2 ps 2 title 'k=30, SC2' , '' using 1:10 pt 3 ps 2  title 'k=50, SC1', '' using 1:11 pt 4 ps 2 title 'k=50, SC2', '' using 1:14 pt 5 ps 2 title 'k=70, SC1', '' using 1:15 pt 6 ps 2 title 'k=70, SC2', '' using 1:20 pt 7 ps 2 title 'k=100, SC1', '' using 1:21 pt 8 ps 2 title 'k=100, SC2'  " >> $fplot;

	gnuplot $fplot;

	epstopdf $f.eps

	newfeps=`echo $f.eps | sed -e 's/\.txt//' | sed -e 's/\.//'` 
	newfpdf=`echo $f.pdf | sed -e 's/\.txt//' | sed -e 's/\.//'`

	mv $f.eps $dplots/$newfeps
	mv $f.pdf $dplots/$newfpdf
done
