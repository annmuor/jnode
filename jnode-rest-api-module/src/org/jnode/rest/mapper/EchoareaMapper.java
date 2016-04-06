package org.jnode.rest.mapper;

import jnode.dto.Echoarea;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;

import java.util.HashMap;
import java.util.Map;

@Named("echoareaMapper")
@Singleton
public class EchoareaMapper {
    public Map<String, Object> toJsonType(Echoarea value) {

        if (value == null) {
            return null;
        }

        Map<String, Object> r = new HashMap<>();
        r.put("description", value.getDescription());
        r.put("group", value.getGroup());
        r.put("id", value.getId());
        r.put("name", value.getName());
        r.put("readlevel", value.getReadlevel());
        r.put("writelevel", value.getWritelevel());

        return r;
    }
}
