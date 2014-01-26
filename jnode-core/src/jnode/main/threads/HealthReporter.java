package jnode.main.threads;

import jnode.core.SysInfo;
import jnode.logger.Logger;

import java.text.MessageFormat;
import java.util.TimerTask;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class HealthReporter extends TimerTask {

    private final Logger logger = Logger
            .getLogger(getClass());

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();

        SysInfo.OpenFilesInfo openFilesInfo = SysInfo.openFilesInfo();
        if (openFilesInfo != null) {
            sb.append(MessageFormat.format("open files = {0,number,#########}/{1,number,##########}; ", openFilesInfo.getOpenFiles(), openFilesInfo.getMaxOpenFiles()));
        }

        SysInfo.MemoryInfo memoryInfo = SysInfo.memoryInfo();
        sb.append(MessageFormat.format("memory usage: max = {0,number,#########} MB, total = {1,number,#########} MB, free = {2,number,#########} MB; ",
                memoryInfo.getMax(), memoryInfo.getTotal(), memoryInfo.getFree()));

        SysInfo.ThreadInfo threadInfo = SysInfo.threadInfo();
        sb.append(MessageFormat.format("running threads {0,number,#########}", threadInfo.getRunningThreads()));

        logger.l5(sb.toString());
    }

}
