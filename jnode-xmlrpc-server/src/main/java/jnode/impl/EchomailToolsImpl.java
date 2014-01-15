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

        check(areaname, "areaname");
        check(subject, "subject");
        check(text, "text");

        Echoarea area = getEchoarea(areaname);

        FtnTools.writeEchomail(area, subject, text);

        return "";
    }

    @Override
    public String writeEchomail(String areaname, String subject, String text, String fromName, String toName) throws XmlRpcException {
        System.out.println("areaname = [" + areaname + "], subject = [" + subject + "], text = [" + text + "], fromName = [" + fromName + "], toName = [" + toName + "]");

        check(areaname, "areaname");
        check(subject, "subject");
        check(text, "text");
        check(fromName, "fromName");
        check(toName, "toName");

        Echoarea area = getEchoarea(areaname);

        FtnTools.writeEchomail(area, subject, text, fromName, toName);

        return "";
    }

    private static Echoarea getEchoarea(String areaname) throws XmlRpcException {
        Echoarea area = FtnTools.getAreaByName(areaname,
                null);
        if (area == null){
            throw new XmlRpcException(MessageFormat.format("echoarea \"{0}\" not found", areaname));
        }
        return area;
    }

    private static void check(String value, String name) throws XmlRpcException {
        if (isEmpty(value)){
            throw new XmlRpcException(MessageFormat.format("empty {0} not allowed", name));
        }
    }
}
