package jnode.main.threads;

import java.util.List;

import jnode.dto.Link;
import jnode.ftn.tosser.FtnTosser;
import jnode.ftn.types.FtnAddress;
import jnode.protocol.io.Message;

public class TosserQueue {
	private FtnTosser tosser;

	private TosserQueue() {
		tosser = new FtnTosser();
	}

	private static TosserQueue self;

	public synchronized static TosserQueue getInstanse() {
		if (self == null) {
			self = new TosserQueue();
		}
		return self;
	}

	public synchronized void toss() {
		tosser.tossInboundDirectory();
		tosser.end();
	}

	public List<Message> getMessages(Link link) {
		return tosser.getMessages2(new FtnAddress(link.getLinkAddress()));
	}
	
	public List<Message> getMessages(FtnAddress address) {
		return tosser.getMessages2(address);
	}
}
