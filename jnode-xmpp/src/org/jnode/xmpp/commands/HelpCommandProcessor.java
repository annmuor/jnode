package org.jnode.xmpp.commands;

public class HelpCommandProcessor implements CommandProcessor {
	private final static String HELP = "LIST <area [name=*]|link [ftn=*]|routing [via=*]|subscription [ftn=*|echo=*]> [limit=N]\n"
			+ "EDIT <link ftn=* [password=password] [ftn=ftn] [flags=flags]|echo name=* [description=*]>\n"
			+ "REMOVE <echo name=*|link ftn=*|subscription ftn=* echo=*>\n"
			+ "NEW <link ftn=* pwd=* [pkt_pwd=*|host=*|port=*|flags=*]|echo name=* description=*|subscription ftn=* echo=*>\n"
			+ "QUIT [key=*]\n";

	@Override
	public String process(String command) {
		return HELP;
	}

}
