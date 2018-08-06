if [[ $1 < 1 ]]; then
	echo "Usage: $0 <config_file>"	
else

	for f in `ls lib/*.jar -1`; do
		export CLASSPATH=$CLASSPATH:`pwd`/$f
	done

#	echo $CLASSPATH
	maxmem=15000M

	java -Xmx$maxmem -Dline.separator=$'\r'$'\n'  -cp bin:$CLASSPATH MainSimulator $1
fi
