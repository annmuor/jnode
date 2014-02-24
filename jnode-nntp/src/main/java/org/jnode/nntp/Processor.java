package org.jnode.nntp;

import org.jnode.nntp.model.Auth;

import java.util.Collection;

public interface Processor {

    Collection<String> process(Collection<String> params, Long selectedGroupId, Long selectedArticleId, Auth auth);
}
