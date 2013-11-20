package jnode.rssposter.jscript;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.jscript.IJscriptHelper;
import jnode.logger.Logger;

/**
 * Постит RSS куда надо
 * 
 * @author kreon
 * 
 */
public class RssPosterHelper extends IJscriptHelper {
	private static final Logger logger = Logger
			.getLogger(RssPosterHelper.class);

	@Override
	public Version getVersion() {
		return new Version() {

			@Override
			public int getMinor() {
				return 2;
			}

			@Override
			public int getMajor() {
				return 1;
			}
		};
	}

	/**
	 * Запостить новости за последние сутки
	 */
	public void postNewsToEchoarea(String title, String echoarea, String URL,
			int daysBefore) {
        postNewsToEchoareaInHours(title, echoarea, URL, daysBefore * 24);
	}

    /**
     * Запостить новости за последний час
     */
    public void postNewsToEchoareaInHours(String title, String echoarea, String URL,
                                   int hoursBefore) {
        Long date = new Date().getTime() - 3600000 * hoursBefore;
        postNewsToEchoareaSinceX(title, echoarea, URL, new Date(date));
    }

    /**
     * Дата, смещенная относительно текущей на shiftInSec секунд
     * @param shiftInSec смещение в секундах (с минусом - в прошлое, с плюсом - в будущее)
     * @return смещенная дата
     */
    public Date shiftedDate(int shiftInSec){
       return new Date(new Date().getTime() + 1000 * shiftInSec);
    }

	public void postNewsToEchoareaSinceX(String title, String echoarea,
			String url, Date x) {

        logger.l5(MessageFormat.format("postNewsToEchoareaSinceX title = {0}, echoarea = {1}, url = {2}, dateX = {3}",
                title, echoarea, url, x));

		Echoarea area = FtnTools.getAreaByName(echoarea, null);
		if (area == null) {
			logger.l4("No such echoarea - " + echoarea);
			return;
		}
        StringBuilder sb = getText(url, x);
        if (sb != null && sb.length() != 0){
            logger.l5(MessageFormat.format("write in area {0} {1} entries", area, sb.length()));
            FtnTools.writeEchomail(area, title, sb.toString());
        }
	}

    private StringBuilder getText(String URL, Date x) {
        SyndFeedInput feedInput = new SyndFeedInput();
        StringBuilder sb = new StringBuilder();

        try {
            SyndFeed feed = feedInput.build(new XmlReader(new java.net.URL(URL)));
            if (feed.getPublishedDate() != null && x.after(feed.getPublishedDate())) {
                logger.l4("There's no new entries at " + URL);
                return null;
            }


            for (Object object : feed.getEntries()) {
                SyndEntry entry = (SyndEntry) object;
                if (x.after(entry.getPublishedDate())) {
                    logger.l5(MessageFormat.format("x = {0}, published date = {1} - break", x, entry.getPublishedDate()));
                    break;
                }
                sb.append("*");
                sb.append(entry.getTitle());
                sb.append("*");
                sb.append("\n\n");
                sb.append(entry.getDescription().getValue()
                        .replaceAll("\\<.*?>", ""));
                sb.append("\nRead more: ");
                sb.append(entry.getUri());
                sb.append("\n----------------------------------------------------------------------\n");
            }
        } catch (Exception e) {
            logger.l2("Some error happens while parsing " + URL, e);
        }
        return sb;
    }
}
