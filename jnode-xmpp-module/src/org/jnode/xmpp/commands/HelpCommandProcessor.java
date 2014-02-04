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
