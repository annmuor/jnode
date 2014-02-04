FILE="$1"
if [ -f "$FILE" ]; then
	cp "$FILE" "$FILE.tmp" && \
	cat header > "$FILE" && \
	cat "$FILE.tmp" >> "$FILE" && \
	rm "$FILE.tmp" && \
	echo "$FILE done" 
fi
