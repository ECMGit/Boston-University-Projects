#!/bin/bash
rm ./AtroposOutput.txt
COUNTER=1
while [ $COUNTER -lt 11 ]; do
	echo $COUNTER\ >> AtroposOutput.txt
	java AtroposGame 7 "java ThreeMonkeeyz" >> AtroposOutputVsDefault.txt
	let COUNTER=COUNTER+1
done

