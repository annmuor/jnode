package jnode.logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Logger {
	public static final int LOG_L5 = 5;
	public static final int LOG_L4 = 4;
	public static final int LOG_L3 = 3;
	public static final int LOG_L2 = 2;
	public static final int LOG_L1 = 1;
	public static int Loglevel = LOG_L5;

	private String className;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"HH:mm:ss");
	private static final String LOG_FORMAT = "%s [%08d] %s %s";

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
			System.out.println(String.format(LOG_FORMAT, DATE_FORMAT
					.format(new Date()), Thread.currentThread().getId(),
					className, log));
		}
	}

	private String th2s(Throwable e) {
		StringBuilder th = new StringBuilder();
		for (StackTraceElement el : e.getStackTrace()) {
			th.append(el.toString());
			th.append('\n');
		}
		return th.toString();
	}

	public void l5(String log) {
		log(LOG_L5, log);
	}

	public void l4(String log) {
		log(LOG_L4, log);
	}

	public void l3(String log) {
		log(LOG_L3, log);
	}

	public void l2(String log) {
		log(LOG_L2, log);
	}

	public void l1(String log) {
		log(LOG_L1, log);
	}

	public void l5(String log, Throwable e) {
		log(LOG_L5, log);
		log(LOG_L2, th2s(e));
	}

	public void l4(String log, Throwable e) {
		log(LOG_L4, log);
		log(LOG_L2, th2s(e));
	}

	public void l3(String log, Throwable e) {
		log(LOG_L3, log);
		log(LOG_L2, th2s(e));
	}

	public void l2(String log, Throwable e) {
		log(LOG_L2, log);
		log(LOG_L2, th2s(e));
	}

	public void l1(String log, Throwable e) {
		log(LOG_L1, log);
		log(LOG_L2, th2s(e));
	}

}
