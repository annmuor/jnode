package org.jnode.nntp;

import com.google.common.collect.Maps;
import org.jnode.nntp.model.NntpCommand;
import org.jnode.nntp.processor.*;

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
        map.put(NntpCommand.QUIT, new QuitProcessor());
        map.put(NntpCommand.AUTHINFO_USER, new AuthInfoUserProcessor());
        map.put(NntpCommand.AUTHINFO_PASS, new AuthInfoPassProcessor());
        map.put(NntpCommand.POST, new PostProcessor());
    }

    public static Processor processor(NntpCommand command) {
        return map.get(command);
    }

}
