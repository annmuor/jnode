package org.jnode.rest.fido;

import jnode.dto.Echoarea;
import jnode.dto.Echomail;

public interface EchomailProxy {
    Echomail get(Long id);
    Long writeEchomail(Echoarea area, String subject,
                       String text, String fromName, String toName,
                       String fromFTN, String tearline, String origin);
}
