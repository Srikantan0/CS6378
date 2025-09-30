#!/bin/bash
netid=dal660753
PROJDIR=/Users/Srikantan/Downloads/Projects/OS/CS6378_P1/src
CONFIGLOCAL=/Users/Srikantan/Downloads/Projects/OS/CS6378_P1/src
BINDIR=$PROJDIR/com/os
PROG=Main
n=0
cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    while [[ $n -lt $i ]]
    do
    	read line
    	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )
	
	gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $p; exec bash" &

        n=$(( n + 1 ))
    done
)
