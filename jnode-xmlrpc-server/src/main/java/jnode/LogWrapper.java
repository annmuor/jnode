package jnode;

import jnode.logger.Logger;
import org.apache.commons.logging.Log;

import java.io.Serializable;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class LogWrapper implements Log, Serializable {

    private final Logger logger;

    /*
    fatal 1
    error, warn 2
    info 3
    debug  4
    trace 5
     */

    public LogWrapper(String logname) {
        logger = Logger.getLogger(logname);
    }

    @Override
    public void debug(Object message) {
        logger.l4(String.valueOf(message));
    }

    @Override
    public void debug(Object message, Throwable t) {
        logger.l4(String.valueOf(message), t);
    }

    @Override
    public void error(Object message) {
        logger.l2(String.valueOf(message));
    }

    @Override
    public void error(Object message, Throwable t) {
        logger.l2(String.valueOf(message), t);
    }

    @Override
    public void fatal(Object message) {
        logger.l1(String.valueOf(message));
    }

    @Override
    public void fatal(Object message, Throwable t) {
        logger.l1(String.valueOf(message), t);
    }

    @Override
    public void info(Object message) {
        logger.l3(String.valueOf(message));
    }

    @Override
    public void info(Object message, Throwable t) {
        logger.l3(String.valueOf(message), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isNeedLog4();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isNeedLog2();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isNeedLog1();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isNeedLog3();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isNeedLog5();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isNeedLog2();
    }

    @Override
    public void trace(Object message) {
        logger.l5(String.valueOf(message));
    }

    @Override
    public void trace(Object message, Throwable t) {
        logger.l5(String.valueOf(message), t);
    }

    @Override
    public void warn(Object message) {
        logger.l2(String.valueOf(message));
    }

    @Override
    public void warn(Object message, Throwable t) {
        logger.l2(String.valueOf(message), t);
    }
}
