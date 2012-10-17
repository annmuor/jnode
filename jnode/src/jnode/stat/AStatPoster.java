package jnode.stat;

import jnode.ftn.FtnTools;
import jnode.main.Main;

public abstract class AStatPoster {
	protected abstract String _getSubject();

	protected abstract String _getText();

	public String getText() {
		StringBuilder b = new StringBuilder();
		b.append(String.format("\001MSGID: %s %s\n\001PID: %s\n\001TID: %s\n\n",
				Main.info.getAddress().toString(), FtnTools.generate8d(),
				Main.info.getVersion(), Main.info.getVersion()));
		b.append(_getText());
		b.append("\n--- " + this.getClass().getCanonicalName() + " robot\n");
		b.append(" * Origin: " + Main.info.getVersion() + " ("
				+ Main.info.getAddress().toString() + ")\n");
		return b.toString();
	}

	public String getSubject() {
		return _getSubject();
	}
}
