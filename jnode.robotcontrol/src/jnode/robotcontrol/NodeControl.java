package jnode.robotcontrol;

import java.io.Writer;
import java.util.regex.Pattern;

import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnMessage;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.robot.IRobot;

/**
 * Робот для контроля ноды
 * 
 * @author kreon
 * 
 */
public class NodeControl implements IRobot {
	private static final Logger logger = Logger.getLogger(NodeControl.class);
	private static final Pattern list_links = Pattern.compile(
			"^show[ ]*links?$", Pattern.CASE_INSENSITIVE);
	private static final Pattern list_rewrites = Pattern.compile(
			"^show[ ]*rewrites?$", Pattern.CASE_INSENSITIVE);
	private static final Pattern list_routes = Pattern.compile("",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern list_echoes = Pattern.compile("",
			Pattern.CASE_INSENSITIVE);

	// private static final Pattern pattern = Pattern.compile("",
	// Pattern.CASE_INSENSITIVE);

	@Override
	public void execute(FtnMessage fmsg) throws Exception {
		// TODO: add security checks
		String password = Main.getProperty("nodecontrol.password", "iamaroot");
		String operator = Main.getProperty("nodecontrol.operator", Main.info
				.getAddress().toString());
		if (!fmsg.getSubject().equals(password)
				|| !fmsg.getFromAddr().toString().equals(operator)) {
			logger.warn("Попытка неавторизованного доступа от "
					+ fmsg.getFromAddr());
			return;
		}
		StringBuilder reply = new StringBuilder();
		for (String line : fmsg.getText().split("\n")) {
			if (list_links.matcher(line).matches()) {
				FtnTools.writeReply(fmsg, "NodeControl: list of links",
						NodeTools.listLinks());
			} else if (list_rewrites.matcher(line).matches()) {
				FtnTools.writeReply(fmsg, "NodeControl: list of rewrites",
						NodeTools.listRewrites());
			}
		}
	}

}
