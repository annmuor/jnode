package org.jnode.xmpp;

import java.util.HashMap;

import org.jnode.xmpp.commands.CommandProcessor;
import org.jnode.xmpp.commands.DummyCommandProcessor;
import org.jnode.xmpp.commands.HelpCommandProcessor;
import org.jnode.xmpp.commands.QuitCommandProcessor;

/**
 * Управление командами
 * 
 * @author kreon
 * 
 */
public class ControlTools {
	private static final HashMap<String, CommandProcessor> commandTable = createCommandTable();

	private static HashMap<String, CommandProcessor> createCommandTable() {
		HashMap<String, CommandProcessor> table = new HashMap<String, CommandProcessor>();
		// TODO: реализовать
		table.put("HELP", new HelpCommandProcessor());
		table.put("QUIT", new QuitCommandProcessor());
		table.put("NEW", new DummyCommandProcessor());
		table.put("REMOVE", new DummyCommandProcessor());
		table.put("LIST", new DummyCommandProcessor());
		table.put("EDIT", new DummyCommandProcessor());
		return table;
	}

	public static String processCommand(String command) {
		for (String key : commandTable.keySet()) {
			if (command.toUpperCase().startsWith(key)) {
				return commandTable.get(key).process(command);
			}
		}
		return "Unknown command. Type HELP for command list";
	}
}
