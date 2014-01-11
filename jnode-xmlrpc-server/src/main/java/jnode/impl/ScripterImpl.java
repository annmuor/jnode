package jnode.impl;

import jnode.Scripter;
import jnode.jscript.JscriptExecutor;
import jnode.logger.Logger;
import org.apache.xmlrpc.XmlRpcException;

import java.text.MessageFormat;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class ScripterImpl implements Scripter {

    private final Logger logger = Logger.getLogger(getClass());

    @Override
    public String run(String id) throws XmlRpcException {

        logger.l5("run id = [" + id + "]");

        long realId;
        try {
            realId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return MessageFormat.format("Bad script id: \"{0}\"", id);
        }

        String result = JscriptExecutor.executeScript(realId);
        if (result == null) {
            result = "";
        }
        logger.l5("run with result " + result);
        return result;
    }
}
