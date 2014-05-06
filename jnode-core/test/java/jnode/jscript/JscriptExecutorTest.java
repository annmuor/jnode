package jnode.jscript;

import org.junit.Assert;
import org.junit.Test;

import javax.script.Bindings;
import javax.script.SimpleBindings;

public class JscriptExecutorTest {

    @Test
    public void testExecScript() throws Exception {

        Bindings bindings = new SimpleBindings();
        final JScriptConsole jScriptConsole = new JScriptConsole();
        bindings.put("console", jScriptConsole);

        JscriptExecutor.execScript("var a = 42 + 'ggg'; console.log(a);", bindings);
        Assert.assertEquals("42ggg", jScriptConsole.out());
    }
}