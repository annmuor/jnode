package jnode.ndl;

import java.io.Serializable;

import jnode.ftn.types.FtnAddress;

class NodelistIndex implements Serializable {
	private static final long serialVersionUID = 1L;
	private FtnNdlAddress[] nodelist;
	private Long timestamp;

	public FtnNdlAddress exists(FtnAddress address) {
		FtnAddress addr = address.clone();
		addr.setPoint(0);
		for (FtnNdlAddress a : nodelist) {
			if (a.equals(addr)) {
				return a;
			}
		}
		return null;
	}

	public NodelistIndex() {

	}

	public NodelistIndex(FtnNdlAddress[] nodelist, Long timestamp) {
		super();
		this.nodelist = nodelist;
		this.timestamp = timestamp;
	}

	public Long getTimestamp() {
		return timestamp;
	}

}
