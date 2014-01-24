package org.jnode.nntp;

import com.google.common.collect.Maps;
import org.jnode.nntp.model.NntpCommand;
import org.jnode.nntp.processor.ArticleProcessor;
import org.jnode.nntp.processor.GroupProcessor;
import org.jnode.nntp.processor.HeadProcessor;
import org.jnode.nntp.processor.ListProcessor;
import org.jnode.nntp.processor.ModeReaderProcessor;
import org.jnode.nntp.processor.XoverProcessor;

import java.util.Map;

public class ProcessorResolver {

    private static final Map<NntpCommand, Processor> map = Maps.newHashMap();

    static {
        map.put(NntpCommand.MODE_READER, new ModeReaderProcessor());
        map.put(NntpCommand.LIST, new ListProcessor());
        map.put(NntpCommand.GROUP, new GroupProcessor());
        map.put(NntpCommand.XOVER, new XoverProcessor());
        map.put(NntpCommand.HEAD, new HeadProcessor());
        map.put(NntpCommand.ARTICLE, new ArticleProcessor());
    }

    public static Processor processor(NntpCommand command) {
        // todo map.contains

        return map.get(command);
    }

}
