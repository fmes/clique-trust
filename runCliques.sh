#java -enableassertions -cp bin MainSimulator 35000 0.5 5 0.2 20
#java -enableassertions -cp bin MainSimulator 

for f in `ls lib/*.jar -1`; do
	export CLASSPATH=$CLASSPATH:`pwd`/$f
done

#echo $CLASSPATH

java -Xmx4000M -ea  -cp bin:$CLASSPATH BronKerboschCliqueFinder $@
