
for f in `ls lib/*.jar -1`; do 
  export CLASSPATH=$CLASSPATH:`pwd`/$f
done

echo $CLASSPATH

javac -classpath "$CLASSPATH" src/*.java -d bin/
