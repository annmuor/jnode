package org.jnode.rest.fido.mock;

import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.logger.Logger;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.EchomailProxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Named("mock-echomailProxy")
@Singleton
public class EchomailProxyMock implements EchomailProxy {

    private static final Logger LOGGER = Logger.getLogger(EchomailProxyMock.class);

    private long seq = 0L;
    private List<Echomail> data = new ArrayList<>();

    @Override
    public synchronized Echomail get(Long id) {
        final Optional<Echomail> res = data.stream().filter(echomail -> id.equals(echomail.getId())).findAny();
        return res.isPresent() ? res.get() : null;
    }

    @Override
    public synchronized Long writeEchomail(Echoarea area, String subject, String text, String fromName, String toName, String fromFTN, String tearline, String origin) {

        Echomail e = new Echomail();
        e.setArea(area);
        e.setDate(new Date());
        e.setFromFTN(fromFTN);
        e.setFromName(fromName);
        e.setId(++seq);
        e.setMsgid(String.valueOf(++seq));
        e.setSubject(subject);
        e.setText(text+tearline+origin);
        e.setToName(toName);
        data.add(e);

        LOGGER.l5("save " + e);

        return e.getId();
    }
}
