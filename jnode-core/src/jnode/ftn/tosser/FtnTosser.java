/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.ftn.tosser;

import jnode.core.FileUtils;
import jnode.dto.*;
import jnode.event.NewEchomailEvent;
import jnode.event.NewFilemailEvent;
import jnode.event.NewNetmailEvent;
import jnode.event.Notifier;
import jnode.ftn.FtnTools;
import jnode.ftn.types.*;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.threads.PollQueue;
import jnode.main.threads.TosserQueue;
import jnode.orm.ORMManager;
import jnode.protocol.io.Message;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static jnode.ftn.FtnTools.*;

/**
 * @author kreon
 */
public class FtnTosser {
	private static final String FILEECHO_ENABLE = "fileecho.enable";
	private static final String FILEECHO_PATH = "fileecho.path";
	private static final Logger logger = Logger.getLogger(FtnTosser.class);
	private final Map<String, Integer> tossed = new HashMap<>();
	private final Map<String, Integer> bad = new HashMap<>();
	private final Set<Link> pollLinks = new HashSet<>();

	/**
	 * Разбор нетмейла
	 * 
	 * @param netmail
	 * @param secure
	 */
	private void tossNetmail(FtnMessage netmail, boolean secure) {
		if (secure) {
			if (checkRobot(netmail)) {
				return;
			}
		}
		boolean drop = checkNetmailMustDropped(netmail);

		if (drop) {
			Integer n = bad.get("netmail");
			bad.put("netmail", (n == null) ? 1 : n + 1);
		} else {
			if ((netmail.getAttribute() & FtnMessage.ATTR_ARQ) > 0) {
				writeReply(netmail, "ARQ reply",
						"Your message was successfully reached this system");
			}
			processRewrite(netmail);
			Link routeVia = getRouting(netmail);

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
			ORMManager.get(Netmail.class).save(dbnm);
			Notifier.INSTANSE.notify(new NewNetmailEvent(dbnm));
			Integer n = tossed.get("netmail");
			tossed.put("netmail", (n == null) ? 1 : n + 1);
			if (routeVia == null) {
				logger.l4(String
						.format("Netmail %s -> %s is not transferred ( routing not found )",
								netmail.getFromAddr().toString(), netmail
										.getToAddr().toString()));
			} else {
				routeVia = ORMManager.get(Link.class).getById(routeVia.getId());
				logger.l4(String.format("Netmail %s -> %s transferred via %s",
						netmail.getFromAddr().toString(), netmail.getToAddr()
								.toString(), routeVia.getLinkAddress()));
				if (getOptionBooleanDefTrue(routeVia,
						LinkOption.BOOLEAN_CRASH_NETMAIL)) {
					PollQueue.getSelf().add(routeVia);
				}
			}
		}
	}

