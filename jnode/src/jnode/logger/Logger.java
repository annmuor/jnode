package jnode.logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Logger {
	public static final int LOG_DEBG = 5;
	public static final int LOG_INFO = 4;
	public static final int LOG_WARN = 3;
	public static final int LOG_ERRR = 2;
	public static int Loglevel = LOG_DEBG;

	private String className;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"HH:mm:ss");
	private static final String LOG_FORMAT = "%s [%s] (tid:%04d,cnt:%04d) %s %s";

	public static Logger getLogger(Class<?> clazz) {
		String className = clazz.getSimpleName();
		StringBuilder b = new StringBuilder(20);
		b.append(className);
		for (int i = b.length(); i < 20; i++) {
			b.append(' ');
		}
		return new Logger(b.toString());
	}

	private Logger(String className) {
		this.className = className;
	}

	private void log(int _type, String log) {
		if (Loglevel >= _type) {
			String type = (_type == 2) ? "ERRR" : (_type == 3) ? "WARN"
					: (_type == 4) ? "INFO" : (_type == 5) ? "DEBG" : "";
			System.out.println(String.format(LOG_FORMAT, type, DATE_FORMAT
					.format(new Date()), Thread.currentThread().getId(), Thread
					.activeCount(), className, log));
		}
	}

	public void debug(String log) {
		log(LOG_DEBG, log);
	}

	public void info(String log) {
		log(LOG_INFO, log);
	}

	public void warn(String log) {
		log(LOG_WARN, log);
	}

	public void error(String log) {
		log(LOG_ERRR, log);
	}

	public void debug(String log, Throwable e) {
		log(LOG_DEBG, log);
		e.printStackTrace();
	}

	public void info(String log, Throwable e) {
		log(LOG_INFO, log);
		e.printStackTrace();
	}

	public void warn(String log, Throwable e) {
		log(LOG_WARN, log);
		e.printStackTrace();
	}

	public void error(String log, Throwable e) {
		log(LOG_ERRR, log);
		e.printStackTrace();
	}

}
