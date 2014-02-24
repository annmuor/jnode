package org.jnode.nntp.processor;


import org.jnode.nntp.Processor;
import org.jnode.nntp.exception.EndOfSessionException;
import org.jnode.nntp.model.Auth;

import java.util.Collection;

public class QuitProcessor implements Processor {

    @Override
    public Collection<String> process(Collection<String> params, Long id, Long selectedArticleId, Auth auth) {
        throw new EndOfSessionException();
    }
}
