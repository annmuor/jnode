package jnode.ftn.tosser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jnode.dto.Dupe;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.EchomailAwaiting;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Netmail;
import jnode.dto.Subscription;
import jnode.ftn.FtnTools;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
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
						.format("Netmail %s -> %s не будет отправлен ( не найден роутинг )",
								netmail.getFromAddr().toString(), netmail
										.getToAddr().toString()));
			} else {
				routeVia = ORMManager.INSTANSE.getLinkDAO().getById(
						routeVia.getId());
				logger.l4(String.format(
						"Netmail %s -> %s будет отправлен через %s", netmail
								.getFromAddr().toString(), netmail.getToAddr()
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
			logger.l3("Эхомейл по unsecure-соединению - уничтожен");
			return;
		}
		Echoarea area = FtnTools.getAreaByName(echomail.getArea(), link);
		if (area == null) {
			logger.l3("Эхоконференция " + echomail.getArea()
					+ " недоступна для узла " + link.getLinkAddress());
			Integer n = bad.get(echomail.getArea());
			bad.put(echomail.getArea(), (n == null) ? 1 : n + 1);
			return;
		}
		if (FtnTools.isADupe(area, echomail.getMsgid())) {
			logger.l3("Сообщение " + echomail.getArea() + " "
					+ echomail.getMsgid() + " является дюпом");
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
							logger.l2("Пароль для пакета не совпал - пакет перемещен в inbound");
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
			logger.l2("Ошибка при распаковке " + message.getMessageName(), e);
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	public void tossInbound() {
		File inbound = new File(Main.getInbound());
		for (File file : inbound.listFiles()) {
			if (file.getName().matches("^[a-f0-9]{8}\\.pkt$")) {
				try {
					Message m = new Message(file);
					logger.l4("Обрабатываем файл " + file.getAbsolutePath());
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
					logger.l3("Не могу обработать пакет "
							+ file.getAbsolutePath());
				}
			}
		}
	}

	public void end() {

		if (!tossed.isEmpty()) {
			logger.l3("Записано сообщений:");
			for (String area : tossed.keySet()) {
				logger.l3(String.format("\t%s - %d", area, tossed.get(area)));
			}
		}
		if (!bad.isEmpty()) {
			logger.l2("Уничтожено сообщений:");
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
		try {
			List<Netmail> netmails = ORMManager.INSTANSE.getNetmailDAO()
					.getAnd("send", "=", false, "route_via", "=", link);

			if (!netmails.isEmpty()) {
				for (Netmail netmail : netmails) {
					FtnMessage msg = FtnTools.netmailToFtnMessage(netmail);
					messages.add(msg);
					logger.l4(String.format(
							"Пакуем netmail #%d %s -> %s для %s (%d)",
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
							logger.l5("К сообщению прикреплен файл " + filename
									+ ", пересылаем");
						}
					}
					netmail.setSend(true);
					ORMManager.INSTANSE.getNetmailDAO().update(netmail);
				}
			}
		} catch (Exception e) {
			logger.l2("Ошибка обработки netmail для " + link.getLinkAddress(),
					e);
		}
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
				logger.l5(link2d + " есть в синбаях для " + link_address);
				continue;
			}
			List<Ftn2D> path = FtnTools.read2D(mail.getPath());
			seenby.add(our2d);
			seenby.add(link2d);

			if (!path.contains(our2d)) {
				path.add(our2d);
			}

			List<Subscription> ssubs = ORMManager.INSTANSE.getSubscriptionDAO()
					.getAnd("echoarea_id", "=", area);
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
			message.setFromAddr(Main.info.getAddress());
			message.setToAddr(link_address);
			message.setDate(mail.getDate());
			message.setSubject(mail.getSubject());
			message.setText(mail.getText());
			message.setSeenby(new ArrayList<Ftn2D>(seenby));
			message.setPath(path);
			logger.l4("Пакуем сообщение #" + mail.getId() + " ("
					+ area.getName() + ") для " + link.getLinkAddress());
			messages.add(message);

		}
		ORMManager.INSTANSE.getEchomailAwaitingDAO().delete("link_id", "=",
				link, "echomail_id", "in", toRemove);
		if (!messages.isEmpty()) {
			List<Message> ret = FtnTools.pack(messages, link);
			for (File f : attachedFiles) {
				try {
					ret.add(new Message(f));
					f.delete();
				} catch (Exception e) {
					logger.l3("Не могу прикрепить файл " + f.getAbsolutePath());
				}
			}
			return ret;
		} else {
			return new ArrayList<Message>();
		}
	}
}
