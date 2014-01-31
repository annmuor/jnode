package org.jnode.nntp.processor;

import org.jnode.nntp.Processor;

import java.util.Arrays;
import java.util.Collection;

public class ModeReaderProcessor implements Processor {

    @Override
    public Collection<String> process(Collection<String> params) {
        return Arrays.asList("200 Posting allowed");
    }
}
