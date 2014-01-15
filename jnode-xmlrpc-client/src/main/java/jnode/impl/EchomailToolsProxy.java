package jnode.impl;

import jnode.EchomailTools;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.net.MalformedURLException;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public final class EchomailToolsProxy {
    private EchomailToolsProxy() {
    }

    public static String writeEchomail(String areaname, String subject, String body) {

        try {
            ClientFactory factory = new ClientFactory(ClientProxy.getXmlRpcClient());
            jnode.EchomailTools echomailTools = (EchomailTools) factory.newInstance(EchomailTools.class);
            return echomailTools.writeEchomail(areaname, subject, body);
        } catch (MalformedURLException | XmlRpcException e) {
            return e.getMessage();
        }

    }

    public static String writeEchomail(String areaname, String subject, String body, String fromName, String toName) {

        try {
            ClientFactory factory = new ClientFactory(ClientProxy.getXmlRpcClient());
            jnode.EchomailTools echomailTools = (EchomailTools) factory.newInstance(EchomailTools.class);
            return echomailTools.writeEchomail(areaname, subject, body, fromName, toName);
        } catch (MalformedURLException | XmlRpcException e) {
            return e.getMessage();
        }

    }
}