	private void tossEchomail(FtnMessage echomail, Link link, boolean secure) {

		if (!secure) {
			logger.l3("Echomail via unsecure is dropped");
			return;
		}
		Echoarea area = getAreaByName(echomail.getArea(), link);
		if (area == null) {
			logger.l3("Echoarea " + echomail.getArea()
					+ " is not availible for " + link.getLinkAddress());
			Integer n = bad.get(echomail.getArea());
			bad.put(echomail.getArea(), (n == null) ? 1 : n + 1);
			return;
		}

		Long rl = getOptionLong(link, LinkOption.LONG_LINK_LEVEL);
		if (rl.longValue() < area.getWritelevel()) {
			writeNetmail(
					getPrimaryFtnAddress(),
					new FtnAddress(link.getLinkAddress()),
					MainHandler.getCurrentInstance().getInfo().getStationName(),
					link.getLinkName(),
					"[" + area.getName() + "]: post rejected",
					String.format(
							"Sorry, you have no enough level to post to this area\n%s",
							quote(echomail), MainHandler.getVersion()));
			logger.l3("Echoarea " + echomail.getArea()
					+ " is not availible for " + link.getLinkAddress()
					+ " (level mismatch)");
			Integer n = bad.get(echomail.getArea());
			bad.put(echomail.getArea(), (n == null) ? 1 : n + 1);
			return;
		}
		// попадаются злобные сообщения без MSGID

		if (echomail.getMsgid() != null) {
			if (isADupe(area, echomail.getMsgid())) {
				logger.l3("Message " + echomail.getArea() + " "
						+ echomail.getMsgid() + " is a dupe");
				Integer n = bad.get(echomail.getArea());
				bad.put(echomail.getArea(), (n == null) ? 1 : n + 1);
				return;
			}
		}

		processRewrite(echomail);

		Echomail mail = new Echomail();
		mail.setArea(area);
		mail.setDate(echomail.getDate());
		mail.setFromFTN(echomail.getFromAddr().toString());
		mail.setFromName(echomail.getFromName());
		mail.setToName(echomail.getToName());
		mail.setSubject(echomail.getSubject());
		mail.setText(echomail.getText());
		mail.setSeenBy(write2D(echomail.getSeenby(), true));
		mail.setPath(write2D(echomail.getPath(), false));
		mail.setMsgid(echomail.getMsgid());
		ORMManager.get(Echomail.class).save(mail);
		if (mail.getId() != null) {
			for (Subscription sub : getSubscription(area)) {
				if (link == null
						|| !sub.getLink().equals(link)
						&& !getOptionBooleanDefFalse(sub.getLink(),
								LinkOption.BOOLEAN_PAUSE)) {
					ORMManager.get(EchomailAwaiting.class).save(
							new EchomailAwaiting(sub.getLink(), mail));
					pollLinks.add(sub.getLink());
				}
			}
		}
		Notifier.INSTANSE.notify(new NewEchomailEvent(mail));
		Integer n = tossed.get(echomail.getArea());
		tossed.put(echomail.getArea(), (n == null) ? 1 : n + 1);

	}

	/**
	 * Получаем сообщения из бандлов
	 * 
	 * @param message
	 *            сообщение
	 */
	public static int tossIncoming(Message message) {
		if (message == null) {
			return 0;
		}

		try {
			unpack(message);
			TosserQueue.getInstanse().toss();
		} catch (IOException e) {
			logger.l1(
					"Exception file tossing message "
							+ message.getMessageName(), e);
			return 1;
		}
		try {
			message.getInputStream().close();
		} catch (IOException ignore) {
		}
		message.delete();
		return 0;
	}

