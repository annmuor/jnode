package jnode.ftn.tosser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import jnode.dto.Dupe;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.EchomailAwaiting;
import jnode.dto.FileSubscription;
import jnode.dto.Filearea;
import jnode.dto.Filemail;
import jnode.dto.FilemailAwaiting;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Netmail;
import jnode.dto.Subscription;
import jnode.ftn.FtnTools;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
import jnode.ftn.types.FtnTIC;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.main.threads.PollQueue;
import jnode.orm.ORMManager;
import jnode.protocol.io.Message;

/**
 * 
 * @author kreon
 * 
 */
public class FtnTosser {
	private static final Logger logger = Logger.getLogger(FtnTosser.class);
	private Map<String, Integer> tossed = new HashMap<String, Integer>();
	private Map<String, Integer> bad = new HashMap<String, Integer>();
	private Set<Link> pollLinks = new HashSet<Link>();

	/**
	 * Разбор нетмейла
	 * 
	 * @param netmail
	 * @param secure
	 */
	private void tossNetmail(FtnMessage netmail, boolean secure) {
		if (secure) {
			if (FtnTools.checkRobot(netmail)) {
				return;
			}
		}
		boolean drop = FtnTools.isNetmailMustBeDropped(netmail, secure);

		if (drop) {
			Integer n = bad.get("netmail");
			bad.put("netmail", (n == null) ? 1 : n + 1);
		} else {
			if ((netmail.getAttribute() & FtnMessage.ATTR_ARQ) > 0) {
				FtnTools.writeReply(netmail, "ARQ reply",
						"Your message was successfully reached this system");
			}
			FtnTools.processRewrite(netmail);
			Link routeVia = FtnTools.getRouting(netmail);

			Netmail dbnm = new Netmail();
			dbnm.setRouteVia(routeVia);
			dbnm.setDate(netmail.getDate());
			dbnm.setFromFTN(netmail.getFromAddr().toString());
			dbnm.setToFTN(netmail.getToAddr().toString());
			dbnm.setFromName(netmail.getFromName());
			dbnm.setToName(netmail.getToName());
			dbnm.setSubject(netmail.getSubject());
			dbnm.setText(netmail.getText());
			dbnm.setAttr(netmail.getAttribute());
			ORMManager.INSTANSE.getNetmailDAO().save(dbnm);
			Integer n = tossed.get("netmail");
			tossed.put("netmail", (n == null) ? 1 : n + 1);
			if (routeVia == null) {
				logger.l4(String
						.format("Netmail %s -> %s is not transferred ( routing not found )",
								netmail.getFromAddr().toString(), netmail
										.getToAddr().toString()));
			} else {
				routeVia = ORMManager.INSTANSE.getLinkDAO().getById(
						routeVia.getId());
				logger.l4(String.format("Netmail %s -> %s transferred via %s",
						netmail.getFromAddr().toString(), netmail.getToAddr()
								.toString(), routeVia.getLinkAddress()));
				if (FtnTools.getOptionBooleanDefTrue(routeVia,
						LinkOption.BOOLEAN_CRASH_NETMAIL)) {
					PollQueue.INSTANSE.add(routeVia);
				}
			}
		}
	}

