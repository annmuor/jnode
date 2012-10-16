package jnode.main.threads;

import jnode.ftn.tosser.FtnTosser;

public enum TosserQueue {
	INSTANSE;
	public void toss() {
		FtnTosser tosser = new FtnTosser();
		tosser.tossInbound();
		tosser.end();
	}
}
