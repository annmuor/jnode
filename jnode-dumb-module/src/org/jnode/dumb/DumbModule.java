package org.jnode.dumb;

import jnode.event.IEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;

public class DumbModule extends JnodeModule {
	public DumbModule(String configFile) throws JnodeModuleException {
		super(configFile);
	}

	private static final Logger logger = Logger.getLogger(DumbModule.class);

	@Override
	public void handle(IEvent event) {

	}

	@Override
	public void start() {
		long delay = new Long(properties.getProperty("delay"));
		synchronized (this) {
			while (true) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
				logger.l1("Dumb module still alive");
			}
		}
	}

}