	/**
	 * Разбор файлов в папке inbound
	 */
	public synchronized void tossInboundDirectory() {
		logger.l5("Start tossInboundDirectory()");
		Set<Link> poll = new HashSet<>();
		File inbound = new File(getInbound());
		final File[] listFiles = inbound.listFiles();
		if (listFiles != null) {
			for (File file : listFiles) {
				String loname = file.getName().toLowerCase();
				if (loname.matches("^[a-f0-9]{8}\\.pkt$")) {
					try {
						Message m = new Message(file);
						logger.l4("Tossing file " + file.getAbsolutePath());
						FtnMessage ftnm;
						FtnPkt pkt = new FtnPkt();
						pkt.unpack(m.getInputStream());
						while ((ftnm = pkt.getNextMessage()) != null) {
							if (ftnm.isNetmail()) {
								tossNetmail(ftnm, true);
							} else {
								tossEchomail(ftnm, null, true);
							}
						}
						file.delete();
					} catch (Exception e) {
						markAsBad(file, "Tossing failed");
						logger.l2(
								"Error while tossing: "
										+ e.getLocalizedMessage(), e);
					}
				} else if (loname.matches("(s|u)inb\\d*.pkt")) {
					try {
						Message m = new Message(file);
						logger.l4("Tossing file " + file.getAbsolutePath());
						FtnPkt pkt = new FtnPkt();
						pkt.unpack(m.getInputStream());
						Link link = getLinkByFtnAddress(pkt.getFromAddr());
						boolean secure = loname.charAt(0) == 's'
								&& link != null;
						if (secure) {
							if (!getOptionBooleanDefTrue(link,
									LinkOption.BOOLEAN_IGNORE_PKTPWD)) {
								if (!link.getPaketPassword().equalsIgnoreCase(
										pkt.getPassword())) {
									logger.l2("Pkt password mismatch - package moved to inbound");
									markAsBad(file, "Password mismatch");
									continue;
								}
							}
						}
						FtnMessage ftnm;
						while ((ftnm = pkt.getNextMessage()) != null) {
							if (ftnm.isNetmail()) {
								tossNetmail(ftnm, secure);
							} else {
								tossEchomail(ftnm, link, secure);
							}
						}
						file.delete();
					} catch (Exception e) {
						markAsBad(file, "Tossing failed");
						logger.l2(
								"Error while tossing: "
										+ e.getLocalizedMessage(), e);
					}
				} else if (loname.matches("^[a-z0-9]{8}\\.tic$")) {
					if (!MainHandler.getCurrentInstance().getBooleanProperty(
							FILEECHO_ENABLE, true)) {
						continue;
					}
					logger.l3("Proccessing " + file.getName());
					try {
						FileInputStream fis = new FileInputStream(file);
						FtnTIC tic = new FtnTIC();
						tic.unpack(fis);
						fis.close();
						String filename = tic.getFile().toLowerCase();
						File attach = FtnTools.guessFilename(filename, true);
						if (attach != null && attach.canRead()) { // processing
							logger.l3("File found as " + filename);
							if (!MainHandler.getCurrentInstance().getInfo()
									.getAddressList().contains(tic.getTo())) {
								markAsBad(file, "Destination mismatch");
								continue;
							}
							Link source = getLinkByFtnAddress(tic.getFrom());
							if (source == null) {
								markAsBad(file, "Source address not found");
								continue;
							}
							Filearea area = getFileareaByName(tic.getArea(),
									source);
							if (area == null) {
								markAsBad(file, "Filearea is not availible");
								continue;
							}
							Filemail mail = new Filemail();
							File newFile = new File(getFilePath(area.getName(),
									tic.getFile()));
							if (FileUtils.move(attach, newFile, true)) {
								mail.setFilepath(newFile.getAbsolutePath());
							} else {
								mail.setFilepath(attach.getAbsolutePath());
							}
							mail.setFilearea(area);
							mail.setFilename(tic.getFile());
							mail.setFiledesc(tic.getDesc());
							mail.setOrigin(tic.getOrigin().toString());
							mail.setPath(tic.getPath());
							mail.setSeenby(write4D(tic.getSeenby()));
							mail.setCreated(new Date());
							ORMManager.get(Filemail.class).save(mail);
							if (mail.getId() != null) {
								for (FileSubscription sub : getSubscription(area)) {
									if (source != null) {
										if (sub.getLink().getId()
												.equals(source.getId())) {
											continue;
										}
									}
									if (!getOptionBooleanDefFalse(
											sub.getLink(),
											LinkOption.BOOLEAN_PAUSE)) {
										ORMManager.get(FilemailAwaiting.class)
												.save(new FilemailAwaiting(sub
														.getLink(), mail));
										if (getOptionBooleanDefFalse(
												sub.getLink(),
												LinkOption.BOOLEAN_CRASH_FILEMAIL)) {
											poll.add(sub.getLink());
										}
									}
								}
							}
							Notifier.INSTANSE
									.notify(new NewFilemailEvent(mail));
						} else {
							logger.l4("File " + filename
									+ " not found in inbound, waiting");
							continue;
						}
						file.delete();
					} catch (Exception e) {
						logger.l1(
								"Error while processing tic " + file.getName(),
								e);
					}
				} else if (loname.matches("^[0-9a-f]{8}\\..?lo$")) {
					FtnAddress address = getPrimaryFtnAddress().clone();
					address.setPoint(0);
					try {
						address.setNet(Integer.parseInt(loname.substring(0, 4),
								16));
						address.setNode(Integer.parseInt(
								loname.substring(4, 8), 16));
						Link l = getLinkByFtnAddress(address);
						if (l != null) {
							try {
								BufferedReader br = new BufferedReader(
										new FileReader(file));
								while (br.ready()) {
									String name = br.readLine();
									if (name != null) {
										File f = new File(name);
										if (f.exists() && f.canRead()) {
											FileForLink ffl = new FileForLink();
											ffl.setLink(l);
											ffl.setFilename(name);
											ORMManager.get(FileForLink.class)
													.save(ffl);
										} else {
											logger.l2("File from ?lo not exists: "
													+ name);
										}
									}
								}
								br.close();
							} catch (Exception e) {
								markAsBad(file, "Unable to read files in ?lo");
							}
							poll.add(l);
						}
					} catch (NumberFormatException e) {
						markAsBad(file, "?LO file is invalid");
					}
					file.delete();

				}
			}
		}
		for (Link l : poll) {
			PollQueue.getSelf().add(
					ORMManager.get(Link.class).getById(l.getId()));
		}
	}

