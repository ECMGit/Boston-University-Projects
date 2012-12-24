#!/bin/bash
rm ./AtroposOutputVsDefaultRandomHeight.txt
COUNTER=1
while [ $COUNTER -lt 11 ]; do
	HEIGHT=$[ ( $RANDOM % 5 )  + 5 ]
	echo "$COUNTER with height $HEIGHT\ " >> AtroposOutput.txt
	java AtroposGame $HEIGHT "java ThreeMonkeeyz" >> AtroposOutputVsDefaultRandomHeight.txt
	let COUNTER=COUNTER+1
done

