package org.jnode.rest.mapper;

import jnode.dto.Echomail;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;

import java.util.HashMap;
import java.util.Map;

@Named("echomailMapper")
@Singleton
public class EchomailMapper {

    @Inject
    @Named("echoareaMapper")
    private EchoareaMapper echoareaMapper;

    @Inject
    @Named("dateMapper")
    private DateMapper dateMapper;

    public Map<String, Object> toJsonType(Echomail value) {
        if (value == null){
            return null;
        }

        Map<String, Object> r = new HashMap<>();

        r.put("area", echoareaMapper.toJsonType(value.getArea()));
        r.put("date", dateMapper.toJsonType(value.getDate()));
        r.put("fromFTN", value.getFromFTN());
        r.put("fromName", value.getFromName());
        r.put("id", value.getId());
        r.put("msgid", value.getMsgid());
        r.put("path", value.getPath());
        r.put("seenBy", value.getSeenBy());
        r.put("subject", value.getSubject());
        r.put("text", value.getText());
        r.put("toName", value.getToName());

        return r;
    }

    public void setEchoareaMapper(EchoareaMapper echoareaMapper) {
        this.echoareaMapper = echoareaMapper;
    }

    public void setDateMapper(DateMapper dateMapper) {
        this.dateMapper = dateMapper;
    }

}
