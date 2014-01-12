package jnode.install;

import java.util.Date;
import java.util.List;

import jnode.dao.GenericDAO;
import jnode.dto.Echoarea;
import jnode.dto.Link;
import jnode.dto.Rewrite;
import jnode.dto.Route;
import jnode.dto.Subscription;
import jnode.dto.Rewrite.Type;
import jnode.dto.Robot;
import jnode.dto.Version;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.SystemInfo;
import jnode.orm.ORMManager;
import jnode.robot.AreaFix;
import jnode.robot.FileFix;
import jnode.robot.ScriptFix;

public class InstallUtil {
	private static final Logger logger = Logger.getLogger(InstallUtil.class);
	private GenericDAO<Version> versionDao;

	public InstallUtil() {
		versionDao = ORMManager.get(Version.class);
		List<Version> versions = versionDao
				.getOrderLimitAnd(1, "int_at", false);
		if (versions.size() == 0) {
			doInstall();
		} else {
			Version ver = versions.get(0);
			logger.l1("You have installed " + ver.toString());
			checkForLastVersion(ver);
		}
	}

	private void checkForLastVersion(Version ver) {
		logger.l1(String.format("Upgrading from %s", ver.toString()));
		List<String> queryList = DefaultVersion.updateFromVersion(ver);
		for(String query : queryList) {
			ORMManager.get(Version.class).executeRaw(query);
		}
		ver.setInstalledAt(new Date());
		ORMManager.get(Version.class).save(ver);
		logger.l1(String.format("Upgraded to %s", ver.toString()));

	}

	private void doInstall() {
		logger.l1("[+] Creating database data");
		SystemInfo info = MainHandler.getCurrentInstance().getInfo();

		// robots
		Robot areafix = new Robot();
		areafix.setClassName(AreaFix.class.getCanonicalName());
		areafix.setRobot("areafix");
		ORMManager.get(Robot.class).save(areafix);
		Robot filefix = new Robot();
		filefix.setClassName(FileFix.class.getCanonicalName());
		filefix.setRobot("filefix");
		ORMManager.get(Robot.class).save(filefix);

		Robot scriptfix = new Robot();
		scriptfix.setClassName(ScriptFix.class.getCanonicalName());
		scriptfix.setRobot("scriptfix");
		ORMManager.get(Robot.class).save(scriptfix);

		logger.l1("[+] Robots created");

		// owner's point
		String ownAddr = FtnTools.getPrimaryFtnAddress().toString() + ".1";
		Link owner = ORMManager.get(Link.class).getFirstAnd("ftn_address", "=",
				ownAddr);
		if (owner == null) {
			owner = new Link();
			owner.setLinkName(info.getSysop());
			owner.setLinkAddress(ownAddr);
			owner.setProtocolHost("-");
			owner.setProtocolPort(0);
			owner.setPaketPassword(FtnTools.generate8d());
			owner.setProtocolPassword(owner.getPaketPassword());
			ORMManager.get(Link.class).save(owner);
			logger.l1("[+] owner point account created");
			logger.l1(String.format("\n\tFTN: %s\n\tAka: %s\n\tPassword: %s\n",
					owner.getLinkAddress(), owner.getLinkName(),
					owner.getPaketPassword()));
			long nice = 1;
			Rewrite rw = new Rewrite();
			rw.setType(Type.NETMAIL);
			rw.setLast(true);
			rw.setNice(nice++);
			rw.setOrig_from_addr("^"
					+ ownAddr.replace(".", "\\.").replace("/", "\\/") + "$");
			rw.setNew_from_addr(FtnTools.getPrimaryFtnAddress().toString());
			ORMManager.get(Rewrite.class).save(rw);

			for (FtnAddress address : info.getAddressList()) {
				Rewrite rw2 = new Rewrite();
				rw2.setType(Type.NETMAIL);
				rw2.setLast(true);
				rw2.setNice(nice++);
				rw2.setOrig_to_addr("^"
						+ address.toString().replace(".", "\\.")
								.replace("/", "\\/") + "$");
				rw2.setNew_to_addr(ownAddr);
				ORMManager.get(Rewrite.class).save(rw2);
			}
			logger.l1("[+] Basic rewrites for owner's point created!");
		}

		{
			boolean route = !ORMManager.get(Route.class).getAll().isEmpty();
			if (!route) {
				List<Link> links = ORMManager.get(Link.class).getAll();
				for (Link l : links) {
					FtnAddress a = new FtnAddress(l.getLinkAddress());
					if (a.getPoint() == 0) { // server side link
						Route defroute = new Route();
						defroute.setNice(Long.MAX_VALUE);
						defroute.setRouteVia(l);
						ORMManager.get(Route.class).save(defroute);
						logger.l1("[+] all messages will be routed through "
								+ l.getLinkAddress());
						route = true;
						break;
					}
				}
				if (!route) {
					logger.l1("[-] link for default route now found. Add it manually");
				}
			}
		}

		{
			List<Echoarea> areas = ORMManager.get(Echoarea.class).getAll();
			if (areas.isEmpty()) {
				Echoarea local = new Echoarea();
				local.setDescription("Local echoarea");
				local.setName(FtnTools.getPrimaryFtnAddress().getNode()
						+ ".local");
				local.setGroup("");
				ORMManager.get(Echoarea.class).save(local);
				Subscription sub = new Subscription();
				sub.setArea(local);
				sub.setLink(owner);
				ORMManager.get(Subscription.class).save(sub);
				FtnTools.writeEchomail(local, "Your new jNode installation",
						"Welcome aboard!\n\nYou've just installed. Enjoy in Fidonet now!");
				logger.l1("[+] created local echoarea " + local.getName());
			}
		}
		ORMManager.get(Version.class).save(DefaultVersion.getSelf());
		logger.l1("Installation completed!");
	}
}