	private void tossEchomail(FtnMessage echomail, Link link, boolean secure) {

		if (!secure) {
			logger.l3("Echomail via unsecure is dropped");
			return;
		}
		Echoarea area = FtnTools.getAreaByName(echomail.getArea(), link);
		if (area == null) {
			logger.l3("Echoarea " + echomail.getArea()
					+ " is not avalible for " + link.getLinkAddress());
			Integer n = bad.get(echomail.getArea());
			bad.put(echomail.getArea(), (n == null) ? 1 : n + 1);
			return;
		}
		if (FtnTools.isADupe(area, echomail.getMsgid())) {
			logger.l3("Message " + echomail.getArea() + " "
					+ echomail.getMsgid() + " is a dupe");
			Integer n = bad.get(echomail.getArea());
			bad.put(echomail.getArea(), (n == null) ? 1 : n + 1);
			return;
		}

		FtnTools.processRewrite(echomail);

		Echomail mail = new Echomail();
		mail.setArea(area);
		mail.setDate(echomail.getDate());
		mail.setFromFTN(echomail.getFromAddr().toString());
		mail.setFromName(echomail.getFromName());
		mail.setToName(echomail.getToName());
		mail.setSubject(echomail.getSubject());
		mail.setText(echomail.getText());
		mail.setSeenBy(FtnTools.write2D(echomail.getSeenby(), true));
		mail.setPath(FtnTools.write2D(echomail.getPath(), false));
		ORMManager.INSTANSE.getEchomailDAO().save(mail);

		for (Subscription sub : ORMManager.INSTANSE.getSubscriptionDAO()
				.getAnd("echoarea_id", "=", area)) {
			if (!sub.getLink().getId().equals(link.getId())) {
				ORMManager.INSTANSE.getEchomailAwaitingDAO().save(
						new EchomailAwaiting(sub.getLink(), mail));
				pollLinks.add(sub.getLink());
			}
		}

		{
			Dupe dupe = new Dupe();
			dupe.setEchoarea(area);
			dupe.setMsgid(echomail.getMsgid());
			ORMManager.INSTANSE.getDupeDAO().save(dupe);

		}

		Integer n = tossed.get(echomail.getArea());
		tossed.put(echomail.getArea(), (n == null) ? 1 : n + 1);

	}

