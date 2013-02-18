package jnode.rssposter.jscript;

import java.net.URL;
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
				return 1;
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
		Long date = new Date().getTime() - 24 * 3600000 * daysBefore;
		postNewsToEchoareaSinceX(title, echoarea, URL, new Date(date));
	}

	private void postNewsToEchoareaSinceX(String title, String echoarea,
			String URL, Date x) {
		SyndFeedInput feedInput = new SyndFeedInput();
		Echoarea area = FtnTools.getAreaByName(echoarea, null);
		if (area == null) {
			logger.l4("No such echoarea - " + echoarea);
			return;
		}
		StringBuilder sb = new StringBuilder();
		try {
			SyndFeed feed = feedInput.build(new XmlReader(new URL(URL)));
			if (x.after(feed.getPublishedDate())) {
				logger.l4("There's no new entries at " + URL);
				return;
			}

			for (Object object : feed.getEntries()) {
				SyndEntry entry = (SyndEntry) object;
				if (x.after(entry.getPublishedDate())) {
					break;
				}
				sb.append(entry.getTitle());
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
		FtnTools.writeEchomail(area, title, sb.toString());
	}
}
