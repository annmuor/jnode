package jnode.ftn.tosser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipFile;

import jnode.dto.Dupe;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.EchomailAwaiting;
import jnode.dto.FileSubscription;
import jnode.dto.Filearea;
import jnode.dto.Filemail;
import jnode.dto.FilemailAwaiting;
import jnode.dto.FileForLink;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Netmail;
import jnode.dto.Subscription;
import jnode.event.NewEchomailEvent;
import jnode.event.NewFilemailEvent;
import jnode.event.NewNetmailEvent;
import jnode.event.Notifier;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
import jnode.ftn.types.FtnTIC;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.threads.PollQueue;
import jnode.orm.ORMManager;
import jnode.protocol.io.Message;
import static jnode.ftn.FtnTools.*;

/**
 * 
 * @author kreon
 * 
 */
public class FtnTosser {
	private static final String FILEECHO_ENABLE = "fileecho.enable";
	private static final String FILEECHO_PATH = "fileecho.path";
	private static final Logger logger = Logger.getLogger(FtnTosser.class);
	private final Map<String, Integer> tossed = new HashMap<String, Integer>();
	private final Map<String, Integer> bad = new HashMap<String, Integer>();
	private final Set<Link> pollLinks = new HashSet<Link>();

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
		boolean drop = validateNetmail(netmail);

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
			ORMManager.INSTANSE.getNetmailDAO().save(dbnm);
			Notifier.INSTANSE.notify(new NewNetmailEvent(dbnm));
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
				if (getOptionBooleanDefTrue(routeVia,
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
		Echoarea area = getAreaByName(echomail.getArea(), link);
		if (area == null) {
			logger.l3("Echoarea " + echomail.getArea()
					+ " is not avalible for " + link.getLinkAddress());
			Integer n = bad.get(echomail.getArea());
			bad.put(echomail.getArea(), (n == null) ? 1 : n + 1);
			return;
		}

        // попадаются злобные сообщения без MSGID

        if (echomail.getMsgid() == null){
            logger.l3("echomai " + echomail + " has null msgid");
        } else {
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
		ORMManager.INSTANSE.getEchomailDAO().save(mail);
		for (Subscription sub : ORMManager.INSTANSE.getSubscriptionDAO()
				.getAnd("echoarea_id", "=", area)) {
			if (link == null || !sub.getLink().getId().equals(link.getId())) {
				ORMManager.INSTANSE.getEchomailAwaitingDAO().save(
						new EchomailAwaiting(sub.getLink(), mail));
				pollLinks.add(sub.getLink());
			}
		}
		Notifier.INSTANSE.notify(new NewEchomailEvent(mail));
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
     * @param message сообщение
     */
	public int tossIncoming(Message message) {
		if (message == null) {
			return 0;
		}

		try {
			unpack(message);
			tossInboundDirectory();
		} catch (IOException e) {
			logger.l1(
					"Exception file tossing message "
							+ message.getMessageName(), e);
			return 1;
		}
		return 0;
	}

	/**
	 * Разбор файлов в папке inbound
	 */
	public void tossInboundDirectory() {
		synchronized (FtnTosser.class) {
			Set<Link> poll = new HashSet<Link>();
			File inbound = new File(getInbound());
			for (File file : inbound.listFiles()) {
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
						logger.l3("Tossing failed " + file.getAbsolutePath(), e);
					}
				} else if (loname.matches("(s|u)inb\\d*.pkt")) {
					try {
						Message m = new Message(file);
						logger.l4("Tossing file " + file.getAbsolutePath());
						FtnPkt pkt = new FtnPkt();
						pkt.unpack(m.getInputStream());
						Link link = ORMManager.INSTANSE.getLinkDAO()
								.getFirstAnd("ftn_address", "=",
										pkt.getFromAddr().toString());
						boolean secure = loname.charAt(0) == 's';
						if (secure) {
							if (!getOptionBooleanDefFalse(link,
									LinkOption.BOOLEAN_IGNORE_PKTPWD)) {
								if (!link.getPaketPassword().equalsIgnoreCase(
										pkt.getPassword())) {
									logger.l2("Pkt password mismatch - package moved to inbound");
									moveToBad(pkt);
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
						logger.l3("Tossing failed " + file.getAbsolutePath(), e);
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
						File attach = new File(getInbound() + File.separator
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
								attach = new File(getInbound() + File.separator
										+ filename);
							}
						}

						if (attach.canRead()) { // processing
							logger.l3("File found as " + filename);
							if (!MainHandler.getCurrentInstance().getInfo()
									.getAddressList().contains(tic.getTo())) {
								file.delete();
								logger.l3("Tic " + file.getName()
										+ " is not for us");
								continue;
							}
							Link source = ORMManager.INSTANSE.getLinkDAO()
									.getFirstAnd("ftn_address", "=",
											tic.getFrom().toString());
							if (source == null) {
								logger.l3("Link " + tic.getFrom()
										+ " not found");
								file.renameTo(new File(getInbound()
										+ File.separator + "bad_"
										+ generateTic()));
								continue;
							}
							Filearea area = getFileareaByName(tic.getArea()
									.toLowerCase(), source);
							if (area == null) {
								logger.l3("Filearea " + tic.getArea()
										+ " is not avalible for "
										+ source.getLinkAddress());
								file.renameTo(new File(getInbound()
										+ File.separator + "bad_"
										+ generateTic()));
								continue;
							}
							new File(getFileechoPath() + File.separator
									+ area.getName()).mkdir();
							Filemail mail = new Filemail();
							if (attach.renameTo(new File(getFileechoPath()
									+ File.separator + area.getName()
									+ File.separator + tic.getFile()))) {
								mail.setFilepath(getFileechoPath()
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
							mail.setSeenby(write4D(tic.getSeenby()));
							mail.setCreated(new Date());
							ORMManager.INSTANSE.getFilemailDAO().save(mail);
							for (FileSubscription sub : ORMManager.INSTANSE
									.getFileSubscriptionDAO().getAnd(
											"filearea_id", "=", area,
											"link_id", "!=", source)) {

								ORMManager.INSTANSE.getFilemailAwaitingDAO()
										.save(new FilemailAwaiting(sub
												.getLink(), mail));
								if (getOptionBooleanDefFalse(sub.getLink(),
										LinkOption.BOOLEAN_CRASH_FILEMAIL)) {
									poll.add(sub.getLink());
								}
							}
							Notifier.INSTANSE.notify(new NewFilemailEvent(mail));
						} else {
							logger.l3("File " + tic.getFile()
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
						Link l = ORMManager.INSTANSE.getLinkDAO().getFirstAnd(
								"ftn_address", "=", address.toString());
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
											ORMManager.INSTANSE
													.getFileForLinkDAO().save(
															ffl);
										} else {
											logger.l2("File from ?lo not exists: "
													+ name);
										}
									}
								}
								br.close();
							} catch (Exception e) {
								logger.l2("Unable to read .?lo for files list",
										e);
							}
							poll.add(l);
						}
					} catch (NumberFormatException e) {
						logger.l3("?LO file " + loname + " is invalid");
					}
					file.delete();

				}
			}
			for (Link l : poll) {
				PollQueue.INSTANSE.add(ORMManager.INSTANSE.getLinkDAO()
						.getById(l.getId()));
			}
		}
	}

	public String getFileechoPath() {
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
		Ftn2D link2d = new Ftn2D(link_address.getNet(), link_address.getNode());
		List<FtnMessage> messages = new ArrayList<FtnMessage>();
		List<File> attachedFiles = new ArrayList<File>();
		List<FtnTIC> tics = new ArrayList<FtnTIC>();
		List<Message> ret = new ArrayList<Message>();
		try {
			List<Netmail> netmails = ORMManager.INSTANSE.getNetmailDAO()
					.getAnd("send", "=", false, "route_via", "=", link);

			if (!netmails.isEmpty()) {
				for (Netmail netmail : netmails) {
					FtnMessage msg = netmailToFtnMessage(netmail);
					messages.add(msg);
					logger.l4(String.format(
							"Pack netmail #%d %s -> %s for %s flags %d",
							netmail.getId(), netmail.getFromFTN(),
							netmail.getToFTN(), link.getLinkAddress(),
							msg.getAttribute()));
					if ((netmail.getAttr() & FtnMessage.ATTR_FILEATT) > 0) {
						String filename = netmail.getSubject();
						filename = filename.replaceAll("^[\\./\\\\]+", "_");
						File file = new File(getInbound() + File.separator
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

                if (mail == null){
                    // консистентность базы, констрейнты? Нет, не слышал
                    logger.l2(MessageFormat.format("Error: not found echomail for awaiting mail {0}", ema));
                    continue;
                }

				Echoarea area = mail.getArea();
				toRemove.add(mail);
				Set<Ftn2D> seenby = new HashSet<Ftn2D>(read2D(mail.getSeenBy()));
				if (seenby.contains(link2d) && link_address.getPoint() == 0) {
					logger.l5(link2d + " is in seenby for " + link_address);
					continue;
				}
				List<Ftn2D> path = read2D(mail.getPath());
				for (FtnAddress address : MainHandler.getCurrentInstance()
						.getInfo().getAddressList()) {
					Ftn2D me = new Ftn2D(address.getNet(), address.getNode());
					seenby.add(me);
					if (!path.contains(me)) {
						path.add(me);
					}
				}
				seenby.add(link2d);

				List<Subscription> ssubs = ORMManager.INSTANSE
						.getSubscriptionDAO().getAnd("echoarea_id", "=", area);
				for (Subscription ssub : ssubs) {

                    try
                    {
                        Link _sslink = ORMManager.INSTANSE.getLinkDAO().getById(
                                ssub.getLink().getId());
                        FtnAddress addr = new FtnAddress(_sslink.getLinkAddress());
                        Ftn2D d2 = new Ftn2D(addr.getNet(), addr.getNode());
                        seenby.add(d2);
                    } catch(NullPointerException e){
                        logger.l1("Bad link for subscriprion " + ssub + " : ignored", e);
                    }

				}

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
						read4D(mail.getSeenby()));
				if (seenby.contains(link_address)) {
					logger.l3("This file have a seen-by for link");
					continue;
				}
				FtnTIC tic = new FtnTIC();
				try {
					CRC32 crc32 = new CRC32();
					FileInputStream fis = new FileInputStream(f);
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
				} catch (IOException e) {
                    logger.l2("fail process tic", e);
				}
				tic.setArea(area.getName().toUpperCase());
				tic.setAreaDesc(area.getDescription());
				tic.setFile(mail.getFilename());
				tic.setSize(f.length());
				tic.setDesc(mail.getFiledesc());
				tic.setPassword(link.getPaketPassword());
				tic.setFrom(getPrimaryFtnAddress());
				tic.setTo(link_address);
				tic.setOrigin(new FtnAddress(mail.getOrigin()));
				for (FtnAddress address : MainHandler.getCurrentInstance()
						.getInfo().getAddressList()) {
					seenby.add(address);
				}
				for (FileSubscription sub : ORMManager.INSTANSE
						.getFileSubscriptionDAO().getAnd("filearea_id", "=",
								area)) {
                    try
                    {
                        Link l = ORMManager.INSTANSE.getLinkDAO().getById(
                                sub.getLink().getId());
                        seenby.add(new FtnAddress(l.getLinkAddress()));
                    } catch (NullPointerException e){
                        logger.l1("bad link for FileSubscription " + sub + " - ignored", e);
                    }
				}
				List<FtnAddress> sb = new ArrayList<FtnAddress>(seenby);
				Collections.sort(sb, new Ftn4DComparator());
				tic.setSeenby(sb);
				tic.setPath(mail.getPath() + "Path " + getPrimaryFtnAddress()
						+ " " + System.currentTimeMillis() / 1000 + " "
						+ FORMAT.format(new Date()) + " "
						+ MainHandler.getVersion() + "\r\n");
				tic.setRealpath(mail.getFilepath());
				tics.add(tic);
			}
			if (!toRemove.isEmpty()) {
				ORMManager.INSTANSE.getFilemailAwaitingDAO().delete("link_id",
						"=", link, "filemail_id", "in", toRemove);
			}
		}
		// files for link
		{
			List<FileForLink> ffls = ORMManager.INSTANSE.getFileForLinkDAO()
					.getAnd("link_id", "eq", link);
			for (FileForLink ffl : ffls) {
				try {
					File file = new File(ffl.getFilename());
					Message m = new Message(file);
					ORMManager.INSTANSE.getFileForLinkDAO().delete("link_id",
							"=", link, "filename", "=", ffl.getFilename());
					ret.add(m);
				} catch (Exception ex) {
                     logger.l1(MessageFormat.format("Exception during get file {0} for link {1}", ffl, link), ex);
				}
			}
		}
		if (!messages.isEmpty()) {
			pack(messages, link);
		}
		// scan inbound
		synchronized (FtnTosser.class) {
			File inbound = new File(getInbound());
			for (File file : inbound.listFiles()) {
				String loname = file.getName().toLowerCase();
				if (loname.matches("^out_" + link.getId() + "\\..*$")) {
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
						ret.add(m);
					} catch (Exception e) {
						// ignore
					}

				}
			}

		}
		for (FtnTIC tic : tics) {
			try {
				byte[] data = tic.pack();
				Message message = new Message(generateTic(), data.length);
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
