package org.jnode.nntp.processor;

import org.jnode.nntp.Processor;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NntpResponse;

import java.util.Arrays;
import java.util.Collection;

public class ModeReaderProcessor implements Processor {

    @Override
    public Collection<String> process(Collection<String> params, Long id, Long selectedArticleId, Auth auth) {
        return Arrays.asList(NntpResponse.ModeReader.POSTING_ALLOWED);
    }
}
