package jnode.event;

import jnode.dto.Echomail;

public class NewEchomailEvent implements IEvent {
	private Echomail echomail;

	public NewEchomailEvent(Echomail echomail) {
		super();
		this.echomail = echomail;
	}

	public Echomail getEchomail() {
		return echomail;
	}

	@Override
	public String getEvent() {
		return "";
	}

}
