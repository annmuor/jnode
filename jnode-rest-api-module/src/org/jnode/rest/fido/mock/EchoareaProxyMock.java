package org.jnode.rest.fido.mock;

import jnode.dto.Echoarea;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;
import org.jnode.rest.fido.EchoareaProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Named("mock-echoareaProxy")
@Singleton
public class EchoareaProxyMock implements EchoareaProxy {

    private List<Echoarea> data = new ArrayList<>();

    private long seq = 0L;

    public EchoareaProxyMock() {
        Echoarea e = new Echoarea();
        e.setDescription("test");
        e.setId(++seq);
        e.setName("828.local");
        data.add(e);
    }

    @Override
    public synchronized Echoarea getAreaByName(String name) {
        if (name == null) {
            return null;
        }
        Optional<Echoarea> result = data.stream().filter(echoarea -> name.equals(echoarea.getName())).findAny();

        return result.isPresent() ? result.get() : null;
    }
}
