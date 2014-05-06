package jnode.robot;

import org.junit.Assert;
import org.junit.Test;

public class ScriptFixTest {

    @Test
    public void testExtractScriptId() throws Exception {
        final long scriptId = ScriptFix.extractScriptId("%RUN 34");
        Assert.assertEquals(34L, scriptId);
    }

    @Test
    public void testExtractBadScriptId() throws Exception {
        Assert.assertNull(ScriptFix.extractScriptId("%RUN 34g"));
    }

    @Test
    public void testExtractBadCommandScriptId() throws Exception {
        Assert.assertNull(ScriptFix.extractScriptId("%RUN2 34"));
    }

    @Test
    public void testExtractScript() throws Exception {
        String text = "lalal \n\r bla-bla \t haha";
        Assert.assertEquals(text, ScriptFix.extractScript("{" + text + "}"));
    }

    @Test
    public void testBadExtractScript() throws Exception {
        String text = "lalal \n\r bla-bla \t haha";
        Assert.assertNull(ScriptFix.extractScript("{" + text));
    }

}