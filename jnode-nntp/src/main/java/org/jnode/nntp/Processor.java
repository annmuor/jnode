package org.jnode.nntp;

import java.util.Collection;

public interface Processor {

    Collection<String> process(Collection<String> params, Long selectedGroupId);
}
