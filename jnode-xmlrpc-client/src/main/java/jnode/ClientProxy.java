package jnode;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

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

        Properties properties = new Properties();

        tryLoadProperties(properties);

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(properties.getProperty("url", "http://127.0.0.1:8080/xmlrpc")));
        config.setBasicUserName(properties.getProperty("login", "admin"));
        config.setBasicPassword(properties.getProperty("password", "password"));
        config.setEnabledForExtensions(false);
        config.setContentLengthOptional(false);
        config.setConnectionTimeout(30 * 1000);
        config.setReplyTimeout(30 * 1000);

        XmlRpcClient client = new XmlRpcClient();
        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        client.setConfig(config);
        return client;
    }

    private static void tryLoadProperties(Properties properties) {
        File config = new File("/opt/projects/jnodeclient/config.properties");
        if (config.exists() && config.canRead()) {

            try {
                properties.load(new FileInputStream(config));
            } catch (IOException ignored) {
            }
        }
    }
}
