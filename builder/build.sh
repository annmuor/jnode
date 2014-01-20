#!/bin/sh
TIME=`date +%s`
TEMP="`mktemp -d --tmpdir="."`"
DIR="$TEMP/jnode"
for I in bin docs etc files inbound lib log nodelist tmp; do 
    mkdir -p "$DIR/$I"; 
    if [ -d "$I" ]; then cp -rv "$I" "$DIR"; fi
done

mvn clean package -f ../pom.xml -pl jnode-core,jnode-httpd-module,jnode-mail-module -am && \
echo "Run!"
for I in jnode-core jnode-httpd-module jnode-mail-module; do
    RPATH="../$I"
    find "$RPATH/target/" -name '*.jar' -exec mv -v '{}' "$DIR/lib" ';'
done

# Runs jNode
for I in $DIR/lib/*.jar; do
    A="`echo $I|sed -r "s#$DIR#..#"`"
    N="$N:$A"
    W="$W;$A"
done

echo "#!/bin/sh" > "$DIR/bin/run.sh"
echo "java -cp \"$N\" jnode.main.Main ../etc/jnode.nix.conf" >> "$DIR/bin/run.sh"
echo "java -cp \"`echo $W|sed 's/\\//\\\\/g'`\" jnode.main.Main ..\\etc\\jnode.win.conf" > "$DIR/bin/run.bat"

cd "$TEMP"
zip -r ../jnode-1.3-release-multidb-$TIME.zip jnode
