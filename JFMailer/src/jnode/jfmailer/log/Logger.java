package jnode.jfmailer.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Logger {
	private final static String LOG_FORMAT = " [%s] - %s\n";
	private final static DateFormat format = new SimpleDateFormat("HH:mm:ss");
	private static StringBuilder sb = new StringBuilder();

	public static void log(String text) {
		String log = String.format(LOG_FORMAT, format.format(new Date()), text);
		sb.append(log);
	}

	public static String getLog() {
		String text = sb.toString();
		sb = new StringBuilder();
		return text;
	}

}
