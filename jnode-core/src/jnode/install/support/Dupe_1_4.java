package jnode.install.support;

import jnode.dto.Echoarea;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author kreon
 */
@DatabaseTable(tableName = "dupes")
public class Dupe_1_4 {
    @DatabaseField(columnName = "msgid", index = true)
    private String msgid;
    @DatabaseField(columnName = "echoarea_id", foreign = true)
    private Echoarea echoarea;

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public Echoarea getEchoarea() {
        return echoarea;
    }

    public void setEchoarea(Echoarea echoarea) {
        this.echoarea = echoarea;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dupe{");
        sb.append("msgid='").append(msgid).append('\'');
        sb.append(", echoarea=").append(echoarea);
        sb.append('}');
        return sb.toString();
    }
}
