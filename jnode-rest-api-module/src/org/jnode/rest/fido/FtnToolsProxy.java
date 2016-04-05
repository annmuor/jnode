package org.jnode.rest.fido;

import jnode.dto.Echoarea;
import jnode.dto.Link;

public interface FtnToolsProxy {
    Echoarea getAreaByName(String name, Link link);
    Long writeEchomail(Echoarea area, String subject,
                       String text, String fromName, String toName);
    String defaultEchoFromName();
    String defaultEchoToName();
}
