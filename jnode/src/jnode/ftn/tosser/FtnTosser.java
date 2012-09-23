package jnode.ftn.tosser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.UpdateBuilder;

import jnode.dto.Dupe;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Netmail;
import jnode.dto.Readsign;
import jnode.dto.Rewrite;
import jnode.dto.Subscription;
import jnode.ftn.FtnTools;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.main.threads.PollQueue;
import jnode.ndl.FtnNdlAddress;
import jnode.ndl.FtnNdlAddress.Status;
import jnode.ndl.NodelistScanner;
import jnode.orm.ORMManager;
import jnode.protocol.io.Message;

/**
 * 
 * @author kreon
 * 
 */
public class FtnTosser {
	private static final Logger logger = Logger.getLogger(FtnTosser.class);

	/**
	 * Получаем сообщения из бандлов
	 * 
	 * @param connector
	 */
	public static void tossIncoming(Message message, Link link) {
		if (message == null) {
			return;
		}
		Map<String, Integer> tossed = new HashMap<String, Integer>();
		Map<String, Integer> bad = new HashMap<String, Integer>();
		FtnPkt[] pkts = FtnTools.unpack(message);
		for (FtnPkt pkt : pkts) {
			if (message.isSecure()) {
				if (!FtnTools.getOptionBooleanDefFalse(link,
						LinkOption.BOOLEAN_IGNORE_PKTPWD)) {
					if (!link.getPaketPassword().equalsIgnoreCase(
							pkt.getPassword())) {
						logger.warn("Пароль для пакета не совпал - пакет перемещен в inbound");
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
					boolean drop = false;
					FtnNdlAddress from = NodelistScanner.getInstance()
							.isExists(ftnm.getFromAddr());
					FtnNdlAddress to = NodelistScanner.getInstance().isExists(
							ftnm.getToAddr());

					if (from == null) {
						logger.warn(String
								.format("Netmail %s -> %s уничтожен ( отправитель не найден в нодлисте )",
										ftnm.getFromAddr().toString(), ftnm
												.getToAddr().toString()));
						drop = true;

					} else if (to == null) {
						try {
							FtnTools.writeReply(
									ftnm,
									"Destination not found",
									"Sorry, but destination of your netmail is not found in nodelist\nMessage rejected");
						} catch (SQLException e) {
							logger.error("Не удалось написать сообщение в ответ");
						}
						logger.warn(String
								.format("Netmail %s -> %s уничтожен ( получатель не найден в нодлисте )",
										ftnm.getFromAddr().toString(), ftnm
												.getToAddr().toString()));

						drop = true;
					} else if (to.getStatus().equals(Status.DOWN)) {
						try {
							FtnTools.writeReply(ftnm, "Destination is DOWN",
									"Sorry, but destination of your netmail is DOWN\nMessage rejected");
						} catch (SQLException e) {
							logger.error("Не удалось написать сообщение в ответ");
						}
						logger.warn(String
								.format("Netmail %s -> %s уничтожен ( получатель имеет статус Down )",
										ftnm.getFromAddr().toString(), ftnm
												.getToAddr().toString()));
						drop = true;
					}

					if (drop) {
						Integer n = bad.get("netmail");
						bad.put("netmail", (n == null) ? 1 : n + 1);
					} else {
						if ((ftnm.getAttribute() & FtnMessage.ATTR_ARQ) > 0) {
							try {
								FtnTools.writeReply(ftnm, "ARQ reply",
										"Your message was successfully reached this system");
							} catch (SQLException e) {
							}
						}
						try {
							List<Rewrite> rewrites = ORMManager.INSTANSE
									.rewrite().queryBuilder()
									.orderBy("nice", true).where()
									.eq("type", (Rewrite.Type.NETMAIL)).query();
							for (Rewrite rewrite : rewrites) {
								if (FtnTools.completeMask(rewrite, ftnm)) {
									logger.debug("(N) Найдено соответствие, переписываем сообщение "
											+ ftnm.getMsgid());
									FtnTools.rewrite(rewrite, ftnm);
									if (rewrite.isLast()) {
										break;
									}
								}
							}
						} catch (SQLException e1) {
							logger.warn("Не удалось получить rewrite", e1);
						}
						Link routeVia = FtnTools.getRouting(ftnm);

						try {
							Netmail netmail = new Netmail();
							netmail.setRouteVia(routeVia);
							netmail.setDate(ftnm.getDate());
							netmail.setFromFTN(ftnm.getFromAddr().toString());
							netmail.setToFTN(ftnm.getToAddr().toString());
							netmail.setFromName(ftnm.getFromName());
							netmail.setToName(ftnm.getToName());
							netmail.setSubject(ftnm.getSubject());
							netmail.setText(ftnm.getText());
							netmail.setAttr(ftnm.getAttribute());
							ORMManager.INSTANSE.netmail().create(netmail);
							Integer n = tossed.get("netmail");
							tossed.put("netmail", (n == null) ? 1 : n + 1);
							if (routeVia == null) {
								logger.warn(String
										.format("Netmail %s -> %s не будет отправлен ( не найден роутинг )",
												ftnm.getFromAddr().toString(),
												ftnm.getToAddr().toString()));
							} else {
								routeVia = ORMManager.INSTANSE.link()
										.queryForSameId(routeVia);
								logger.debug(String
										.format("Netmail %s -> %s будет отправлен через %s",
												ftnm.getFromAddr().toString(),
												ftnm.getToAddr().toString(),
												routeVia.getLinkAddress()));
								if (FtnTools.getOptionBooleanDefTrue(link,
										LinkOption.BOOLEAN_CRASH_NETMAIL)) {
									PollQueue.INSTANSE.add(routeVia);
								}
							}
						} catch (SQLException e) {
							e.printStackTrace();
							logger.error("Ошибка при сохранении нетмейла", e);
						}
					}
				} else if (message.isSecure()) {
					try {
						Echoarea area = null;
						boolean flag = false;
						{
							List<Echoarea> areas = ORMManager.INSTANSE
									.echoarea().queryForEq("name",
											ftnm.getArea().toLowerCase());
							if (areas.isEmpty()) {
								if (FtnTools.getOptionBooleanDefFalse(link,
										LinkOption.BOOLEAN_AUTOCREATE_AREA)) {
									area = new Echoarea();
									area.setName(ftnm.getArea().toLowerCase());
									area.setDescription("Autocreated echoarea");
									ORMManager.INSTANSE.echoarea().create(area);
									Subscription sub = new Subscription();
									sub.setArea(area);
									sub.setLink(link);
									sub.setLast(0L);
									ORMManager.INSTANSE.subscription().create(
											sub);
									flag = true;
								}
							} else {
								area = areas.get(0);
								List<Subscription> subs = ORMManager.INSTANSE
										.subscription().queryBuilder().where()
										.eq("echoarea_id", area.getId()).and()
										.eq("link_id", link.getId()).query();
								if (!subs.isEmpty()) {
									flag = true;
								}
							}
						}
						if (flag) {
							try {
								if (!ORMManager.INSTANSE.dupe().queryBuilder()
										.where().eq("msgid", ftnm.getMsgid())
										.and().eq("echoarea_id", area).query()
										.isEmpty()) {
									logger.warn(ftnm.getMsgid()
											+ " дюп - уничтожен");
									Integer n = bad.get(ftnm.getArea());
									bad.put(ftnm.getArea(), (n == null) ? 1
											: n + 1);
								}
							} catch (SQLException e) {
								logger.warn(
										"Не удалось проверить "
												+ ftnm.getMsgid() + " на дюпы",
										e);
							}
							try {
								List<Rewrite> rewrites = ORMManager.INSTANSE
										.rewrite().queryBuilder()
										.orderBy("nice", true).where()
										.eq("type", Rewrite.Type.ECHOMAIL)
										.query();
								for (Rewrite rewrite : rewrites) {
									if (FtnTools.completeMask(rewrite, ftnm)) {
										logger.debug("(E) Найдено соответствие, переписываем сообщение "
												+ ftnm.getMsgid());
										FtnTools.rewrite(rewrite, ftnm);
										if (rewrite.isLast()) {
											break;
										}
									}
								}
							} catch (SQLException e1) {
								logger.warn("Не удалось получить rewrite", e1);
							}
							Echomail mail = new Echomail();
							mail.setArea(area);
							mail.setDate(ftnm.getDate());
							mail.setFromFTN(ftnm.getFromAddr().toString());
							mail.setFromName(ftnm.getFromName());
							mail.setToName(ftnm.getToName());
							mail.setSubject(ftnm.getSubject());
							mail.setText(ftnm.getText());
							mail.setSeenBy(FtnTools.write2D(ftnm.getSeenby(),
									true));
							mail.setPath(FtnTools.write2D(ftnm.getPath(), false));
							ORMManager.INSTANSE.echomail().create(mail);

							{
								try {
									Dupe dupe = new Dupe();
									dupe.setEchoarea(area);
									dupe.setMsgid(ftnm.getMsgid());
									ORMManager.INSTANSE.dupe().create(dupe);
								} catch (SQLException e1) {
								}

								Readsign sign = new Readsign();
								sign.setLink(link);
								sign.setMail(mail);
								ORMManager.INSTANSE.readsign().create(sign);
							}

							Integer n = tossed.get(ftnm.getArea());
							tossed.put(ftnm.getArea(), (n == null) ? 1 : n + 1);

							for (Link l : FtnTools.getSubscribers(area, link)) {
								if (FtnTools.getOptionBooleanDefFalse(l,
										LinkOption.BOOLEAN_CRASH_ECHOMAIL)) {
									PollQueue.INSTANSE.add(l);
								}
							}

						} else {
							Integer n = bad.get(ftnm.getArea());
							bad.put(ftnm.getArea(), (n == null) ? 1 : n + 1);
						}
					} catch (SQLException e) {
						logger.error(
								"Не удалось записать сообщение "
										+ ftnm.getMsgid(), e);
						Integer n = bad.get(ftnm.getArea());
						bad.put(ftnm.getArea(), (n == null) ? 1 : n + 1);
					}
				} else {
					logger.warn("Эхомейл по unsecure-соединению - уничтожен");
				}
			}
		}
		if (!tossed.isEmpty()) {
			logger.info("Записано сообщений:");
			for (String area : tossed.keySet()) {
				logger.info(String.format("\t%s - %d", area, tossed.get(area)));
			}
		}
		if (!bad.isEmpty()) {
			logger.warn("Уничтожено сообщений:");
			for (String area : bad.keySet()) {
				logger.warn(String.format("\t%s - %d", area, bad.get(area)));
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

		try {
			List<Netmail> netmails = ORMManager.INSTANSE.netmail()
					.queryBuilder().where().eq("send", false).and()
					.eq("route_via", link).query();
			if (!netmails.isEmpty()) {
				for (Netmail netmail : netmails) {
					FtnMessage msg = FtnTools.netmailToFtnMessage(netmail);
					messages.add(msg);
					logger.debug(String.format(
							"Пакуем netmail #%d %s -> %s для %s",
							netmail.getId(), netmail.getFromFTN(),
							netmail.getToFTN(), link.getLinkAddress()));
					netmail.setSend(true);
					ORMManager.INSTANSE.netmail().update(netmail);
				}
			}
		} catch (Exception e) {
			logger.error(
					"Ошибка обработки netmail для " + link.getLinkAddress(), e);
		}
		try {
			final String echomail_query = "SELECT a.name as AREA,e.* FROM subscription s LEFT JOIN echoarea"
					+ " a ON (a.id=s.echoarea_id) LEFT JOIN echomail e ON"
					+ " (e.echoarea_id=s.echoarea_id) WHERE e.id > s.lastmessageid AND"
					+ " e.id NOT IN (SELECT r.echomail_id FROM readsing r WHERE"
					+ " r.link_id=s.link_id AND r.echomail_id > s.lastmessageid) AND"
					+ " s.link_id=%d ORDER BY e.id ASC LIMIT 100";
			final String seenby_query = "SELECT l.ftn_address from subscription s left join links l on (l.id=s.link_id) where s.echoarea_id=%d";
			DataType[] types = new DataType[] { DataType.STRING, // area [0]
					DataType.LONG_OBJ, // id [1]
					DataType.LONG_OBJ, // earea_id [2]
					DataType.STRING, // from name [3]
					DataType.STRING, // to name [4]
					DataType.STRING, // from addr [5]
					DataType.DATE_LONG, // date [6]
					DataType.LONG_STRING, // subject [7]
					DataType.LONG_STRING, // message [8]
					DataType.LONG_STRING, // seenby [9]
					DataType.LONG_STRING // path [10]
			};
			Map<Long, Long> subcription = new HashMap<Long, Long>();
			List<Long> signs = new ArrayList<Long>();
			GenericRawResults<Object[]> results = ORMManager.INSTANSE
					.echomail().queryRaw(
							String.format(echomail_query, link.getId()), types);
			if (results.getNumberColumns() > 0) {
				for (Object[] result : results.getResults()) {
					Set<Ftn2D> seenby = new HashSet<Ftn2D>(
							FtnTools.read2D((String) result[9]));
					signs.add((Long) result[1]);
					subcription.put((Long) result[2], (Long) result[1]);
					if (seenby.contains(link2d) && link_address.getPoint() == 0) {
						logger.debug(our2d + " есть в синбаях для "
								+ link_address);
					} else {
						seenby.add(our2d);
						seenby.add(link2d);
						List<Ftn2D> path = FtnTools.read2D((String) result[10]);
						if (!path.contains(our2d)) {
							path.add(our2d);
						}
						GenericRawResults<String[]> seens = ORMManager.INSTANSE
								.link().queryRaw(
										String.format(seenby_query,
												(Long) result[2]));
						for (String[] seen : seens.getResults()) {
							FtnAddress addr = new FtnAddress(seen[0]);
							Ftn2D d2 = new Ftn2D(addr.getNet(), addr.getNode());
							seenby.add(d2);
						}
						FtnMessage message = new FtnMessage();
						message.setNetmail(false);
						message.setArea(((String) result[0]).toUpperCase());
						message.setFromName((String) result[3]);
						message.setToName((String) result[4]);
						message.setFromAddr(Main.info.getAddress());
						message.setToAddr(link_address);
						message.setDate((Date) result[6]);
						message.setSubject((String) result[7]);
						message.setText((String) result[8]);
						message.setSeenby(new ArrayList<Ftn2D>(seenby));
						message.setPath(path);
						logger.debug("Пакуем сообщение #" + result[1] + " ("
								+ result[0] + ") для " + link.getLinkAddress());
						messages.add(message);
					}
				}
				for (Long id : signs) {
					Echomail m = new Echomail();
					m.setId(id);
					ORMManager.INSTANSE.readsign()
							.create(new Readsign(link, m));

				}
				for (Long echoid : subcription.keySet()) {
					UpdateBuilder<Subscription, ?> upd = ORMManager.INSTANSE
							.subscription().updateBuilder();
					upd.updateColumnValue("lastmessageid",
							subcription.get(echoid));
					upd.where().eq("link_id", link).and()
							.eq("echoarea_id", echoid);
					ORMManager.INSTANSE.subscription().update(upd.prepare());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(
					"Ошибка обработки echomail для " + link.getLinkAddress(), e);
		}
		if (!messages.isEmpty()) {
			return FtnTools.pack(messages, link);
		} else {
			return new ArrayList<Message>();
		}
	}
}
