package jnode.robotcontrol;

import java.util.List;

import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Rewrite;
import jnode.ftn.types.FtnAddress;
import jnode.orm.ORMManager;

public class NodeTools {

	static String listLinks() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("======== Links List ========\n\n");
		for (Link l : ORMManager.INSTANSE.link().queryForAll()) {
			sb.append(String
					.format("Link %s (id %d)\n Aka: %s\n Tosser pwd: %s\n Binkp host: %s\n Binkp port: %d\n Binkp password: %s\n",
							l.getLinkName(), l.getId(), l.getLinkAddress(),
							l.getPaketPassword(), l.getProtocolHost(),
							l.getProtocolPort(), l.getProtocolPassword()));
			List<LinkOption> options = ORMManager.INSTANSE.linkoption()
					.queryForEq("link_id", l);
			if (!options.isEmpty()) {
				sb.append(" Options:\n");
				for (LinkOption o : options) {
					sb.append(" - ");
					sb.append(o.getOption());
					sb.append(" = ");
					sb.append(o.getValue());
					sb.append("\n");
				}
			}
			sb.append("\n");
		}
		sb.append("======== Links List ========");
		return sb.toString();
	}
	
	static String listRewrites() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("======== Rewrites List ========\n\n");
		for(Rewrite r : ORMManager.INSTANSE.rewrite().queryForAll()) {
			sb.append(String.format("Rewrite for %s (nice %d)\n", r.getType().name(), r.getNice()));
			if(!r.getOrig_from_addr().equals("*")) {
				sb.append(" Match: FromAddr ~ " + r.getOrig_from_addr() + "\n");
			}
			if(!r.getOrig_from_name().equals("*")) {
				sb.append(" Match: FromName ~ " + r.getOrig_from_name() + "\n");
			}
			if(!r.getOrig_to_addr().equals("*")) {
				sb.append(" Match: ToAddr ~ " + r.getOrig_to_addr() + "\n");
			}
			if(!r.getOrig_to_name().equals("*")) {
				sb.append(" Match: ToName ~ " + r.getOrig_to_name() + "\n");
			}
			if(!r.getOrig_subject().equals("*")) {
				sb.append(" Match: Subject ~ " + r.getOrig_subject() + "\n");
			}
			
			if(!r.getNew_from_addr().equals("*")) {
				sb.append(" Replace: FromAddr = " + r.getNew_from_addr() + "\n");
			}
			if(!r.getNew_from_name().equals("*")) {
				sb.append(" Replace: FromName = " + r.getNew_from_name() + "\n");
			}
			if(!r.getNew_to_addr().equals("*")) {
				sb.append(" Replace: ToAddr = " + r.getNew_to_addr() + "\n");
			}
			if(!r.getNew_to_name().equals("*")) {
				sb.append(" Replace: ToName = " + r.getNew_to_name() + "\n");
			}
			if(!r.getNew_subject().equals("*")) {
				sb.append(" Replace: Subject = " + r.getNew_subject() + "\n");
			}
			sb.append("\n");
		}
		// DATA
		sb.append("======== Rewrites List ========\n");
		return sb.toString();
	}

	private static Link getLinkBy(String name) throws Exception {
		Link ret = null;
		try {
			Long l = Long.valueOf(name);
			ret = ORMManager.INSTANSE.link().queryForId(l);
		} catch (NumberFormatException e) {
		}
		if (ret == null) {
			try {
				FtnAddress a = new FtnAddress(name);
				List<Link> links = ORMManager.INSTANSE.link().queryForEq(
						"ftn_address", a.toString());
				if (!links.isEmpty()) {
					ret = links.get(0);
				}
			} catch (NumberFormatException e) {
			}
		}
		if (ret == null) {
			List<Link> links = ORMManager.INSTANSE.link().queryForEq(
					"station_name", name);
			if (!links.isEmpty()) {
				ret = links.get(0);
			}
		}
		if (ret == null) {
			List<Link> links = ORMManager.INSTANSE.link().queryForEq("host",
					name);
			if (!links.isEmpty()) {
				ret = links.get(0);
			}
		}
		return ret;
	}
}
