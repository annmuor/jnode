package jnode.impl;

import jnode.EchomailTools;
import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import org.apache.xmlrpc.XmlRpcException;

import java.text.MessageFormat;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class EchomailToolsImpl implements EchomailTools {

    private final Logger logger = Logger.getLogger(getClass());

    private static boolean isEmpty(String s){
       return s == null || s.length() == 0;
    }

    @Override
    public String writeEchomail(String areaname, String subject, String text) throws XmlRpcException {

        logger.l5("writeEchomail areaname = [" + areaname + "], subject = [" + subject + "], text = [" + text + "]");

        if (isEmpty(areaname)){
            throw new XmlRpcException("empty areaname not allowed");
        }
        if (isEmpty(subject)){
            throw new XmlRpcException("empty subject not allowed");
        }
        if (isEmpty(text)){
            throw new XmlRpcException("empty text not allowed");
        }

        Echoarea area = FtnTools.getAreaByName(areaname,
                null);
        if (area == null){
            throw new XmlRpcException(MessageFormat.format("echoarea \"{0}\" not found", areaname));
        }

        FtnTools.writeEchomail(area, subject, text);

        return "";
    }
}
