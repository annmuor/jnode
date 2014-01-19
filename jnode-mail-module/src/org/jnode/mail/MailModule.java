package org.jnode.mail;

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

	public MailModule(String configFile) throws JnodeModuleException {
		super(configFile);
	}

	@Override
	public void start() {
		service = new EMailService();
		service.setFromAddr(properties.getProperty(CONFIG_FROM,
				"root@localhost"));
		service.setHost(properties.getProperty(CONFIG_HOST, "127.0.0.1"));
		service.setPort(properties.getProperty(CONFIG_PORT, "25"));
		service.setUserName(properties.getProperty(CONFIG_USER, "root"));
		service.setPassWord(properties.getProperty(CONFIG_PASSWORD, "root"));
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
	}

	@Override
	public void handle(IEvent event) {
		if (event instanceof SharedModuleEvent) {
			SharedModuleEvent e = (SharedModuleEvent) event;
			if (getClass().getCanonicalName().equals(e.to())) {
				proccessEvent(e);
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
		} catch (RuntimeException ignore) {
			logger.l2("Event from " + e.from() + " has invalid args");
		} catch (Exception err) {
			logger.l2("Mail send failed", err);
		}
	}

}
