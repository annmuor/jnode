package jnode.dto;

import java.util.Date;

public interface Mail {
    void setFromName(String fromName);

    void setToName(String toName);

    void setFromFTN(String fromFTN);

    void setSubject(String subject);

    void setText(String text);

    void setDate(Date date);
}
