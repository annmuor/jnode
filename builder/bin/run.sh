#!/bin/sh
CLASSPATH=".";
for I in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$I"
done
java -Xmx200m -server -cp "$CLASSPATH" jnode.main.Main config/test.config