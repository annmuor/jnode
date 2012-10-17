package jnode.stat;

import java.util.Date;

import jnode.dto.Filemail;
import jnode.orm.ORMManager;

public class FileareaStat implements IStatPoster {

	@Override
	public String getSubject() {
		return "New files last 24h";
	}

	@Override
	public String getText() {
		StringBuilder b = new StringBuilder();
		String filearea = "";
		Date d = new Date(new Date().getTime() - (24 * 3600 * 1000));
		for (Filemail m : ORMManager.INSTANSE.getFilemailDAO().getOrderAnd(
				"filearea_id", true, "created", ">", d)) {
			if (!filearea.equalsIgnoreCase(m.getFilearea().getName())) {
				filearea = m.getFilearea().getName().toUpperCase();
				b.append(" > " + filearea + " - "
						+ m.getFilearea().getDescription() + "\n");
			}
			b.append("  " + m.getFilename() + " - " + m.getFiledesc() + "\n");
		}
		return b.toString();
	}

}
