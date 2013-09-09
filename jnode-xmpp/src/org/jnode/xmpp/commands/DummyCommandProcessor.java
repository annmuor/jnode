package org.jnode.xmpp.commands;

/**
 * Пустой "процессор" для еще несделанных фич
 * 
 * @author kreon
 * 
 */
public class DummyCommandProcessor implements CommandProcessor {

	@Override
	public String proccess(String command) {
		return "Not realized yet";
	}

}
