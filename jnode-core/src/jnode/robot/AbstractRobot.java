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

package jnode.robot;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.dto.Link;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public abstract class AbstractRobot implements IRobot {
	protected static final String YOU_ARE_NOT_IN_LINKS_OF_ORIGIN = "You are not in links of origin";
	protected static final String ACCESS_DENIED = "Access denied";
	protected static final String YOU_ARE_NOT_WELCOME = "You are not welcome";
	protected static final String WRONG_PASSWORD = "Wrong password";
	protected static final String SORRY_0_IS_OFF_FOR_YOU = "Sorry, {0} is off for you";
	protected static final String WRONG_ASLINK = "%ASLINK command with wrong arg!";
	protected static final Pattern aslink = Pattern
			.compile(
					"^%ASLINK ((\\d)?:?(\\d{1,5})/(\\d{1,5})\\.?(\\d{1,5})?@?(\\S+)?)$",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	protected static final Pattern HELP = Pattern.compile("^%HELP$",
			Pattern.CASE_INSENSITIVE);
	protected static final String UNKNOWN_COMMAND_0 = "Unknown command {0}\n";

	protected abstract String getRobotName();

	protected abstract boolean isEnabled(Link link);

	protected abstract String getPasswordOption();

	protected Link getAndCheckLink(FtnMessage fmsg) {
		FtnAddress linkAddress = fmsg.getFromAddr();
		// check AS_LINK
		{
			Matcher m = aslink.matcher(fmsg.getText());
			if (m.find()) {
				try {
					linkAddress = new FtnAddress(m.group(1));
				} catch (NumberFormatException e) {
					FtnTools.writeReply(fmsg, ACCESS_DENIED, WRONG_ASLINK);
					return null;
				}
			}
		}
		Link link = FtnTools.getLinkByFtnAddress(linkAddress);
		{
			if (link == null) {
				FtnTools.writeReply(fmsg, ACCESS_DENIED,
						YOU_ARE_NOT_IN_LINKS_OF_ORIGIN);
				return null;
			}
		}
		if (!isEnabled(link)) {
			FtnTools.writeReply(fmsg, YOU_ARE_NOT_WELCOME, MessageFormat
					.format(SORRY_0_IS_OFF_FOR_YOU, getRobotName()));
			return null;
		}
		{
			String password = getPassword(link);
			if (!password.equals(fmsg.getSubject())) {
				FtnTools.writeReply(fmsg, ACCESS_DENIED, WRONG_PASSWORD);
				return null;
			}
		}
		return link;
	}

	protected String getPassword(Link link) {
		if (link == null) {
			return "";
		}
		String password = FtnTools.getOptionString(link, getPasswordOption());
		if ("".equals(password)) {
			password = link.getPaketPassword();
		}
		return password;
	}

	protected abstract String help();

}
