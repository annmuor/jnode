package org.jnode.xmpp;

import jnode.event.IEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;

public class XMPPModule extends JnodeModule {
	private final XMPPClient client;
	private static final Logger logger = Logger.getLogger(XMPPModule.class);

	public XMPPModule(String configFile) throws JnodeModuleException {
		super(configFile);
		client = new XMPPClient(properties);
		if (!client.testConnection()) {
			throw new JnodeModuleException("Invalid XMPP configuration");
		}
	}

	@Override
	public void handle(IEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		synchronized (client) {
			while (true) {
				logger.l3("Running XMPP client...");
				client.run();
				try {
					client.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
