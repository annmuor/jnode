package jnode;

import jnode.event.IEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.ServletWebServer;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class XmlRpcServerModule extends JnodeModule {

    private static final int port = 8080;
    private final Logger logger = Logger.getLogger(getClass());

    public XmlRpcServerModule(String configFile) throws JnodeModuleException {
        super(configFile);
        Properties props = System.getProperties();
        props.setProperty("org.apache.commons.logging.Log", LogWrapper.class.getName());
    }

    public static void main(String[] args) throws JnodeModuleException {
        XmlRpcServerModule x = new XmlRpcServerModule("C:\\FTN\\bat\\test.config");
        x.start();
    }

    @Override
    public void start() {

        final String login = properties.getProperty("xmlrpc.login");
        final String pwd = properties.getProperty("xmlrpc.password");

        if (login == null) {
            logger.l2("Warning: login is empty");
        }
        if (pwd == null) {
            logger.l2("Warning: password is empty");
        }

        XmlRpcServlet servlet = new MainServlet(login, pwd);

        ServletWebServer webServer;
        try {
            webServer = new ServletWebServer(servlet, port);
        } catch (ServletException e) {
            logger.l1("fail start XmlRpcServerModule", e);
            return;
        }

        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

        XmlRpcServerConfigImpl serverConfig =
                (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(false);
        serverConfig.setContentLengthOptional(false);

        try {
            webServer.start();
            logger.l3("xml-rpc server started");
        } catch (IOException e) {
            logger.l1("fail start XmlRpcServerModule", e);
            return;
        }

        while (true) {
            try {
                logger.l5("Still alive, next report after 1 hour");
                Thread.sleep(60 * 60 * 1000);
            } catch (InterruptedException e) {
                logger.l1("Interrupted");
                break;
            }
        }

    }

    @Override
    public void handle(IEvent event) {
    }
}
