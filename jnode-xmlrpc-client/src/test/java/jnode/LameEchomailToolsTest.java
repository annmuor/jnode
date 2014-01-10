package jnode;

import jnode.impl.EchomailToolsProxy;
import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public final class LameEchomailToolsTest {
    private LameEchomailToolsTest() {
    }

    public static void main(String[] args) throws XmlRpcException, MalformedURLException {
        System.out.println(EchomailToolsProxy.writeEchomail("828.test", "Превед", "Всем превед от xml-rpc"));
    }
}
