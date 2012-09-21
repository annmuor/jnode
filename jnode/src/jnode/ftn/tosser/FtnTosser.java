package jnode.ftn.tosser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.j256.ormlite.stmt.UpdateBuilder;

import jnode.dto.Dupe;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.Link;
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
import jnode.main.threads.Client.Poll;
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
		List<Link> pollAfterEnd = new ArrayList<Link>();
		Map<String, Integer> tossed = new HashMap<String, Integer>();
		Map<String, Integer> bad = new HashMap<String, Integer>();
		FtnPkt[] pkts = FtnTools.unpack(message);
		for (FtnPkt pkt : pkts) {
			if (message.isSecure()) {
				if (!link.getPaketPassword()
						.equalsIgnoreCase(pkt.getPassword())) {
					logger.warn("Пароль для пакета не совпал - пакет уничтожен");
					continue;
				}
			}
			for (FtnMessage ftnm : pkt.getMessages()) {
				if (message.isSecure()) {
					if (FtnTools.checkRobot(ftnm)) {
						continue;
					}
				}
				if (ftnm.isNetmail()) {
					// проверить from и to
					FtnNdlAddress from = NodelistScanner.getInstance()
							.isExists(ftnm.getFromAddr());
					FtnNdlAddress to = NodelistScanner.getInstance().isExists(
							ftnm.getToAddr());
					if (from == null) {
						logger.warn(String
								.format("Netmail %s -> %s уничтожен ( отправитель не найден в нодлисте )",
										ftnm.getFromAddr().toString(), ftnm
												.getToAddr().toString()));
						continue;
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
						continue;
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
						continue;
					}
					try {
						List<Rewrite> rewrites = ORMManager.rewrite()
								.queryBuilder().orderBy("nice", true).where()
								.eq("type", (Rewrite.Type.NETMAIL)).query();
						for (Rewrite rewrite : rewrites) {
							if (FtnTools.completeMask(rewrite, ftnm)) {
								logger.info("(N) Найдено соответствие, переписываем сообщение "
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
						ORMManager.netmail().create(netmail);
						Integer n = tossed.get("netmail");
						tossed.put("netmail", (n == null) ? 1 : n + 1);
						if (routeVia == null) {
							logger.warn(String
									.format("Netmail %s -> %s не будет отправлен ( не найден роутинг )",
											ftnm.getFromAddr().toString(), ftnm
													.getToAddr().toString()));
						} else {
							routeVia = ORMManager.link().queryForSameId(
									routeVia);
							logger.info(String
									.format("Netmail %s -> %s будет отправлен через %s",
											ftnm.getFromAddr().toString(), ftnm
													.getToAddr().toString(),
											routeVia.getLinkAddress()));
							pollAfterEnd.add(routeVia);
						}
					} catch (SQLException e) {
						e.printStackTrace();
						logger.error("Ошибка при сохранении нетмейла", e);
					}
				} else if (message.isSecure()) {
					try {
						Echoarea area;
						Subscription sub;
						boolean flag = true;
						{
							List<Echoarea> areas = ORMManager.echoarea()
									.queryForEq("name",
											ftnm.getArea().toLowerCase());
							if (areas.isEmpty()) {
								// TODO: autoCreate
								area = new Echoarea();
								area.setName(ftnm.getArea().toLowerCase());
								area.setDescription("Autocreated echoarea");
								ORMManager.echoarea().create(area);
								sub = new Subscription();
								sub.setArea(area);
								sub.setLink(link);
								sub.setLast(0L);
								ORMManager.subscription().create(sub);
							} else {
								area = areas.get(0);
								List<Subscription> subs = ORMManager
										.subscription().queryBuilder().where()
										.eq("echoarea_id", area.getId()).and()
										.eq("link_id", link.getId()).query();
								if (!subs.isEmpty()) {
									sub = subs.get(0);
								} else {
									flag = false;
								}
							}
						}
						if (flag) {
							try {
								if (!ORMManager.dupe().queryBuilder().where()
										.eq("msgid", ftnm.getMsgid()).and()
										.eq("echoarea_id", area).query()
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
								List<Rewrite> rewrites = ORMManager.rewrite()
										.queryBuilder().orderBy("nice", true)
										.where()
										.eq("type", Rewrite.Type.ECHOMAIL)
										.query();
								for (Rewrite rewrite : rewrites) {
									if (FtnTools.completeMask(rewrite, ftnm)) {
										logger.info("(E) Найдено соответствие, переписываем сообщение "
												+ ftnm.getMsgid());
										FtnTools.rewrite(rewrite, ftnm);
										if (rewrite.isLast()) {
											break;
										}
									}
								}
							} catch (SQLException e1) {
								e1.printStackTrace();
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
							ORMManager.echomail().create(mail);
							try {
								Dupe dupe = new Dupe();
								dupe.setEchoarea(area);
								dupe.setMsgid(ftnm.getMsgid());
								ORMManager.dupe().create(dupe);
							} catch (SQLException e1) {
								logger.warn(
										"Не удалось записать "
												+ ftnm.getMsgid()
												+ " на в базу дюпов", e1);
							}
							// метка что уже прочитано
							Readsign sign = new Readsign();
							sign.setLink(link);
							sign.setMail(mail);
							ORMManager.readsign().create(sign);
							Integer n = tossed.get(ftnm.getArea());
							tossed.put(ftnm.getArea(), (n == null) ? 1 : n + 1);
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
					logger.info("Эхомейл по unsecure-соединению - уничтожен");
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
		if (!pollAfterEnd.isEmpty()) {
			for (Link l : pollAfterEnd) {
				if (!"".equals(l.getProtocolHost()) && l.getProtocolPort() > 0) {
					new Poll(l).start();
				}
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
			List<Netmail> netmails = ORMManager.netmail().queryBuilder()
					.where().eq("send", false).and().eq("route_via", link)
					.query();
			if (!netmails.isEmpty()) {
				for (Netmail netmail : netmails) {
					FtnMessage msg = FtnTools.netmailToFtnMessage(netmail);
					messages.add(msg);
					logger.debug(String.format(
							"Пакуем netmail #%d %s -> %s для %s",
							netmail.getId(), netmail.getFromFTN(),
							netmail.getToFTN(), link.getLinkAddress()));
					netmail.setSend(true);
					ORMManager.netmail().update(netmail);
				}
			}
		} catch (Exception e) {
			logger.error(
					"Ошибка обработки netmail для " + link.getLinkAddress(), e);
		}
		try {

			List<Subscription> subscr = ORMManager.subscription().queryForEq(
					"link_id", link.getId());
			for (Subscription sub : subscr) {
				List<Echomail> newmail = ORMManager.echomail().queryBuilder()
						.orderBy("id", true).where()
						.eq("echoarea_id", sub.getArea().getId()).and()
						.gt("id", sub.getLast()).query();
				for (Echomail mail : newmail) {
					if (mail.getId() > sub.getLast()) {
						sub.setLast(mail.getId());
					}
					List<Readsign> signs = ORMManager.readsign().queryBuilder()
							.where().eq("link_id", link.getId()).and()
							.eq("echomail_id", mail.getId()).query();
					if (signs.isEmpty()) {
						Set<Ftn2D> seenby = new HashSet<Ftn2D>(
								FtnTools.read2D(mail.getSeenBy()));
						/**
						 * Если мы пакуем на линка - то чекаем синбаи
						 */
						if (seenby.contains(link2d)
								&& link_address.getPoint() == 0) {
							logger.info(our2d + " есть в синбаях для "
									+ link_address);
						} else {
							seenby.add(our2d);
							seenby.add(link2d);
							for (Subscription s : ORMManager.subscription()
									.queryForEq("echoarea_id",
											mail.getArea().getId())) {
								Link l = ORMManager.link().queryForSameId(
										s.getLink());
								FtnAddress addr = new FtnAddress(
										l.getLinkAddress());
								Ftn2D d2 = new Ftn2D(addr.getNet(),
										addr.getNode());
								seenby.add(d2);
							}

							List<Ftn2D> path = FtnTools.read2D(mail.getPath());
							if (!path.contains(our2d)) {
								path.add(our2d);
							}
							mail.setPath(FtnTools.write2D(path, false));
							mail.setSeenBy(FtnTools.write2D(
									new ArrayList<Ftn2D>(seenby), true));
							logger.info("Пакуем сообщение #" + mail.getId()
									+ " (" + mail.getArea().getName()
									+ ") для " + link.getLinkAddress());
							messages.add(FtnTools.echomailToFtnMessage(mail));
						}
						Readsign sign = new Readsign();
						sign.setLink(link);
						sign.setMail(mail);
						ORMManager.readsign().create(sign);
					}
				}
				{
					UpdateBuilder<Subscription, ?> upd = ORMManager
							.subscription().updateBuilder();
					upd.updateColumnValue("lastmessageid", sub.getLast());
					upd.where().eq("link_id", sub.getLink()).and()
							.eq("echoarea_id", sub.getArea());
					ORMManager.subscription().update(upd.prepare());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(
					"Ошибка обработки echomail для " + link.getLinkAddress(), e);
		}
		if (!messages.isEmpty()) {
			return FtnTools.pack(messages, link_address,
					link.getPaketPassword());
		} else {
			return new ArrayList<Message>();
		}
	}

}
