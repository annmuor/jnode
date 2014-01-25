package jnode.logger;

import jnode.core.ConcurrentDateFormatAccess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public final class Logger {
    public static final int LOG_L5 = 5;
    public static final int LOG_L4 = 4;
    public static final int LOG_L3 = 3;
    public static final int LOG_L2 = 2;
    public static final int LOG_L1 = 1;
    public static int Loglevel = LOG_L5;

    private final String className;
    @SuppressWarnings("unused")
    private static final String LOG_FORMAT = "%s [%08d] %s %s";
    private static final ConcurrentDateFormatAccess DATE_FORMAT = new ConcurrentDateFormatAccess("dd-MM-yy HH:mm:ss");

    public static Logger getLogger(Class<?> clazz) {
        String className = clazz.getSimpleName();
        return getLogger(className);
    }

    public static Logger getLogger(String name) {
        StringBuilder b = new StringBuilder(20);
        b.append(name);
        for (int i = b.length(); i < 20; i++) {
            b.append(' ');
        }
        return new Logger(b.toString());
    }

    private Logger(String className) {
        this.className = className;
    }

    private boolean isNeedLog(int type) {
        return Loglevel >= type;
    }

    private void log(int type, String log) {
        if (isNeedLog(type)) {
            System.out.println(String.format(LOG_FORMAT, DATE_FORMAT.currentDateAsString(), Thread.currentThread()
                    .getId(), className, log));
        }
    }

    private String th2s(Throwable e) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        e.printStackTrace(ps);
        ps.close();
        try {
            bos.close();
        } catch (IOException e1) {
            return "[LOGGING INTERNAL ERROR]" + bos.toString();
        }
        return bos.toString();
    }

    public boolean isNeedLog5() {
        return isNeedLog(LOG_L5);
    }

    public boolean isNeedLog4() {
        return isNeedLog(LOG_L4);
    }

    public boolean isNeedLog3() {
        return isNeedLog(LOG_L3);
    }

    public boolean isNeedLog2() {
        return isNeedLog(LOG_L2);
    }

    public boolean isNeedLog1() {
        return isNeedLog(LOG_L1);
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
        log(LOG_L1, log + ": " + th2s(e));
    }

    public void l4(String log, Throwable e) {
        log(LOG_L1, log + ": " + th2s(e));
    }

    public void l3(String log, Throwable e) {
        log(LOG_L1, log + ": " + th2s(e));
    }

    public void l2(String log, Throwable e) {
        log(LOG_L1, log + ": " + th2s(e));
    }

    public void l1(String log, Throwable e) {
        log(LOG_L1, log + ": " + th2s(e));
    }

}
