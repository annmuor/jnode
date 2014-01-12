package jnode.main.threads;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import jnode.dto.Link;
import jnode.dto.Netmail;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.logger.Logger;
import jnode.orm.ORMManager;

/**
 * Поиск лучшего роутинга для нетмейла
 * 
 * @author kreon
 * 
 */
public class NetmailFallback extends TimerTask {
	private static final Logger logger = Logger
			.getLogger(NetmailFallback.class);

	@Override
	public void run() {
		Date date = new Date(new Date().getTime() - 3600000L); // 1 hour ago
		List<Netmail> expiredNetmail = ORMManager.get(Netmail.class).getAnd(
				"last_modified", "<", date, "send", "=", false);
		if (expiredNetmail.isEmpty()) {
			return;
		}
		for (Netmail netmail : expiredNetmail) {
			
			FtnMessage msg = FtnTools.netmailToFtnMessage(netmail);
			msg.setToAddr(new FtnAddress(netmail.getToFTN()));
			Link routeVia = FtnTools.getRoutingFallback(msg,
					netmail.getRouteVia());
			if (routeVia != null) {
				netmail.setRouteVia(routeVia);
				logger.l3("Netmail #" + netmail.getId() + " re-routed via "
						+ routeVia.getLinkAddress());
			}
			netmail.setLastModified(new Date());
			ORMManager.get(Netmail.class).update(netmail);
		}
	}

}
