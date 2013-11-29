package jnode;

import jnode.impl.ScripterImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class MainServlet extends XmlRpcServlet {

    private String login;
    private String pwd;

    public MainServlet(String login, String pwd) {
        this.login = login;
        this.pwd = pwd;
    }

    private boolean isAuthenticated(String pUserName, String pPassword) {
        return !(login == null || pwd == null) && login.equals(pUserName) && pwd.equals(pPassword);
    }

    protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {

        PropertyHandlerMapping phm = new PropertyHandlerMapping();
        phm.addHandler(Scripter.class.getName(), ScripterImpl.class);

        AbstractReflectiveHandlerMapping.AuthenticationHandler handler =
                new AbstractReflectiveHandlerMapping.AuthenticationHandler() {
                    public boolean isAuthorized(XmlRpcRequest pRequest) {
                        XmlRpcHttpRequestConfig config =
                                (XmlRpcHttpRequestConfig) pRequest.getConfig();
                        return isAuthenticated(config.getBasicUserName(),
                                config.getBasicPassword());
                    }
                };
        phm.setAuthenticationHandler(handler);
        return phm;
    }


}
