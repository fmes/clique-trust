LANG=en_US

mintopk=10
maxtopk=100

	#join previous file for various values of topk, alpha fixed
	for alpha in `seq 0 0.15 0.95`; do
		unset fprecv; 
		unset frecv;
		header="DELTA "; 
		for t in `seq $mintopk 10 $maxtopk`; do 
				fprec="elabMED_topk"$t"Prec_Alpha"$alpha.txt;
				frec="elabMED_topk"$t"Rec_Alpha"$alpha.txt;

				echo $fprec, $frec

				header=$header"'k=$t,SC1' 'k=$t,SC2' "

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

			final_prec="elabMED_alpha"$alpha"Prec.txt";
			final_rec="elabMED_alpha"$alpha"Rec.txt";

			l1=`wc -l $final_prec | awk '{print $1} '`
			l2=`wc -l $final_rec | awk '{print $1} '`

			l1tail=$((l1-1))
			l2tail=$((l2-1))

			echo $header > $final_prec
			echo $header > $final_rec

			tail -n$l1tail $fprecv >> $final_prec
			tail -n$l2tail $frecv >> $final_rec

			ls -l $final_prec $final_rec; 
	done	
