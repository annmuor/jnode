package jnode.main.threads;

import jnode.ftn.tosser.FtnTosser;

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
}
