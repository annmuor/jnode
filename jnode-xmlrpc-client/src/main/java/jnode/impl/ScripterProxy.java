package jnode.impl;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.net.MalformedURLException;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public final class ScripterProxy {

    private ScripterProxy() {
    }

    public static String runScript(String id) {

        try {
            ClientFactory factory = new ClientFactory(ClientProxy.getXmlRpcClient());
            jnode.Scripter scripter = (jnode.Scripter) factory.newInstance(jnode.Scripter.class);
            return scripter.run(id);

        } catch (MalformedURLException | XmlRpcException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
