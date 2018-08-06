add_first_column=0
regenelab=1

if [[ $#<1 ]]; then
  echo "Usage: $0 <datafile> [setting_file]"
  exit
fi

if [[ ! -z $2 ]]; then
  source $2
fi

if [[ -z $min || -z $fq || -z $tq || -z $max ]]; then
  echo "must define at least varible min,fq,tq,max. Each variable is a column number"
  echo "could define also variables quart_title, med_title, med, fileformat"
  exit
fi

#will remove emty lines
sed '/^$/d' $1 > $1.tmp;
mv $1.tmp $1;

tmpdata=$1.elab
tmpgnuplot=$1.gnuplot

if [[ $regenelab != 0 ]]; then
  j=1
  rm -f $tmpdata
  cat $1 | grep -v '#' | grep -v '%'| while read line; do
    if [[ $add_first_column == 1 ]]; then
      lin="$j $line";
    else
      lin=$line;
    fi
    echo -e $lin >> $tmpdata
    j=$((j+1))
  done
elif [[ ! -r $tmpdata ]]; then
  echo "Error: $tmpdata does not exists. Please set var \"regenelab\" to a value != 0"
  exit -1
fi

#compute maximum of X axis if first column (X) already present
if [[ $add_first_column == 0 ]];  then
  if [[ -z $xmn || -z $xmx ]]; then 
    echo "Warning xmn and/or xmx not defined!" > 2
  fi
fi

len=`cat $tmpdata | wc -l`
#cat $1.tmp
#read

echo "set terminal postscript enhanced 26" > $tmpgnuplot
echo "set xrange[$xmn:$xmx]" >> $tmpgnuplot
#nocrop enhanced nocrop font arial 8 size 420,320
#if [[ -z $title ]]; then
#  title=""
#fi

if [[ -z $quart_title ]]; then
  #quart_title="quartiles"
	echo ""
fi

if [[ -z $med_title ]]; then
  #med_title="median"
	echo ""
fi

if [[ -z $title ]]; then
  title="candlestick"
fi

if [[ -z $fname ]]; then
  fname="$1.eps"
fi

if [[ ! -z $xlabel ]]; then 
  echo "set xlabel '$xlabel'" >> $tmpgnuplot
fi

if [[ ! -z $ylabel ]]; then
  echo "set ylabel '$ylabel'" >> $tmpgnuplot
fi

#echo "set title '$title'" >> $tmpgnuplot
echo "set key ins vert center top spacing 1.0" >> $tmpgnuplot
echo "set output '$fname'" >> $tmpgnuplot

if [[ $add_first_column == 1 ]];  then 
  echo "set xrange[0:$((len+1))];" >> $tmpgnuplot
fi

if [[ $logy == 1 ]]; then
  logy=1
  echo "set logscale y" >> $tmpgnuplot
fi

#if [[ -z $logy ]]; then 
#  echo "set yrange[0:$((mx+1))]" >> $tmpgnuplot
#fi

echo "set bars 4.0" >> $tmpgnuplot
echo "set style fill empty" >> $tmpgnuplot

echo "stats '$tmpdata' using 2 name 'X'" >> $tmpgnuplot
echo "stats '$tmpdata' using 6 name 'Ym'" >> $tmpgnuplot
echo "stats '$tmpdata' using 10 name 'YM'" >> $tmpgnuplot

echo "set xrange [X_min-$deltax:X_max+$deltax]" >> $tmpgnuplot 
echo "set yrange [Ym_min-$deltay:YM_max+$deltay]" >> $tmpgnuplot 

if [[ $add_first_column == 1 ]];  then 
  fq=$((fq+1));
  max=$((max+1));
  tq=$((tq+1));
  min=$((min+1));
  med=$((med+1));  
fi

#echo "set arrow from -0.5,1 to 0.75,1 nohead linestyle 1"  >> $tmpgnuplot

echo -n "plot '$tmpdata' using $((first)):$((fq)):$((min)):$((max)):$((tq)):xticlabel($first) with candlesticks lt -1 lw 1 title '$quart_title' whiskerbars" >> $tmpgnuplot

if [[ ! -z $med ]]; then
  echo ",'' using $((first)):$((med)):$((med)):$((med)):$((med)):xticlabels($first) with candlesticks lt -1 lw 4 title '$med_title'" >> $tmpgnuplot
fi

gnuplot $tmpgnuplot
epstopdf $fname
#rm $fname
