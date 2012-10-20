package jnode.event;

import jnode.ftn.types.FtnAddress;

public class ConnectionEndEvent implements IEvent {
	private int bytesReceived;
	private int bytesSended;
	private FtnAddress address;
	private boolean incoming;
	private boolean success;

	@Override
	public String getEvent() {
		return "";
	}

	public ConnectionEndEvent() {
		super();
	}

	public ConnectionEndEvent(FtnAddress address, boolean incoming,
			boolean success, int bytesReceived, int bytesSended) {
		super();
		this.bytesReceived = bytesReceived;
		this.bytesSended = bytesSended;
		this.address = address;
		this.incoming = incoming;
		this.success = success;
	}

	public ConnectionEndEvent(boolean incoming, boolean success) {
		super();
		this.incoming = incoming;
		this.success = success;
		bytesReceived = 0;
		bytesSended = 0;
		address = null;
	}

	public int getBytesReceived() {
		return bytesReceived;
	}

	public int getBytesSended() {
		return bytesSended;
	}

	public FtnAddress getAddress() {
		return address;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public boolean isSuccess() {
		return success;
	}

	@Override
	public String toString() {
		return "ConnectionEndEvent [bytesReceived=" + bytesReceived
				+ ", bytesSended=" + bytesSended + ", address=" + address
				+ ", incoming=" + incoming + ", success=" + success + "]";
	}

}
