#!/bin/sh
JAVA_HOME=/home/fido/jNode/jre1.7.0_10
CLASSPATH=".";
for I in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$I"
done
java -Xmx200m -server -cp "$CLASSPATH" jnode.main.Main config/test.config