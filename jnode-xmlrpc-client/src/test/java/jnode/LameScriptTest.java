package jnode;

import jnode.impl.ScripterProxy;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public final class LameScriptTest {
    private LameScriptTest() {
    }

    public static void main(String[] args) {
        System.out.println(ScripterProxy.runScript("18"));
    }
}
