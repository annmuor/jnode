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

    public boolean isDirtySilence() {
        return dirtySilence;
    }

    // пока что грязно захачим
    private final boolean dirtySilence;

    public LogWrapper(String logname) {
        dirtySilence = logname != null && logname.contains("ormlite");
        logger = Logger.getLogger(logname);
    }

    @Override
    public void debug(Object message) {
        if (isDirtySilence()){
            return;
        }
        logger.l4(String.valueOf(message));
    }

    @Override
    public void debug(Object message, Throwable t) {
        if (isDirtySilence()){
            return;
        }
        logger.l4(String.valueOf(message), t);
    }

    @Override
    public void error(Object message) {
        if (isDirtySilence()){
            return;
        }
        logger.l2(String.valueOf(message));
    }

    @Override
    public void error(Object message, Throwable t) {
        if (isDirtySilence()){
            return;
        }
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
        if (isDirtySilence()){
            return;
        }
        logger.l3(String.valueOf(message));
    }

    @Override
    public void info(Object message, Throwable t) {
        if (isDirtySilence()){
            return;
        }
        logger.l3(String.valueOf(message), t);
    }

    @Override
    public boolean isDebugEnabled() {
        if (isDirtySilence()){
            return false;
        }
        return logger.isNeedLog4();
    }

    @Override
    public boolean isErrorEnabled() {
        if (isDirtySilence()){
            return false;
        }
        return logger.isNeedLog2();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isNeedLog1();
    }

    @Override
    public boolean isInfoEnabled() {
        if (isDirtySilence()){
            return false;
        }
        return logger.isNeedLog3();
    }

    @Override
    public boolean isTraceEnabled() {
        if (isDirtySilence()){
            return false;
        }
        return logger.isNeedLog5();
    }

    @Override
    public boolean isWarnEnabled() {
        if (isDirtySilence()){
            return false;
        }
        return logger.isNeedLog2();
    }

    @Override
    public void trace(Object message) {
        if (isDirtySilence()){
            return;
        }
        logger.l5(String.valueOf(message));
    }

    @Override
    public void trace(Object message, Throwable t) {
        if (isDirtySilence()){
            return;
        }
        logger.l5(String.valueOf(message), t);
    }

    @Override
    public void warn(Object message) {
        if (isDirtySilence()){
            return;
        }
        logger.l2(String.valueOf(message));
    }

    @Override
    public void warn(Object message, Throwable t) {
        if (isDirtySilence()){
            return;
        }
        logger.l2(String.valueOf(message), t);
    }
}
