@echo off
setLocal EnableDelayedExpansion
set CLASSPATH=.
for /R ../lib %%a in (*.jar) do (
   set CLASSPATH=!CLASSPATH!;"%%a"
)
set CLASSPATH=!CLASSPATH!
echo !CLASSPATH!
java -Xmx100m -server -cp !CLASSPATH! jnode.main.Main ..\\etc\\jnode.win.conf