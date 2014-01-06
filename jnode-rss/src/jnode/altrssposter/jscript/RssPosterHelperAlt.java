package jnode.altrssposter.jscript;

import jnode.altrssposter.RssPoster;
import jnode.altrssposter.Watermarks;
import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.jscript.IJscriptHelper;
import jnode.logger.Logger;

import java.io.FileNotFoundException;
import java.text.MessageFormat;

/**
 * Еще одна постилка RSS
 *
 * @author Manjago
 */
public class RssPosterHelperAlt extends IJscriptHelper {
    private static final Logger logger = Logger
            .getLogger(RssPosterHelperAlt.class);

    @Override
    public Version getVersion() {
        return new Version() {

            @Override
            public int getMinor() {
                return 1;
            }

            @Override
            public int getMajor() {
                return 1;
            }
        };
    }

    /**
     * Постит новости в фидоарию
     *
     * @param title    subject сообщения
     * @param echoarea ария
     * @param url      url источника
     * @param datafile файл с данными
     * @param limit    максимальное количество собщений для положительной величины, либо неограничено
     */
    public void postNewsToEchoarea(String title, String echoarea, String url, String datafile, int limit) {

        logger.l5(MessageFormat.format("postNewsToEchoarea title = {0}, echoarea = {1}, url = {2}",
                title, echoarea, url));

        Echoarea area = FtnTools.getAreaByName(echoarea, null);
        if (area == null) {
            logger.l4("No such echoarea - " + echoarea);
            return;
        }

        try {
            Watermarks watermarks = new Watermarks(datafile);

            StringBuilder sb = RssPoster.getText(url, watermarks, limit);
            if (sb != null && sb.length() != 0) {
                FtnTools.writeEchomail(area, title, sb.toString());
            }

        } catch (FileNotFoundException e) {
            logger.l4(MessageFormat.format("File {0} not found", datafile), e);
        }

    }
}
