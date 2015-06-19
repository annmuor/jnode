#!/bin/sh
if [ "$1" == "start" ]; then
	CLASSPATH=".";
	for I in ../lib/*.jar; do
		CLASSPATH="$CLASSPATH:$I"
	done
	java -Xmx300m -server -cp "$CLASSPATH" jnode.main.Main ../etc/jnode.nix.conf 2> /dev/null &
elif [ "$1" == "stop" ]; then
	if [ -f ../log/jnode.pid ]; then
		kill "$(cat ../log/jnode.pid)"
	fi
elif [ "$1" == "restart" ]; then
	$0 stop
	$0 start

else 
	echo "$0 {start|stop|restart}"
fi