	/**
	 * Получаем сообщения из бандлов
	 * 
	 * @param connector
	 */
	public int tossIncoming(Message message, Link link) {
		if (message == null) {
			return 0;
		}

		try {
			FtnPkt[] pkts = FtnTools.unpack(message);
			for (FtnPkt pkt : pkts) {
				if (message.isSecure()) {
					if (!FtnTools.getOptionBooleanDefFalse(link,
							LinkOption.BOOLEAN_IGNORE_PKTPWD)) {
						if (!link.getPaketPassword().equalsIgnoreCase(
								pkt.getPassword())) {
							logger.l2("Pkt password mismatch - package moved to inbound");
							FtnTools.moveToBad(pkt);
							continue;
						}
					}
				}
				for (FtnMessage ftnm : pkt.getMessages()) {
					if (message.isSecure()) {
						if (FtnTools.checkRobot(ftnm)) {
							continue;
						}
					}
					if (ftnm.isNetmail()) {
						tossNetmail(ftnm, message.isSecure());
					} else {
						tossEchomail(ftnm, link, message.isSecure());
					}
				}

			}
		} catch (Exception e) {
			logger.l2("Unpack error " + message.getMessageName(), e);
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	/**
	 * Разбор входящих файлов
	 */
	public void tossInbound() {
		Set<Link> poll = new HashSet<Link>();
		File inbound = new File(Main.getInbound());
		for (File file : inbound.listFiles()) {
			if (file.getName().toLowerCase().matches("^[a-f0-9]{8}\\.pkt$")) {
				try {
					Message m = new Message(file);
					logger.l4("Tossing file " + file.getAbsolutePath());
					FtnPkt[] pkts = FtnTools.unpack(m);
					for (FtnPkt pkt : pkts) {
						for (FtnMessage ftnm : pkt.getMessages()) {
							if (ftnm.isNetmail()) {
								tossNetmail(ftnm, true);
							} else {
								tossEchomail(ftnm, null, true);
							}
						}
					}
					file.delete();
				} catch (Exception e) {
					logger.l3("Tossing failed " + file.getAbsolutePath());
				}
			} else if (file.getName().toLowerCase()
					.matches("^[a-z0-9]{8}\\.tic$")) {
				if (!Main.isFileechoEnable()) {
					continue;
				}
				logger.l3("Proccessing " + file.getName());
				try {
					FileInputStream fis = new FileInputStream(file);
					FtnTIC tic = new FtnTIC();
					tic.unpack(fis);
					fis.close();
					String filename = tic.getFile().toLowerCase();
					File attach = new File(Main.getInbound() + File.separator
							+ filename);
					boolean ninetoa = false;
					boolean ztonull = false;
					boolean underll = false;

					while (!attach.exists()) {
						if ((ninetoa && ztonull) || underll) {
							break;
						} else {
							char[] array = filename.toCharArray();
							char c = array[array.length - 1];
							if ((c >= '0' && c <= '8')
									|| (c >= 'a' && c <= 'y')) {
								c++;
							} else if (c == '9') {
								c = 'a';
								ninetoa = true;
							} else if (c == 'z') {
								c = '0';
								ztonull = true;
							} else {
								c = '_';
								underll = true;
							}
							array[array.length - 1] = c;
							filename = new String(array);
							attach = new File(Main.getInbound()
									+ File.separator + filename);
						}
					}
					if (attach.canRead()) { // processing
						logger.l3("File found as " + filename);
						if (!tic.getTo().equals(Main.info.getAddress())) {
							file.delete();
							logger.l3("Tic " + file.getName()
									+ " is not for us");
							continue;
						}
						Link source = ORMManager.INSTANSE.getLinkDAO()
								.getFirstAnd("ftn_address", "=",
										tic.getFrom().toString());
						if (source == null) {
							logger.l3("Link " + tic.getFrom() + " not found");
							file.renameTo(new File(Main.getInbound()
									+ File.separator + "bad_"
									+ FtnTools.generateTic()));
							continue;
						}
						Filearea area = FtnTools.getFileareaByName(tic
								.getArea().toLowerCase(), source);
						if (area == null) {
							logger.l3("Filearea " + tic.getArea()
									+ " is not avalible for "
									+ source.getLinkAddress());
							file.renameTo(new File(Main.getInbound()
									+ File.separator + "bad_"
									+ FtnTools.generateTic()));
							continue;
						}
						new File(Main.getFileechoPath() + File.separator
								+ area.getName()).mkdir();
						Filemail mail = new Filemail();
						if (attach.renameTo(new File(Main.getFileechoPath()
								+ File.separator + area.getName()
								+ File.separator + tic.getFile()))) {
							mail.setFilepath(Main.getFileechoPath()
									+ File.separator + area.getName()
									+ File.separator + tic.getFile());
						} else {
							mail.setFilepath(attach.getAbsolutePath());
						}
						mail.setFilearea(area);
						mail.setFilename(tic.getFile());
						mail.setFiledesc(tic.getDesc());
						mail.setOrigin(tic.getOrigin().toString());
						mail.setPath(tic.getPath());
						mail.setSeenby(FtnTools.write4D(tic.getSeenby()));
						mail.setCreated(new Date());
						ORMManager.INSTANSE.getFilemailDAO().save(mail);
						for (FileSubscription sub : ORMManager.INSTANSE
								.getFileSubscriptionDAO().getAnd("filearea_id",
										"=", area, "link_id", "!=", source)) {

							ORMManager.INSTANSE.getFilemailAwaitingDAO().save(
									new FilemailAwaiting(sub.getLink(), mail));
							poll.add(sub.getLink());
						}
					} else {
						logger.l3("File " + tic.getFile()
								+ " not found in inbound, waiting");
						continue;
					}
					file.delete();
				} catch (Exception e) {
					logger.l1("Error while processing tic " + file.getName(), e);
				}
			} else if (file.getName().toLowerCase()
					.matches("^[0-9a-f]\\..?lo$")) {
				// TODO: make poll

			}
			for (Link l : poll) {
				if (FtnTools.getOptionBooleanDefFalse(l,
						LinkOption.BOOLEAN_CRASH_FILEMAIL)) {
					PollQueue.INSTANSE.add(ORMManager.INSTANSE.getLinkDAO()
							.getById(l.getId()));
				}
			}
		}
	}

	public void end() {

		if (!tossed.isEmpty()) {
			logger.l3("Messages wrote:");
			for (String area : tossed.keySet()) {
				logger.l3(String.format("\t%s - %d", area, tossed.get(area)));
			}
		}
		if (!bad.isEmpty()) {
			logger.l2("Messages dropped:");
			for (String area : bad.keySet()) {
				logger.l2(String.format("\t%s - %d", area, bad.get(area)));
			}
		}

		for (Link l : pollLinks) {
			if (FtnTools.getOptionBooleanDefFalse(l,
					LinkOption.BOOLEAN_CRASH_ECHOMAIL)) {
				PollQueue.INSTANSE.add(ORMManager.INSTANSE.getLinkDAO()
						.getById(l.getId()));
			}
		}

	}

	/**
	 * Получить новые сообщения для линка
	 * 
	 * @param link
	 * @return
	 */
	public static List<Message> getMessagesForLink(Link link) {
		FtnAddress link_address = new FtnAddress(link.getLinkAddress());
		FtnAddress our_address = Main.info.getAddress();
		Ftn2D link2d = new Ftn2D(link_address.getNet(), link_address.getNode());
		Ftn2D our2d = new Ftn2D(our_address.getNet(), our_address.getNode());
		List<FtnMessage> messages = new ArrayList<FtnMessage>();
		List<File> attachedFiles = new ArrayList<File>();
		List<FtnTIC> tics = new ArrayList<FtnTIC>();
		List<Message> ret = new ArrayList<Message>();
		try {
			List<Netmail> netmails = ORMManager.INSTANSE.getNetmailDAO()
					.getAnd("send", "=", false, "route_via", "=", link);

			if (!netmails.isEmpty()) {
				for (Netmail netmail : netmails) {
					FtnMessage msg = FtnTools.netmailToFtnMessage(netmail);
					messages.add(msg);
					logger.l4(String.format(
							"Pack netmail #%d %s -> %s for %s flags %d",
							netmail.getId(), netmail.getFromFTN(),
							netmail.getToFTN(), link.getLinkAddress(),
							msg.getAttribute()));
					if ((netmail.getAttr() & FtnMessage.ATTR_FILEATT) > 0) {
						String filename = netmail.getSubject();
						filename = filename.replaceAll("^[\\./\\\\]+", "_");
						File file = new File(Main.getInbound() + File.separator
								+ filename);
						if (file.canRead()) {
							attachedFiles.add(file);
							logger.l5("Netmail with attached file " + filename);
						}
					}
					netmail.setSend(true);
					ORMManager.INSTANSE.getNetmailDAO().update(netmail);
				}
			}
		} catch (Exception e) {
			logger.l2("Netmail error " + link.getLinkAddress(), e);
		}
		// echomail
		{
			List<Echomail> toRemove = new ArrayList<Echomail>();
			List<EchomailAwaiting> mailToSend = ORMManager.INSTANSE
					.getEchomailAwaitingDAO().getAnd("link_id", "=", link);
			for (EchomailAwaiting ema : mailToSend) {
				Echomail mail = ema.getMail();
				Echoarea area = mail.getArea();
				toRemove.add(mail);
				Set<Ftn2D> seenby = new HashSet<Ftn2D>(FtnTools.read2D(mail
						.getSeenBy()));
				if (seenby.contains(link2d) && link_address.getPoint() == 0) {
					logger.l5(link2d + " is in seenby for " + link_address);
					continue;
				}
				List<Ftn2D> path = FtnTools.read2D(mail.getPath());
				seenby.add(our2d);
				seenby.add(link2d);

				if (!path.contains(our2d)) {
					path.add(our2d);
				}

				List<Subscription> ssubs = ORMManager.INSTANSE
						.getSubscriptionDAO().getAnd("echoarea_id", "=", area);
				for (Subscription ssub : ssubs) {
					Link _sslink = ORMManager.INSTANSE.getLinkDAO().getById(
							ssub.getLink().getId());
					FtnAddress addr = new FtnAddress(_sslink.getLinkAddress());
					Ftn2D d2 = new Ftn2D(addr.getNet(), addr.getNode());
					seenby.add(d2);
				}

				FtnMessage message = new FtnMessage();
				message.setNetmail(false);
				message.setArea(area.getName().toUpperCase());
				message.setFromName(mail.getFromName());
				message.setToName(mail.getToName());
				message.setFromAddr(our_address);
				message.setToAddr(link_address);
				message.setDate(mail.getDate());
				message.setSubject(mail.getSubject());
				message.setText(mail.getText());
				message.setSeenby(new ArrayList<Ftn2D>(seenby));
				message.setPath(path);
				logger.l4("Echomail #" + mail.getId() + " (" + area.getName()
						+ ") packed for " + link.getLinkAddress());
				messages.add(message);

			}
			if (!toRemove.isEmpty()) {
				ORMManager.INSTANSE.getEchomailAwaitingDAO().delete("link_id",
						"=", link, "echomail_id", "in", toRemove);
			}
		}
		// fileechoes
		{
			List<Filemail> toRemove = new ArrayList<Filemail>();
			List<FilemailAwaiting> filemail = ORMManager.INSTANSE
					.getFilemailAwaitingDAO().getAnd("link_id", "=", link);
			for (FilemailAwaiting awmail : filemail) {
				Filemail mail = awmail.getMail();
				toRemove.add(mail);
				if (mail == null) {
					continue;
				}
				Filearea area = mail.getFilearea();
				File f = new File(mail.getFilepath());

				if (!f.canRead()) {
					logger.l3("File unavalible");
					continue;
				}

				Set<FtnAddress> seenby = new HashSet<FtnAddress>(
						FtnTools.read4D(mail.getSeenby()));
				if (seenby.contains(link_address)) {
					logger.l3("This file have a seen-by for link");
					continue;
				}
				FtnTIC tic = new FtnTIC();
				try {
					CRC32 crc32 = new CRC32();
					FileInputStream fis = new FileInputStream(f);
					int len = 0;
					do {
						byte buf[];
						len = fis.available();
						if (len > 1024) {
							buf = new byte[1024];
						} else {
							buf = new byte[len];
						}
						fis.read(buf);
						crc32.update(buf);
					} while (len > 0);
					tic.setCrc32(crc32.getValue());
					fis.close();
				} catch (IOException e) {
				}
				tic.setArea(area.getName().toUpperCase());
				tic.setAreaDesc(area.getDescription());
				tic.setFile(mail.getFilename());
				tic.setSize(f.length());
				tic.setDesc(mail.getFiledesc());
				tic.setPassword(link.getPaketPassword());
				tic.setFrom(our_address);
				tic.setTo(link_address);
				tic.setOrigin(new FtnAddress(mail.getOrigin()));
				seenby.add(our_address);
				for (FileSubscription sub : ORMManager.INSTANSE
						.getFileSubscriptionDAO().getAnd("filearea_id", "=",
								area)) {
					Link l = ORMManager.INSTANSE.getLinkDAO().getById(
							sub.getLink().getId());
					seenby.add(new FtnAddress(l.getLinkAddress()));
				}
				List<FtnAddress> sb = new ArrayList<FtnAddress>(seenby);
				Collections.sort(sb, new FtnTools.Ftn4DComparator());
				tic.setSeenby(sb);
				tic.setPath(mail.getPath() + "Path " + Main.info.getAddress()
						+ " " + System.currentTimeMillis() / 1000 + " "
						+ FtnTools.format.format(new Date()) + " "
						+ Main.info.getVersion() + "\r\n");
				tic.setRealpath(mail.getFilepath());
				tics.add(tic);
			}
			if (!toRemove.isEmpty()) {
				ORMManager.INSTANSE.getFilemailAwaitingDAO().delete("link_id",
						"=", link, "filemail_id", "in", toRemove);
			}
		}
		if (!messages.isEmpty()) {
			ret.addAll(FtnTools.pack(messages, link));
		}
		for (FtnTIC tic : tics) {
			try {
				byte[] data = tic.pack();
				Message message = new Message(FtnTools.generateTic(),
						data.length);
				message.setInputStream(new ByteArrayInputStream(data));
				ret.add(message);

				message = new Message(tic.getFile(), tic.getSize());
				message.setInputStream(new FileInputStream(tic.getRealpath()));
				ret.add(message);
				logger.l4("Packed tic " + message.getMessageName()
						+ " for file " + tic.getFile());
			} catch (Exception e) {
				logger.l3("Tic attach failed");
			}
		}
		for (File f : attachedFiles) {
			try {
				ret.add(new Message(f));
				f.delete();
			} catch (Exception e) {
				logger.l3("File attach filed " + f.getAbsolutePath());
			}
		}
		return ret;
	}
}
