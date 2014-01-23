package org.jnode.nntp;

import java.util.Collection;

public interface Processor {

    String process(Collection<String> params);
}
