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

package org.jnode.mail.robot;

import java.util.Date;
import java.util.Hashtable;

import jnode.event.Notifier;
import jnode.event.SharedModuleEvent;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.main.MainHandler;
import jnode.robot.IRobot;

public class MailSender implements IRobot {
	private Hashtable<FtnAddress, Long> timeoutMap;
	private static final long MAIL_TIMEOUT = 3600;
	private static final String MAIL_REGEXP = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String MAIL_FORMAT = "Netmail2Email Gate greets you!\nYou have received this message because somebody specified it for receiving this mail\nPlease, contact gate holder if this message annoys you\n\nNetmail from: %s <%s>\n\n%s\n\n-- "
			+ MainHandler.getVersion();

	public MailSender() {
		timeoutMap = new Hashtable<>();
	}

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		if (checkTimeout(fmsg)) {
			if (send(fmsg)) {
				reply(fmsg, "Your message was transferred to mail queue");
			} else {
				reply(fmsg, "Sorry, email address you provided is wrong");
			}
		} else {
			reply(fmsg, "Sorry, you can't send more than 1 message per "
					+ MAIL_TIMEOUT + " seconds");
		}
	}

	private boolean send(FtnMessage fmsg) {
		String to = null;
		String subject = "[FIDO] MailSender message";
		String text = fmsg.getText().replace('\001', '@');
		if (fmsg.getSubject().matches(MAIL_REGEXP)) {
			to = fmsg.getSubject();
		} else {
			subject = "[FIDO] " + fmsg.getSubject();
			for (String line : text.split("\n")) {
				if (line.matches(MAIL_REGEXP)) {
					to = line;
					text = text.replace(to + "\n", "");
					break;
				}
			}
		}
		if (to != null) {

			Notifier.INSTANSE.notify(new SharedModuleEvent(
					"org.jnode.mail.MailModule", "to", to, "subject", subject,
					"text", String.format(MAIL_FORMAT, fmsg.getFromName(), fmsg
							.getFromAddr().toString(), text)));
			timeoutMap.put(fmsg.getFromAddr(), new Date().getTime());
			return true;
		}
		return false;
	}

	private boolean checkTimeout(FtnMessage fmsg) {
		long now = new Date().getTime();
		Long last = timeoutMap.get(fmsg.getFromAddr());
		if (last != null) {
			return (now - MAIL_TIMEOUT * 1000 > last);
		}
		return true;
	}

	public void reply(FtnMessage fmsg, String text) {
		FtnTools.writeReply(fmsg, "MailSender reply", text);
	}

}
