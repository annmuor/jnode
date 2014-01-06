package jnode.event;

import jnode.dto.Netmail;

public class NewNetmailEvent implements IEvent {
	private final Netmail netmail;

	public Netmail getNetmail() {
		return netmail;
	}

	public NewNetmailEvent(Netmail netmail) {
		super();
		this.netmail = netmail;
	}

	@Override
	public String getEvent() {
		return "";
	}

}
