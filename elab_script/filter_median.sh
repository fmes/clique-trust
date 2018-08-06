LANG=en_US
mintopk=10
maxtopk=100

minsc=1
maxsc=2

# fix alpha and topk. Join only median values of Precision and recall vs Delta. 
	for alpha in `seq 0 0.15 0.95`; do
		for t in `seq $mintopk 10 $maxtopk`; do 
				unset fprecv; 
				unset frecv;
				header="DELTA";   	
				for i in `seq $minsc 1 $maxsc`; do 
					fprec="elabSC"$i"_topk"$t"Prec_Alpha"$alpha".txt";
					frec="elabSC"$i"_topk"$t"Rec_Alpha"$alpha.txt;
 
					# get median values and delta values
					awk '{print $2, $8}' $fprec > $fprec.MED
					awk '{print $2, $8}' $frec > $frec.MED

					header=$header" '{/Symbol a}=$alpha, SC$i'";

					if [[ ! -z $fprecv && ! -z $frecv ]]; then
						newprecv=`mktemp`;
						newrecv=`mktemp`;
						join $fprecv $fprec.MED > $newprecv;
						join $frecv $frec.MED > $newrecv;
						fprecv=$newprecv;
						frecv=$newrecv;
					else
						fprecv=$fprec.MED;
						frecv=$frec.MED;
					fi
				done

				lprec=`wc -l $fprecv | awk '{print $1}'`; 
				lrec=`wc -l $frecv | awk '{print $1}'`; 

				lprectail=$((lprec-1)); 
				lrectail=$((lrec-1)); 

				final_prec="elabMED_topk"$t"Prec_Alpha"$alpha.txt;
				final_rec="elabMED_topk"$t"Rec_Alpha"$alpha.txt;

				echo $header > $final_prec;
				echo $header > $final_rec; 

				tail -n $lprectail $fprecv >> $final_prec;
				tail -n $lrectail $frecv >> $final_rec;

				ls -l $final_prec $final_rec; 
			done
		done

	#join previous file for various values of alpha, topk fixed
	for t in `seq $mintopk 10 $maxtopk`; do 
		unset fprecv; 
		unset frecv;
		for alpha in `seq 0 0.15 0.95`; do

				fprec="elabMED_topk"$t"Prec_Alpha"$alpha.txt;
				frec="elabMED_topk"$t"Rec_Alpha"$alpha.txt;

					if [[ ! -z $fprecv && ! -z $frecv ]]; then
						newprecv=`mktemp`;
						newrecv=`mktemp`;
						join $fprecv $fprec > $newprecv;
						join $frecv $frec > $newrecv;
						fprecv=$newprecv;
						frecv=$newrecv;
					else
						fprecv=$fprec;
						frecv=$frec;
					fi
				done

				final_prec="elabMED_topk"$t"Prec.txt";
				final_rec="elabMED_topk"$t"Rec.txt";

				mv $fprecv $final_prec;
				mv $frecv $final_rec;

				ls -l $final_prec $final_rec; 
			done


