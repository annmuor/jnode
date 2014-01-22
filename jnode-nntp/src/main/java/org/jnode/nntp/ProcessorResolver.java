package org.jnode.nntp;

import com.google.common.collect.Maps;
import org.jnode.nntp.processor.ModeReaderProcessor;

import java.util.Map;

public class ProcessorResolver {

    private static final Map<NntpCommand, Processor> map = Maps.newHashMap();

    static {
        map.put(NntpCommand.MODE_READER, new ModeReaderProcessor());
    }

    public static Processor processor(NntpCommand command) {
        // todo map.contains

        return map.get(command);
    }

}
