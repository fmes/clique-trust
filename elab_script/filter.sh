LANG=en_US

topk1=10
topk2=30

minsc=1
maxsc=3

for i in 1 2; do
	f=elabSC$i.txt

	mintopk=`awk '{print $3}' elabSC$i.txt | sort -n | head -n 2 | tail -n 1`;
	maxtopk=`awk '{print $3}' elabSC$i.txt | sort -n | tail -n 1`;

	#fix topk, separate values of Precision and Recall for different values of Delta
	for t in `seq $mintopk 10 $maxtopk`; do
		head -n 1 $f > elabSC_topk$t.txt;
		grep "[[:space:]]$t[[:space:]]" $f >> elabSC_topk$t.txt;

		ff=elabSC_topk$t.txt;

		#Precision and Recall
		awk  '{print $1, $2, $3, $4, $5, $7, $8, $9, $10, $11 }' $ff | column -t > elabSC$i"_topk"$t"Prec".txt 
		awk  '{print $1, $2, $3, $4, $6, $12, $13, $14, $15, $16 }' $ff | column -t > elabSC$i"_topk"$t"Rec".txt 

		#fixed alpha, var delta	
		for alpha in `seq 0 0.15 0.95`; do
			fprec="elabSC"$i"_topk"$t"Prec_Alpha"$alpha".txt"; 
			frec=elabSC$i"_topk"$t"Rec_Alpha"$alpha.txt;
 
			head -n 1 elabSC$i"_topk"$t"Prec".txt > $fprec; 
			head -n 1 elabSC$i"_topk"$t"Rec".txt > $frec; 
			
			grep "^$alpha" elabSC$i"_topk"$t"Rec".txt >> $frec
			grep "^$alpha" elabSC$i"_topk"$t"Prec".txt >> $fprec				
		done
	done
done
