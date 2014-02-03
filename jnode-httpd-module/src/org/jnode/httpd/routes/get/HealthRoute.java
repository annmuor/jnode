package org.jnode.httpd.routes.get;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class HealthRoute extends Route {
	private final String FORMAT_TABLE = "<table class=\"info\">%s</table>";
	private final String FORMAT_TR = "<tr><th>%s</th><td>%s</td></tr>";

	public HealthRoute() {
		super("/secure/index.html");
	}

	@Override
	public Object handle(Request req, Response resp) {
		Runtime runtime = Runtime.getRuntime();
		int free = Math.round(runtime.freeMemory() / (1024 * 1024));
		int max = Math.round(runtime.maxMemory() / (1024 * 1024));
		int total = Math.round(runtime.totalMemory() / (1024 * 1024));
		String text = String.format(
				FORMAT_TABLE,
				String.format(FORMAT_TR, "Количество ядер",
						"" + runtime.availableProcessors())
						+ String.format(FORMAT_TR, "Количество потоков",
								Thread.activeCount())
						+ String.format(FORMAT_TR, "Использования памяти",
								"Доступно: " + max + "Мб / Использовано: "
										+ (total - free) + " Мб"));
		return HTML.start(true).append(text).footer().get();
	}
}
