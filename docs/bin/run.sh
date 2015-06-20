#!/bin/sh
ROOT="`dirname $(readlink -f $0)`/../"
PIDFILE="$ROOT/jnode.pid";
JAR="$ROOT/lib"

cd $ROOT
if [ "$1" == "" ]; then 
	echo "Usage: $0 (stop|start|restart|build)"
fi
case "$1" in
	start)
	if [ -f $PIDFILE ]; then
		echo "jNode already running..."
		exit
	fi
	for I in $JAR/*.jar; do A="$A:$I"; done
	nohup java -Xmx300m -server -cp "$A" jnode.main.Main jnode.conf &
	echo -ne $! > $PIDFILE
	;;
	stop)
	if [ -f $PIDFILE ]; then
		kill `cat $PIDFILE`
		rm -f $PIDFILE
	fi
	;;
	restart)
	$0 stop
	$0 start
	;;
	*)
	echo "Usage: $0 (stop|start|restart)"
	exit 0;
	;;
esac

