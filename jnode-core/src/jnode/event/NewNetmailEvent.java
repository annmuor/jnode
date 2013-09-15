package jnode.event;

import jnode.dto.Netmail;

public class NewNetmailEvent implements IEvent {
	private Netmail netmail;

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
