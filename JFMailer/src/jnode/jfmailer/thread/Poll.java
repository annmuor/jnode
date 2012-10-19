package jnode.jfmailer.thread;

import jnode.jfmailer.log.Logger;
import jnode.protocol.io.Connector;
import jnode.protocol.io.exception.ProtocolException;

public class Poll extends Thread {
	public void run() {
		try {
			new Connector().connect();
		} catch (ProtocolException e) {
			Logger.log("Protocol Error: " + e.getMessage());
		}
		synchronized (Poll.class) {
			Poll.class.notifyAll();
		}
	}
}
