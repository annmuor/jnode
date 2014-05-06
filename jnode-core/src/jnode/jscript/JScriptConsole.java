package jnode.jscript;

public class JScriptConsole {

    private final StringBuilder sb = new StringBuilder();

    public void log(Object a){
        sb.append(String.valueOf(a));
    }

    public String out(){
        return sb.toString();
    }
}
