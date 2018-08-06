d=PLOTS_`date +%s`
mkdir $d
for f in `ls -1 *Alpha*.txt`; do 
	echo $f | grep Prec
	if [[ $? == 1 ]]; then 
		echo $f: Recall
		type=Rec
	else
		echo $f: Precision
		type=Prec;
	fi
	bash candle_stick.gnuplot.sh $f candleStick.$type.def 
	newfe=`echo $f.eps | sed -e 's/\.//' -e 's/\.txt//'`
	newfpdf=`echo $f.pdf | sed -e 's/\.//' -e 's/\.txt//'`
	mv $f.eps  $d/$newfe
	mv $f.pdf  $d/$newfpdf
done 
