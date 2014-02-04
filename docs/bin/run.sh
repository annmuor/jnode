#!/bin/sh
CLASSPATH=".";
for I in ../lib/*.jar; do
    CLASSPATH="$CLASSPATH:$I"
done
java -Xmx100m -server -cp "$CLASSPATH" jnode.main.Main ../etc/jnode.nix.conf