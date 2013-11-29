package jnode;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class ClientProxy {
    public static String runScript(String id) {

        try {
            ClientFactory factory = new ClientFactory(getXmlRpcClient());
            Scripter scripter = (Scripter) factory.newInstance(Scripter.class);
            return scripter.run(id);

        } catch (MalformedURLException e) {
            return e.getMessage();
        } catch (XmlRpcException e) {
            return e.getMessage();
        }
    }

    private static XmlRpcClient getXmlRpcClient() throws MalformedURLException {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://127.0.0.1:8080/xmlrpc"));
        config.setBasicUserName("admin");
        config.setBasicPassword("password");
        config.setEnabledForExtensions(false);
        config.setContentLengthOptional(false);
        config.setConnectionTimeout(30 * 1000);
        config.setReplyTimeout(30 * 1000);

        XmlRpcClient client = new XmlRpcClient();
        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        client.setConfig(config);
        return client;
    }
}
