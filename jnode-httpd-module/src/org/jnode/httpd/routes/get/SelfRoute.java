package org.jnode.httpd.routes.get;

import java.util.List;

import jnode.ftn.types.FtnAddress;
import jnode.main.MainHandler;
import jnode.main.SystemInfo;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class SelfRoute extends Route {
	private final String FORMAT_TABLE = "<table class=\"info\">%s</table>";
	private final String FORMAT_TR = "<tr><th>%s</th><td>%s</td></tr>";

	public SelfRoute() {
		super("/index.html");
	}

	public SelfRoute(String path) {
		super(path);
	}

	@Override
	public Object handle(Request req, Response resp) {
		SystemInfo info = MainHandler.getCurrentInstance().getInfo();

		String text = String.format(
				FORMAT_TABLE,
				String.format(FORMAT_TR, "Имя узла", info.getStationName())
						+ String.format(FORMAT_TR, "Расположение узла",
								info.getLocation())
						+ String.format(FORMAT_TR, "Сисоп",
								info.getSysop())
						+ String.format(FORMAT_TR, "FTN-адрес(а)",
								getAddrList(info.getAddressList()))
						+ String.format(FORMAT_TR, "Версия софта",
								MainHandler.getVersion())
						+ String.format(FORMAT_TR, "ОС", getOS()));
		return HTML.start(false).append(text).footer().get();
	}

	private String getOS() {
		return System.getProperty("os.name") + " "
				+ System.getProperty("os.version") + " ("
				+ System.getProperty("os.arch") + ")";
	}

	private String getAddrList(List<FtnAddress> list) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for (FtnAddress address : list) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(address.toString());
		}
		return sb.toString();
	}
}
