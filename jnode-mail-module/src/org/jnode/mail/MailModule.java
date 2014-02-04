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

package org.jnode.mail;

import java.util.LinkedList;
import java.util.Map;

import jnode.dto.Robot;
import jnode.event.IEvent;
import jnode.event.SharedModuleEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;

import org.jnode.mail.service.EMailService;

public class MailModule extends JnodeModule {
	private static final String CONFIG_HOST = "smtp.host";
	private static final String CONFIG_PORT = "smtp.port";
	private static final String CONFIG_USER = "smtp.user";
	private static final String CONFIG_PASSWORD = "smtp.password";
	private static final String CONFIG_FROM = "smtp.from";
	private static final String CONFIG_ROBOT = "robot";

	private static final Logger logger = Logger.getLogger(MailModule.class);
	private EMailService service;
	private LinkedList<SharedModuleEvent> queue;

	public MailModule(String configFile) throws JnodeModuleException {
		super(configFile);
		queue = new LinkedList<>();
	}

	@Override
	public void start() {
		service = new EMailService();
		service.setFromAddr(properties.getProperty(CONFIG_FROM,
				"root@localhost"));
		service.setHost(properties.getProperty(CONFIG_HOST, "127.0.0.1"));
		service.setPort(properties.getProperty(CONFIG_PORT, "25"));
		service.setUsername(properties.getProperty(CONFIG_USER));
		service.setPassword(properties.getProperty(CONFIG_PASSWORD));
		String robotName = properties.getProperty(CONFIG_ROBOT, "mailsender");
		Robot r = ORMManager.get(Robot.class).getById(robotName);
		if (r == null) {
			r = new Robot();
			r.setRobot(robotName);
			r.setClassName("org.jnode.mail.robot.MailSender");
			ORMManager.get(Robot.class).save(r);
			logger.l2("Robot " + robotName + " create by MailModule");
		}
		logger.l3("Mail service started");
		while (true) {
			synchronized (this) {
				if (queue.isEmpty()) {
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			SharedModuleEvent e = queue.removeFirst();
			proccessEvent(e);
		}
	}

	@Override
	public void handle(IEvent event) {
		if (event instanceof SharedModuleEvent) {
			SharedModuleEvent e = (SharedModuleEvent) event;
			if (getClass().getCanonicalName().equals(e.to())) {
				synchronized (this) {
					queue.addLast(e);
					this.notify();
				}
			}
		}

	}

	private void proccessEvent(SharedModuleEvent e) {
		Map<String, Object> data = e.params();
		try {
			String to = data.get("to").toString();
			String subject = data.get("subject").toString();
			String text = data.get("text").toString();
			service.sendEMail(to, subject, text);
			logger.l3("Mail to " + to + " sent");
		} catch (RuntimeException ignore) {
			logger.l2("Event from " + e.from() + " has invalid args");
		} catch (Exception err) {
			logger.l2("Mail send failed", err);
		}
	}

}
