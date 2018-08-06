if [[ $@ < 1 ]]; then 
	echo "Usage: $0 <simkey>"
	exit
fi

cp stat1.m /tmp
cp stat2.m /tmp

simkey=$1
echo "ALPHA DELTA TOPK THREESHOLD AVG-PRECISION AVG-RECALL minPrec 1QPrec MedPrec 3QPrec MaxPrec minRec 1QRec MedRec 3QRec MaxRec"
for f in `ls -1 $simkey"-"RESULTS*`; do
#	echo $f
	base=`echo $f | sed -e "s/$simkey-RESULTS_//"`;
	alpha=`echo $base | sed -e 's/\[RatingThreeshold[[:digit:]]\]-//' | sed -e 's/\[topk[[:digit:]]\+\]\.txt//' | sed -e 's/\[DELTA[[:digit:]]\.[[:digit:]]\+\]-//' | sed -e 's/\[ALPHA//g' -e 's/\]-//'`;
	topk=`echo $base | sed -e 's/\[RatingThreeshold[[:digit:]]\]-//' | sed -e 's/\[DELTA[[:digit:]]\.[[:digit:]]\+\]-//' | sed -e 's/\[ALPHA.*\]-//g' -e  's/\[topk//' -e 's/\]\.txt//'`;
	delta=`echo $base | sed -e 's/\[RatingThreeshold[[:digit:]]\]-//' |  sed -e 's/\[ALPHA[[:digit:]]\.[[:digit:]]\+\]-//' | sed -e  's/\[topk.*\]\.txt//' -e 's/\[DELTA//' | sed -e 's/\]-//'`
	s=`echo $base | sed -e 's/\[ALPHA[[:digit:]]\.[[:digit:]]\+\]-//' | sed -e  's/\[topk.*\]\.txt//' -e 's/\[DELTA[[:digit:]]\.[[:digit:]]\+\]-//' | sed -e 's/\[RatingThreeshold//' | sed -e 's/\]-//'`

	#compute min, 1stq, median, 3thq, max
	cp $f /tmp/test.txt

	echo -n "$alpha $delta $topk $s "; 
	avg=`awk 'BEGIN{avg1=0; avg2=0; n=0}{avg1+=$2; avg2+=$3; n++;}END{printf("%.4f %.4f", avg1/n, avg2/n)}' $f`
	echo -ne "$avg "
	(cd /tmp; prec=`octave --no-gui --silent stat1.m`; recall=`octave --no-gui --silent stat2.m`; echo "$prec $recall")
done 
