package jnode.main;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.Connector;
import jnode.protocol.io.exception.ProtocolException;

/**
 * 
 * @author kreon
 * 
 */
public class Poll extends Thread {
	private static final Logger logger = Logger.getLogger(Poll.class);
	private Link link;

	public Poll(Link link) {
		super();
		this.link = link;
	}

	@Override
	public void run() {
		try {
			BinkpConnector binkpConnector = new BinkpConnector();
			Connector connector = new Connector(binkpConnector);
			logger.info(String.format("Соединяемся с %s (%s:%d)", link.getLinkAddress(), link.getProtocolHost(),
					link.getProtocolPort()));
			connector.connect(link);

		} catch (ProtocolException e) {
			logger.error("Ошибка протокола:" + e.getMessage());
		}
	}
}