	private void markAsBad(File file, String message) {
		logger.l2("File " + file.getName() + " is bad: " + message);
		file.renameTo(new File(file.getAbsolutePath() + ".bad"));
	}

	public static String getFileechoPath() {
		return MainHandler.getCurrentInstance().getProperty(FILEECHO_PATH,
				getInbound());
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
			if (getOptionBooleanDefFalse(l, LinkOption.BOOLEAN_CRASH_ECHOMAIL)) {
				PollQueue.getSelf().add(
						ORMManager.get(Link.class).getById(l.getId()));
			}
		}
		tossed.clear();
		bad.clear();
		pollLinks.clear();
	}

	private List<Message> packNetmail(FtnAddress address) {
		Link link = getLinkByFtnAddress(address);
		LinkedList<Message> messages = new LinkedList<>();
		if (link == null) {
			link = new Link();
			link.setLinkAddress(address.toString());
			link.setPaketPassword("-");
		}
		FtnPkt header = new FtnPkt(selectOurAka(link), address,
				link.getPaketPassword(), new Date());
		List<Netmail> mail = null;
		boolean pack = (link.getId() != null) ? getOptionBooleanDefFalse(link,
				LinkOption.BOOLEAN_PACK_NETMAIL) : false;
		int num = 0;
		try {
			File f = createOutboundFile(link);
			FileOutputStream fos = new FileOutputStream(f);
			OutputStream os = (pack) ? new ZipOutputStream(fos) : fos;
			if (pack) {
				((ZipOutputStream) os).putNextEntry(new ZipEntry(generate8d()
						+ ".pkt"));
			}
			header.write(os);
			do {
				mail = new ArrayList<>();
				mail.addAll(ORMManager.get(Netmail.class).getAnd("send", "=",
						false, "to_address", "=", address.toString(),
						"route_via", "null"));
				if (link != null) {
					mail.addAll(getMail(link));
				}
				if (!mail.isEmpty()) {
					try {

						for (Netmail n : mail) {
							FtnMessage m = netmailToFtnMessage(n);
							m.write(os);
							num++;
							if ((n.getAttr() & FtnMessage.ATTR_FILEATT) >= FtnMessage.ATTR_FILEATT) {
								String filename = n.getSubject();
								File file = guessFilename(filename, true);
								if (file != null && file.canRead()) {
									messages.add(new Message(file));
									logger.l5("Netmail with attached file "
											+ filename);
								}
							}
						}
						for (Netmail n : mail) {
							n.setSend(true);
							ORMManager.get(Netmail.class).update(n);
							logger.l4(String
									.format("Netmail #%d %s -> %s for %s flags %d was packed",
											n.getId(), n.getFromFTN(),
											n.getToFTN(),
											link.getLinkAddress(), n.getAttr()));
						}
					} catch (IOException e) {
						logger.l2("Error while packing netmail", e);
					}
					mail.clear();
				}
			} while (mail != null && !mail.isEmpty());
			header.finalz(os);
			fos.close();
			if (num == 0) {
				f.delete();
			} else {
				Message m = new Message(f);
				if (pack) {
					m.setMessageName(generateEchoBundle());
				} else {
					m.setMessageName(generate8d() + ".pkt");
				}
				messages.add(m);
			}
		} catch (IOException e) {
			logger.l2("Error while packing messages", e);
		}
		return messages;
	}

	private List<Message> packEchomail(Link link, FtnAddress address) {
		LinkedList<Message> messages = new LinkedList<>();
		boolean pack = getOptionBooleanDefTrue(link,
				LinkOption.BOOLEAN_PACK_ECHOMAIL);
		Ftn2D link2d = new Ftn2D(address.getNet(), address.getNode());
		int num = 0;
		try {
			List<EchomailAwaiting> email = null;
			File f = createOutboundFile(link);
			FileOutputStream fos = new FileOutputStream(f);
			OutputStream os = (pack) ? new ZipOutputStream(fos) : fos;
			FtnPkt header = new FtnPkt(selectOurAka(link), address,
					link.getPaketPassword(), new Date());
			if (pack) {
				((ZipOutputStream) os).putNextEntry(new ZipEntry(generate8d()
						+ ".pkt"));
			}
			header.write(os);
			do {
				email = getEchoMail(link);
				if (!email.isEmpty()) {
					for (EchomailAwaiting e : email) {
						Echomail mail = e.getMail();
						if (mail == null) {
							deleteEAmail(e);
							continue;
						}
						Echoarea area = mail.getArea();
						if (area == null) {
							deleteEAmail(e);
							continue;
						}
						List<Ftn2D> path = read2D(mail.getPath());
						Set<Ftn2D> seenby = new HashSet<>(
								read2D(mail.getSeenBy()));
						if (seenby.contains(link2d) && address.getPoint() == 0) {
							logger.l5(link2d + " is in seenby for " + address);
							deleteEAmail(e);
							continue;
						}
						seenby.add(link2d);
						seenby.addAll(createSeenBy(area));
						for (FtnAddress addr : MainHandler.getCurrentInstance()
								.getInfo().getAddressList()) {
							Ftn2D me = new Ftn2D(addr.getNet(), addr.getNode());
							seenby.add(me);
							if (!path.contains(me)) {
								path.add(me);
							}
						}

						FtnMessage msg = createEchomail(address, mail, area,
								seenby, path);
						logger.l4("Echomail #" + mail.getId() + " ("
								+ area.getName() + ") packed for "
								+ link.getLinkAddress());
						msg.write(os);
						num++;
					}

					for (EchomailAwaiting e : email) {
						deleteEAmail(e);
					}
				}
			} while (!email.isEmpty());
			header.finalz(os);
			fos.close();
			if (num == 0) {
				f.delete();
			} else {
				Message m = new Message(f);
				if (pack) {
					m.setMessageName(generateEchoBundle());
				} else {
					m.setMessageName(generate8d() + ".pkt");
				}
				messages.add(m);
			}
		} catch (IOException e) {
			logger.l2("Error while packing echomails ", e);
		}
		return messages;
	}

	private void deleteEAmail(EchomailAwaiting e) {
		ORMManager.get(EchomailAwaiting.class).delete("link_id", "=",
				e.getLink(), "echomail_id", "null");
		ORMManager.get(EchomailAwaiting.class).delete("link_id", "=",
				e.getLink(), "echomail_id", "=", e.getMail());

	}

	private void deleteFAMail(FilemailAwaiting f) {
		ORMManager.get(FilemailAwaiting.class).delete("link_id", "=",
				f.getLink(), "filemail_id", "null");
		ORMManager.get(FilemailAwaiting.class).delete("link_id", "=",
				f.getLink(), "filemail_id", "=", f.getMail());

	}

	private List<Message> packFilemail(Link link, FtnAddress address) {
		List<Message> msgs = new LinkedList<>();
		List<FilemailAwaiting> filemail = null;
		do {
			filemail = getFileMail(link);
			if (!filemail.isEmpty()) {
				for (FilemailAwaiting f : filemail) {
					Filemail mail = f.getMail();
					if (mail == null) {
						deleteFAMail(f);
						continue;
					}
					Filearea area = mail.getFilearea();
					if (area == null) {

						continue;
					}

					File attach = new File(mail.getFilepath());
					if (!attach.canRead()) {
						deleteFAMail(f);
						logger.l3("File unavailible");
						continue;
					}

					Set<FtnAddress> seenby = new HashSet<>(
							read4D(mail.getSeenby()));
					if (seenby.contains(address)) {
						deleteFAMail(f);
						logger.l3("This file have a seen-by for link");
						continue;
					}
					for (FtnAddress our : MainHandler.getCurrentInstance()
							.getInfo().getAddressList()) {
						seenby.add(our);
					}
					for (FileSubscription sub : getSubscription(area)) {
						try {
							Link l = ORMManager.get(Link.class).getById(
									sub.getLink().getId());
							seenby.add(new FtnAddress(l.getLinkAddress()));
						} catch (NullPointerException e) {
							logger.l4("bad link for FileSubscription " + sub
									+ " - ignored", e);
						}
					}
					List<FtnAddress> sb = new ArrayList<>(seenby);
					Collections.sort(sb, new Ftn4DComparator());
					FtnTIC tic = createTic(link, mail, attach);
					tic.setTo(address);
					tic.setSeenby(sb);
					tic.setPath(mail.getPath() + "Path "
							+ getPrimaryFtnAddress() + " "
							+ System.currentTimeMillis() / 1000 + " "
							+ FORMAT.format(new Date()) + " "
							+ MainHandler.getVersion() + "\r\n");
					try {
						File tmp = File.createTempFile("outtic", ".tic",
								new File(getInbound()));
						FileOutputStream fos = new FileOutputStream(tmp);
						fos.write(tic.pack());
						fos.close();
						Message message = new Message(tmp);
						message.setMessageName(generateTic());
						msgs.add(message);
						Message m2 = new Message(mail.getFilename(),
								attach.length());
						m2.setInputStream(new FileInputStream(attach));
						msgs.add(m2);
						deleteFAMail(f);
					} catch (IOException e) {
						logger.l2("Error while packing tic:", e);
					}
				}
			}
		} while (!filemail.isEmpty());
		List<FileForLink> ffls = ORMManager.get(FileForLink.class).getAnd(
				"link_id", "eq", link);
		for (FileForLink ffl : ffls) {
			try {
				File file = new File(ffl.getFilename());
				Message m = new Message(file);
				ORMManager.get(FileForLink.class).delete("link_id", "=", link,
						"filename", "=", ffl.getFilename());
				msgs.add(m);
			} catch (Exception ex) {
				logger.l1(MessageFormat
						.format("Exception during get file {0} for link {1}",
								ffl, link), ex);
			}
		}
		return msgs;
	}

	public List<Message> getMessages2(FtnAddress address) {
		LinkedList<Message> messages = new LinkedList<>();
		String key = address.toString().intern();
		synchronized (key) {
			messages.addAll(packNetmail(address));
			Link link = getLinkByFtnAddress(address);
			if (link != null) {
				messages.addAll(packEchomail(link, address));
				messages.addAll(packFilemail(link, address));
				if (messages.isEmpty()) {
					File inbound = new File(getInbound());
					final File[] listFiles = inbound.listFiles();
					if (listFiles != null) {
						for (File file : listFiles) {
							String loname = file.getName().toLowerCase();
							if (loname.matches("^out_" + link.getId()
									+ "\\..*$")) {
								boolean packed = true;
								try {
									new ZipFile(file).close();
								} catch (Exception e) {
									packed = false;
								}
								try {
									Message m = new Message(file);
									if (packed) {
										m.setMessageName(generateEchoBundle());
									} else {
										m.setMessageName(generate8d() + ".pkt");
									}
									messages.add(m);
									break;
								} catch (Exception e) {
									// ignore
								}

							}
						}
					}
				}
			}

		}
		return messages;
	}

	/**
	 * Получить новые сообщения для линка
	 * 
	 * @param link
	 * @return
	 */
	public static List<Message> getMessagesForLink(Link link) {
		return TosserQueue.getInstanse().getMessages(link);
	}

	protected FtnTIC createTic(Link link, Filemail mail, File attach) {
		FtnTIC tic = new FtnTIC();
		try {
			CRC32 crc32 = new CRC32();
			FileInputStream fis = new FileInputStream(attach);
			int len;
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
			tic.setArea(mail.getFilearea().getName().toUpperCase());
			tic.setAreaDesc(mail.getFilearea().getDescription());
			tic.setFile(mail.getFilename());
			tic.setSize(attach.length());
			tic.setDesc(mail.getFiledesc());
			tic.setPassword(link.getPaketPassword());
			tic.setFrom(FtnTools.selectOurAka(link));
			tic.setTo(null);
			tic.setOrigin(new FtnAddress(mail.getOrigin()));
			return tic;
		} catch (IOException e) {
			logger.l2("fail process tic", e);
		}
		return null;
	}

	protected Set<Ftn2D> createSeenBy(Echoarea area) {
		List<Subscription> ssubs = getSubscription(area);
		Set<Ftn2D> seenby = new HashSet<>();
		for (Subscription ssub : ssubs) {
			try {
				Link _sslink = ORMManager.get(Link.class).getById(
						ssub.getLink().getId());
				FtnAddress addr = new FtnAddress(_sslink.getLinkAddress());
				Ftn2D d2 = new Ftn2D(addr.getNet(), addr.getNode());
				seenby.add(d2);
			} catch (NullPointerException e) {
				logger.l1("Bad link for subscriprion " + ssub + " : ignored", e);
			}

		}
		return seenby;
	}

	protected FtnMessage createEchomail(FtnAddress link_address, Echomail mail,
			Echoarea area, Set<Ftn2D> seenby, List<Ftn2D> path) {
		FtnMessage message = new FtnMessage();
		message.setNetmail(false);
		message.setArea(area.getName().toUpperCase());
		message.setFromName(mail.getFromName());
		message.setToName(mail.getToName());
		message.setFromAddr(getPrimaryFtnAddress());
		message.setToAddr(link_address);
		message.setDate(mail.getDate());
		message.setSubject(mail.getSubject());
		message.setText(mail.getText());
		message.setSeenby(new ArrayList<>(seenby));
		message.setPath(path);
		message.setMsgid(mail.getMsgid());
		return message;
	}

	private List<Netmail> getMail(Link link) {
		if (link.getId() != null) {
			return ORMManager.get(Netmail.class).getAnd("send", "=", false,
					"route_via", "=", link);
		} else {
			return new ArrayList<>();
		}
	}

	private List<EchomailAwaiting> getEchoMail(Link link) {
		if (link.getId() != null) {
			return ORMManager.get(EchomailAwaiting.class).getAnd("link_id",
					"=", link);
		} else {
			return new ArrayList<>();
		}
	}

	private List<Subscription> getSubscription(Echoarea area) {
		return ORMManager.get(Subscription.class).getAnd("echoarea_id", "=",
				area);
	}

	private List<FilemailAwaiting> getFileMail(Link link) {
		if (link.getId() != null) {
			return ORMManager.get(FilemailAwaiting.class).getAnd("link_id",
					"=", link);
		} else {
			return new ArrayList<>();
		}
	}

	private List<FileSubscription> getSubscription(Filearea area) {
		return ORMManager.get(FileSubscription.class).getAnd("filearea_id",
				"=", area);
	}
}
