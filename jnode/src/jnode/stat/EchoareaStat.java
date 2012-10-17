package jnode.stat;

import java.util.Date;

import com.j256.ormlite.dao.GenericRawResults;

import jnode.orm.ORMManager;

public class EchoareaStat extends AStatPoster {

	@Override
	protected String _getSubject() {
		return "Echoarea messages per day";
	}

	@Override
	protected String _getText() {

		Long before = new Date().getTime() - (24 * 3600 * 1000);
		Long after = new Date().getTime();
		GenericRawResults<String[]> results = ORMManager.INSTANSE
				.getEchomailDAO()
				.getRaw(String
						.format("SELECT a.name,a.description,count(e.id) AS count FROM echomail e"
								+ " LEFT JOIN echoarea a ON (e.echoarea_id=a.id) WHERE e.date >= %d AND e.date <= %d"
								+ " GROUP BY a.name,a.description ORDER BY count DESC",
								before, after));
		StringBuilder result = new StringBuilder();
		result.append("|");
		result.append("Area");
		for (int i = 4; i < 35; i++) {
			result.append(' ');
		}
		result.append("|");
		result.append("Count");
		for (int i = 5; i < 4; i++) {
			result.append(' ');
		}
		result.append("|");
		result.append("Description");
		result.append('\n');
		for (int i = 4; i < 78; i++) {
			result.append('-');
		}
		result.append('\n');
		for (String[] res : results) {
			result.append("|");
			result.append(res[0]);
			for (int i = res[0].length(); i < 35; i++) {
				result.append(' ');
			}
			result.append("|");
			result.append(res[2]);
			for (int i = res[2].length(); i < 5; i++) {
				result.append(' ');
			}
			result.append("|");
			result.append(res[1]);
			result.append('\n');
		}
		for (int i = 4; i < 78; i++) {
			result.append('-');
		}
		result.append('\n');
		return result.toString();
	}
}
