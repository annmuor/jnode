/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jnode.xmpp;

import java.util.HashMap;

import org.jnode.xmpp.commands.CommandProcessor;
import org.jnode.xmpp.commands.DummyCommandProcessor;
import org.jnode.xmpp.commands.HelpCommandProcessor;
import org.jnode.xmpp.commands.ListCommandProcessor;
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
		HashMap<String, CommandProcessor> table = new HashMap<>();
		// TODO: реализовать
		table.put("HELP", new HelpCommandProcessor());
		table.put("QUIT", new QuitCommandProcessor());
		table.put("LIST", new ListCommandProcessor());
		table.put("NEW", new DummyCommandProcessor());
		table.put("REMOVE", new DummyCommandProcessor());
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
