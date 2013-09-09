package org.jnode.xmpp.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuitCommandProcessor implements CommandProcessor {
	private String key = generateKey();
	private static final Pattern pQuit = Pattern.compile("QUIT[ ]+key=(.*)");
	@Override
	public String process(String command) {
		
		Matcher m = pQuit.matcher(command);
		if(m.matches()) {
			if(key.equals(m.group(1))) {
				System.exit(0);
				return null;
			} else {
				return "Invalid key. Type QUIT to regenerate key";
			}
		} else {
			key = generateKey();
			return "Are you sure? reply QUIT key="+key+" to real exit";
		}
	}

	private String generateKey() {
		char[] key = new char[48];
		for (int i = 0; i < key.length; i++) {
			key[i] = (char) ((booleanRandom()) ? intRandom('a', 'z')
					: intRandom('A', 'Z'));
		}
		return new String(key);
	}

	private boolean booleanRandom() {
		return Math.random() > 0.5;
	}

	private int intRandom(int min, int max) {
		return (int) (min + (Math.random() * (double) (max - min)));
	}

}
