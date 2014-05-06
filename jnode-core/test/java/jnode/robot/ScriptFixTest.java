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
    public void testMarginedExtractScript() throws Exception {
        String text = "lalal \n\r bla-bla \t haha";
        Assert.assertEquals(text, ScriptFix.extractScript("sdfdsf {" + text + "} dasf adf \n gg \n"));
    }

    @Test
    public void testBadExtractScript() throws Exception {
        String text = "lalal \n\r bla-bla \t haha";
        Assert.assertNull(ScriptFix.extractScript("{" + text));
    }

    @Test
    public void testConsole() throws Exception {
        Assert.assertEquals("42FIFO", ScriptFix.executeScriptWithConsole("var a = 42 + 'FIFO'; console.log(a);", true));
    }

    @Test
    public void testExtractRealScript() throws Exception {
        String realScript = "[\u0001MSGID: 2:5020/828.17 5368d8ed\n" +
                "\u0001PID: GED+W32 1.1.5-040321\n" +
                "\u0001CHRS: CP866 2\n" +
                "{\n" +
                "console.log('ffff');\n" +
                "}\n" +
                "\n" +
                "--- 26CDDD30B63806A25C6FD3AB22BB423C8B45A86B\n" +
                " * Origin: kirill@temnenkov.com (2:5020/828.17)\n" +
                "\u0001Via 2:5020/828.17 @20140506.124330.UTC hpt/w32-mvcdll 1.4.0-sta 29-12-03\n" +
                "]";
        Assert.assertEquals("\nconsole.log('ffff');\n", ScriptFix.extractScript(realScript));
    }

}