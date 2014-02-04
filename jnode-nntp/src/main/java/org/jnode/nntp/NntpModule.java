package org.jnode.nntp;

import jnode.event.IEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <a href="http://tools.ietf.org/html/rfc2980">RFC 2980 - Common NNTP Extensions</a><br>
 * <a href="http://tools.ietf.org/html/rfc3977">RFC 3977 - Network News Transfer Protocol (NNTP)</a><br>
 * <a href="http://tools.ietf.org/html/rfc6048">RFC 6048 - Network News Transfer Protocol (NNTP) Additions to LIST Command</a><br>
 */
public class NntpModule extends JnodeModule {

    private Logger logger = Logger.getLogger(NntpModule.class);

    private static final String DEFAULT_PORT = "1119";
    private static final String PORT_PROPERTY = "nntp.port";

    public NntpModule(String configFile) throws JnodeModuleException {
        super(configFile);
    }

    @Override
    public void start() {
        while (!Thread.currentThread().isInterrupted()) {
            String port = properties.getProperty(PORT_PROPERTY, DEFAULT_PORT);
            logger.l4("Using the following port for NNTP: " + port);
            try (ServerSocket serverSocket = new ServerSocket(Integer.valueOf(port))) {
                Socket socket = serverSocket.accept();

                logger.l4("New client accepted.");
                new Thread(new NntpClient(socket)).start();

            } catch (IOException e) {
                Thread.currentThread().interrupt();
                logger.l1("NNTP module can't be initialised.", e);
            }
        }
    }

    @Override
    public void handle(IEvent event) {

    }
}
