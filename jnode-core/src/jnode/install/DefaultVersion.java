package jnode.install;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jnode.dto.LinkOption;
import jnode.dto.Route;
import jnode.dto.Version;
import jnode.install.support.LinkOption_1_1;
import jnode.install.support.Route_1_2;
import jnode.orm.ORMManager;

import com.j256.ormlite.table.TableUtils;

public class DefaultVersion extends Version {
	private static DefaultVersion self;

	public static Version getSelf() {
		if (self == null) {
			synchronized (DefaultVersion.class) {
				self = new DefaultVersion();
			}
		}
		return self;
	}

	private DefaultVersion() {
		setMajorVersion(1L);
		setMinorVersion(3L);
		setInstalledAt(new Date());
	}

	@Override
	public String toString() {
		return String.format("%d.%d", getMajorVersion(), getMinorVersion());
	}

	public static List<String> updateFromVersion(Version ver) {
		List<String> ret = new ArrayList<String>();
		if (ver.equals("1.0")) {
			ret.add("ALTER TABLE netmail ADD last_modified BIGINT NOT NULL DEFAULT 0;");
			ver.setMinorVersion(1L);
		}
		if (ver.equals("1.1")) {
			try {
				List<LinkOption_1_1> options2 = ORMManager.get(
						LinkOption_1_1.class).getAll();

				ArrayList<LinkOption> options = new ArrayList<LinkOption>();
				for (LinkOption_1_1 l2 : options2) {
					LinkOption l = new LinkOption();
					l.setLink(l2.getLink());
					l.setOption(l2.getOption());
					l.setValue(l2.getValue());
					options.add(l);
				}
				TableUtils.dropTable(ORMManager.getSource(),
						LinkOption_1_1.class, true);
				TableUtils
						.createTable(ORMManager.getSource(), LinkOption.class);
				for (LinkOption l : options) {
					ORMManager.get(LinkOption.class).save(l);
				}
				ver.setMinorVersion(2L);
				ret.add("SELECT * FROM linkoptions;");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (ver.equals("1.2")) {
			try {
				List<Route_1_2> routes = ORMManager.get(Route_1_2.class)
						.getAll();
				LinkedList<Route> newroute = new LinkedList<Route>();
				for (Route_1_2 r2 : routes) {
					Route r = new Route();
					r.setNice(r2.getNice());
					r.setFromAddr(r2.getFromAddr());
					r.setFromName(r2.getFromName());
					r.setToAddr(r2.getToAddr());
					r.setToName(r2.getToName());
					r.setSubject(r2.getSubject());
					r.setRouteVia(r2.getRouteVia());
					newroute.add(r);
				}
				TableUtils.dropTable(ORMManager.getSource(), Route_1_2.class,
						true);
				TableUtils.createTable(ORMManager.getSource(), Route.class);
				for (Route r : newroute) {
					ORMManager.get(Route.class).save(r);
				}
				ver.setMinorVersion(3L);
				ret.add("SELECT * FROM routing;");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

}
