package jnode.jscript;

import com.j256.ormlite.dao.GenericRawResults;
import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.orm.ORMManager;
import jnode.report.ReportBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class ReportHelper extends IJscriptHelper {

    private final Logger logger = Logger
            .getLogger(getClass());

    @Override
    public Version getVersion() {
        return new Version() {

            @Override
            public int getMinor() {
                return 1;
            }

            @Override
            public int getMajor() {
                return 0;
            }
        };
    }

    public void report(String echoarea, String subject, String sql, String headers, String colLen) {

        GenericRawResults<String[]> results = ORMManager.INSTANSE
                .getEchomailDAO()
                .getRaw(sql);

        if (results == null) {
            return;
        }

        List<String[]> res;
        try {
            res = results.getResults();
        } catch (SQLException e) {
            logger.l3("sql problem", e);
            return;
        }

        if (res == null || res.size() == 0) {
            return;
        }

        ReportBuilder builder = new ReportBuilder();
        builder.setColumns(headers, ",");
        builder.setColLength(colLen, ",");

        for (String[] items : res) {
            builder.printLine(items);
        }

        String text = builder.getText().toString();
        if (text.length() != 0) {
            Echoarea area = FtnTools.getAreaByName(echoarea,
                    null);
            FtnTools.writeEchomail(area, subject, text);
            logger.l5("send message to " + echoarea);
        }
    }
}
