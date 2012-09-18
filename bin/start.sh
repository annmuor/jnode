#!/bin/sh
CLASSPATH="../jar/jnode.jar";
for I in ../lib/*.jar; do
    CLASSPATH="$CLASSPATH:$I"
done
java -cp "$CLASSPATH" jnode.main.Main ../config/jnode.conf