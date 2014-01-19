# Runs jNode
for I in ../lib/*.jar; do
N="$I:$N"
W="$I;$W"
done
echo "#!/bin/sh" > run.sh
echo "java -cp \"$N\" jnode.main.Main ../etc/jnode.nix.conf" >> run.sh
echo "==============================="
echo "java -cp \"`echo $W|sed 's/\\//\\\\/g'`\" jnode.main.Main ..\\etc\\jnode.win.conf" > run.bat
