package jnode.ndl;

import jnode.ftn.types.FtnAddress;

public class FtnNdlAddress extends FtnAddress {
	public static enum Status {
		HOLD, DOWN, HUB, HOST, PVT, NORMAL
	}

	private static final long serialVersionUID = 1L;
	private Status status;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public FtnNdlAddress(String addr, Status status) {
		super(addr);
		this.status = status;
	}

	public FtnNdlAddress(String addr) {
		super(addr);
		this.status = Status.NORMAL;
	}

	public FtnNdlAddress(FtnAddress address) {
		this.zone = address.getZone();
		this.net = address.getNet();
		this.node = address.getNode();
		this.point = address.getPoint();
		this.status = Status.NORMAL;
	}

	public FtnNdlAddress(Status status) {
		super();
		this.status = status;
	}

	public FtnNdlAddress() {
		super();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (!(object instanceof FtnAddress)) {
			return false;
		}
		return object.equals(this);
	}

	@Override
	public String toString() {
		return "FtnNdlAddress [" + super.toString() + ", status=" + status
				+ "]";
	}

}
