if [[ $# < 1 ]]; then 
	echo "Usage: $0 <simkey>"
else
	ln -s ../average.sh 
	ln -s ../stat* . 
	bash average.sh $1| column -t > elab.txt 
fi
