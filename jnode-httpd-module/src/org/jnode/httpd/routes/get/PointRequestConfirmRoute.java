package org.jnode.httpd.routes.get;

import jnode.dto.Link;
import jnode.event.Notifier;
import jnode.event.SharedModuleEvent;
import jnode.ftn.FtnTools;
import jnode.main.MainHandler;
import jnode.orm.ORMManager;

import org.jnode.httpd.dto.PointRequest;
import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class PointRequestConfirmRoute extends Route {

	public PointRequestConfirmRoute() {
		super("/pointrequest");
	}

	@Override
	public Object handle(Request req, Response resp) {
		String key = req.queryParams("key");
		String text = "";
		if (key != null) {
			PointRequest pr = ORMManager.get(PointRequest.class).getById(key);
			if (pr != null) {
				synchronized (PointRequest.class) {
					if (null == ORMManager.get(Link.class).getFirstAnd(
							"ftn_address", "=", pr.getAddr())) {
						Link l = new Link();
						l.setLinkName(pr.getName());
						l.setLinkAddress(pr.getAddr());
						l.setPaketPassword(pr.getPassword());
						l.setProtocolHost("-");
						l.setProtocolPort(0);
						l.setProtocolPassword(pr.getPassword());
						ORMManager.get(Link.class).save(l);
						ORMManager.get(PointRequest.class).delete(pr);
						writeMails(pr);
						text = "Проверьте вашу почту для получения дальнейших инструкций";
					} else {
						text = "Данный пойнт уже зарегистрирован в системе";
					}
				}
			} else {
				text = "Неверный ключ подтверждения";
			}
		} else {
			text = "Неверный запрос";
		}
		return HTML.start(false)
				.append("<b>Статус: " + text + "</b>")
				.footer().get();
	}

	private void writeMails(PointRequest pr) {
		String text = String
				.format("Point data:\n > Addr: %s\n > Name: %s\n > Email: %s\n > Password: %s\n",
						pr.getAddr(), pr.getName(), pr.getEmail(),
						pr.getPassword());
		FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(),
				FtnTools.getPrimaryFtnAddress(), MainHandler
						.getCurrentInstance().getInfo().getStationName(),
				MainHandler.getCurrentInstance().getInfo().getSysop(),
				"Point confirmed", text);

		Notifier.INSTANSE.notify(new SharedModuleEvent(
				"org.jnode.mail.MailModule", "to", pr.getEmail(), "subject",
				"Point connection info", "text", text));

	}
}
