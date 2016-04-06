package org.jnode.rest.mapper;

import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Named("dateMapper")
@Singleton
public class DateMapper {

    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public String toJsonType(Date value){

        if (value == null){
            return null;
        }

        DateFormat df = new SimpleDateFormat(ISO_FORMAT);
        return df.format(value);
    }
}
