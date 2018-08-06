export LANG=en_US

if [[ $# < 2 ]]; then 
	echo "Usage $0 <simkey> <template_file>"
	exit -1
fi

simkey=$1
i=0
TIMESTAMP=`date +%s`
dirout=RESULTS"_"$simkey"_"$TIMESTAMP
mkdir -p $dirout 
for alpha in `seq 0 0.15 1.0`; do
	for delta in `seq 0 0.1 0.7`; do
		for s in 4; do #threshold
			for tk in `seq 10 10 100`; do #top_k
				i=$((i+1))
				config=`mktemp`;
				cp $2 $config
				echo "alpha=$alpha" | tee -a $config
				echo "delta=$delta" | tee -a $config
				echo "top_k=$tk" | tee -a $config
				echo "productRatingThreeshold=$s" | tee -a $config
				bash run.sh $config > $dirout/$simkey-LOG_ALPHA$alpha-DELTA$delta-TOPK-$tk-ratingThrees-$s 2>&1
				mv $simkey-parameters.txt $dirout/$simkey-RESULTS_[ALPHA$alpha]-[DELTA$delta]-[RatingThreeshold$s]-[topk$tk].txt
				mv $config $dirout/$simkey-CONFIG_[ALPHA$alpha]-[DELTA$delta]-[RatingThreeshold$s]-[topk$tk].txt
			done
		done
	done
done

mv $simkey"_"trust* $dirout

echo "Results in $dirout"
