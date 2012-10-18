package jnode.ftn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.EchomailAwaiting;
import jnode.dto.FileSubscription;
import jnode.dto.Filearea;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Netmail;
import jnode.dto.Rewrite;
import jnode.dto.Robot;
import jnode.dto.Route;
import jnode.dto.Subscription;
import jnode.event.NewEchoareaEvent;
import jnode.event.NewFileareaEvent;
import jnode.event.Notifier;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.main.threads.PollQueue;
import jnode.ndl.FtnNdlAddress;
import jnode.ndl.NodelistScanner;
import jnode.ndl.FtnNdlAddress.Status;
import jnode.orm.ORMManager;
import jnode.protocol.io.Message;
import jnode.robot.IRobot;

/**
 * Сборник всякой хрени
 * 
 * @author kreon
 * 
 */
public final class FtnTools {
	private final static String SEEN_BY = "SEEN-BY:";
	private final static String PATH = "\001PATH:";
	public static Charset cp866 = Charset.forName("CP866");
	private final static String ROUTE_VIA = "\001Via %s "
			+ Main.info.getVersion() + " %s";
	public final static DateFormat format = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
	private static final Logger logger = Logger.getLogger(FtnTools.class);

	/**
	 * Сортировщик 2D-адресов
	 * 
	 * @author kreon
	 * 
	 */
	public static class Ftn2DComparator implements Comparator<Ftn2D> {

		@Override
		public int compare(Ftn2D o1, Ftn2D o2) {
			if (o1.getNet() == o2.getNet()) {
				return o1.getNode() - o2.getNode();
			} else {
				return o1.getNet() - o2.getNet();
			}
		}
	}

	/**
	 * Сортировщик 4D-адресов
	 * 
	 * @author kreon
	 * 
	 */
	public static class Ftn4DComparator implements Comparator<FtnAddress> {

		@Override
		public int compare(FtnAddress o1, FtnAddress o2) {
			if (o1.getZone() == o2.getZone()) {
				if (o1.getNet() == o2.getNet()) {
					if (o1.getNode() == o2.getNode()) {
						if (o1.getPoint() == o2.getPoint()) {
							return 0;
						} else {
							return o1.getPoint() - o2.getPoint();
						}
					} else {
						return o1.getNode() - o2.getNode();
					}
				} else {
					return o1.getNet() - o2.getNet();
				}
			} else {
				return o1.getZone() - o2.getZone();
			}
		}
	}

	/**
	 * Генерация 8d-рандома
	 * 
	 * @return
	 */
	public static String generate8d() {
		byte[] digest = new byte[4];
		for (int i = 0; i < 4; i++) {
			long a = Math.round(Integer.MAX_VALUE * Math.random());
			long b = Math.round(Integer.MIN_VALUE * Math.random());
			long c = a ^ b;
			byte d = (byte) ((c >> 12) & 0xff);
			digest[i] = d;
		}
		return String.format("%02x%02x%02x%02x", digest[0], digest[1],
				digest[2], digest[3]);
	}

	public static String generateTic() {
		char[] chars = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'h', 'i',
				'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
				'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
				'7', '8', '9' };
		StringBuilder sb = new StringBuilder(11);
		sb.append("jt");
		for (int i = 0; i < 6; i++) {
			sb.append(chars[(int) Math.round((chars.length - 1) * Math.random())]);
		}
		sb.append(".tic");
		return sb.toString();
	}

	/**
	 * Big-Endian -> Little-Endian
	 * 
	 * @param v
	 * @return
	 */
	public static short revShort(short v) {
		return (short) ((short) ((short) (v >> 8) & 0xff) | (short) (v << 8));
	}

	/**
	 * Подстрока в виде байтов и в cp866
	 * 
	 * @param s
	 * @param len
	 * @return
	 */
	public static byte[] substr(String s, int len) {
		byte[] bytes = s.getBytes(cp866);

		if (bytes.length > len) {
			return ByteBuffer.wrap(bytes, 0, len).array();
		} else {
			return bytes;
		}
	}

	/**
	 * Читаем пакет пока не встретим \0
	 * 
	 * @param is
	 * @return
	 */
	public static String readUntillNull(InputStream is) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1);
		int b;
		try {
			while ((b = is.read()) != 0) {
				bos.write(b);
			}
		} catch (IOException e) {
			//
		}
		return new String(bos.toByteArray(), cp866);
	}

	/**
	 * Превращает строки синбаев в лист 2D адресов
	 * 
	 * @param seenByLines
	 * @return
	 */
	public static List<Ftn2D> readSeenBy(String seenByLines) {
		List<Ftn2D> seen = new ArrayList<Ftn2D>();
		String[] seenBy = seenByLines.split("[ \n]");
		int net = 0;
		for (String parts : seenBy) {
			if (parts == null || parts.length() < 1 || parts.equals(SEEN_BY)) {
				continue;
			} else {
				String[] part = parts.split("/");
				int node;
				if (part.length == 2) {
					net = Integer.valueOf(part[0]);
					node = Integer.valueOf(part[1]);
				} else {
					node = Integer.valueOf(part[0]);
				}
				seen.add(new Ftn2D(net, node));
			}
		}
		return seen;
	}

	/**
	 * Превращает лист синбаев в строку для добавления в письмо
	 * 
	 * @param seenby
	 * @return
	 */
	public static String writeSeenBy(List<Ftn2D> seenby) {
		StringBuilder ret = new StringBuilder();
		Collections.sort(seenby, new Ftn2DComparator());
		int net = 0;
		int linelen = 0;
		for (Ftn2D ftn : seenby) {
			if (linelen >= 72) {
				linelen = 0;
				net = 0;
				ret.append("\n");
			}
			if (linelen == 0) {
				ret.append(SEEN_BY);
				linelen += SEEN_BY.length();
			}
			if (net != ftn.getNet()) {
				net = ftn.getNet();
				String app = String.format(" %d/%d", ftn.getNet(),
						ftn.getNode());
				ret.append(app);
				linelen += app.length();
			} else {
				String app = String.format(" %d", ftn.getNode());
				ret.append(app);
				linelen += app.length();
			}
		}
		if (ret.charAt(ret.length() - 1) != '\n') {
			ret.append('\n');
		}
		return ret.toString();
	}

	/**
	 * Превращает путь в List
	 * 
	 * @param seenByLines
	 * @return
	 */
	public static List<Ftn2D> readPath(String seenByLines) {
		List<Ftn2D> seen = new ArrayList<Ftn2D>();
		String[] seenBy = seenByLines.split("[ \n]");
		int net = 0;
		for (String parts : seenBy) {
			if (parts == null || parts.length() < 1 || parts.equals(PATH)) {
				continue;
			} else {
				String[] part = parts.split("/");
				int node;
				if (part.length == 2) {
					net = Integer.valueOf(part[0]);
					node = Integer.valueOf(part[1]);
				} else {
					node = Integer.valueOf(part[0]);
				}
				seen.add(new Ftn2D(net, node));
			}
		}
		return seen;
	}

	/**
	 * Превращает List в путь
	 * 
	 * @param path
	 * @return
	 */
	public static String writePath(List<Ftn2D> path) {
		StringBuilder ret = new StringBuilder();
		int net = 0;
		int linelen = 0;
		for (Ftn2D ftn : path) {
			if (linelen >= 72) {
				linelen = 0;
				net = 0;
				ret.append("\n");
			}
			if (linelen == 0) {
				ret.append(PATH);
				linelen += PATH.length();
			}
			if (net != ftn.getNet()) {
				net = ftn.getNet();
				String app = String.format(" %d/%d", ftn.getNet(),
						ftn.getNode());
				ret.append(app);
				linelen += app.length();
			} else {
				String app = String.format(" %d", ftn.getNode());
				ret.append(app);
				linelen += app.length();
			}
		}
		if (ret.charAt(ret.length() - 1) != '\n') {
			ret.append('\n');
		}
		return ret.toString();
	}

	/**
	 * Читаем 2d-адреса через разделитель
	 * 
	 * @param list2d
	 * @return
	 */
	public static List<Ftn2D> read2D(String list2d) {
		List<Ftn2D> ret = new ArrayList<Ftn2D>();
		for (String l2d : list2d.split(" ")) {
			String[] part = l2d.split("/");
			try {
				ret.add(new Ftn2D(Integer.valueOf(part[0]), Integer
						.valueOf(part[1])));
			} catch (RuntimeException e) {

			}
		}
		return ret;
	}

	/**
	 * Читаем 4d-адреса через разделитель
	 * 
	 * @param list2d
	 * @return
	 */
	public static List<FtnAddress> read4D(String list2d) {
		List<FtnAddress> ret = new ArrayList<FtnAddress>();
		for (String l2d : list2d.split(" ")) {
			try {
				ret.add(new FtnAddress(l2d));
			} catch (RuntimeException e) {

			}
		}
		return ret;
	}

	/**
	 * Пишем 2d-адреса через разделитель
	 * 
	 * @param list
	 * @param sort
	 * @return
	 */
	public static String write2D(List<Ftn2D> list, boolean sort) {
		StringBuilder ret = new StringBuilder();
		if (sort) {
			Collections.sort(list, new Ftn2DComparator());
		}
		boolean flag = false;
		for (Ftn2D d : list) {
			if (flag) {
				ret.append(" ");
			} else {
				flag = true;
			}
			ret.append(String.format("%d/%d", d.getNet(), d.getNode()));
		}
		return ret.toString();
	}

	/**
	 * Пишем 4d-адреса через разделитель
	 * 
	 * @param list
	 * @param sort
	 * @return
	 */
	public static String write4D(List<FtnAddress> list) {
		StringBuilder ret = new StringBuilder();

		boolean flag = false;
		for (FtnAddress d : list) {
			if (flag) {
				ret.append(" ");
			} else {
				flag = true;
			}
			ret.append(d.toString());
		}
		return ret.toString();
	}

	/**
	 * Опции для линков
	 * 
	 * @param link
	 * @param option
	 * @return
	 */
	private static String getOption(Link link, String option) {
		String value = "";
		LinkOption opt = ORMManager.INSTANSE.getLinkOptionDAO().getFirstAnd(
				"link_id", "=", link, "name", "=", option.toLowerCase());
		if (opt != null) {
			value = opt.getValue();
		}
		return value;
	}

	public static String getOptionString(Link link, String option) {
		return getOption(link, option);
	}

	public static boolean getOptionBooleanDefFalse(Link link, String option) {
		String s = getOption(link, option);
		if (s.equalsIgnoreCase("TRUE") || s.equalsIgnoreCase("ON")) {
			return true;
		}
		return false;
	}

	public static boolean getOptionBooleanDefTrue(Link link, String option) {
		String s = getOption(link, option);
		if (s.equalsIgnoreCase("FALSE") || s.equalsIgnoreCase("OFF")) {
			return false;
		}
		return true;
	}

	public static long getOptionLong(Link link, String option) {
		String s = getOption(link, option);
		long ret = 0;
		try {
			ret = Long.valueOf(s);
		} catch (NumberFormatException ignore) {
		}
		return ret;
	}

	public static String[] getOptionStringArray(Link link, String option) {
		String s = getOption(link, option);
		return s.split(" ");
	}

	/**
	 * Конвертер
	 * 
	 * @param mail
	 * @return
	 */
	public static FtnMessage netmailToFtnMessage(Netmail mail) {
		FtnMessage message = new FtnMessage();
		message.setNetmail(true);
		message.setFromName(mail.getFromName());
		message.setToName(mail.getToName());
		message.setFromAddr(new FtnAddress(mail.getFromFTN()));
		message.setToAddr(new FtnAddress(mail.getToFTN()));
		message.setDate(mail.getDate());
		message.setSubject(mail.getSubject());
		message.setAttribute(mail.getAttr());
		StringBuilder text = new StringBuilder();
		text.append(mail.getText());
		if (text.charAt(text.length() - 1) != '\n') {
			text.append('\n');
		}
		text.append(String.format(ROUTE_VIA, Main.info.getAddress().toString(),
				format.format(new Date())));
		message.setText(text.toString());
		return message;
	}

	/**
	 * Распаковка из зип-архива
	 * 
	 * @param message
	 * @return
	 */
	public static FtnPkt[] unpack(Message message) throws IOException {
		ArrayList<FtnPkt> unzipped = new ArrayList<FtnPkt>();
		String filename = message.getMessageName().toLowerCase();
		if (filename.matches("^[a-f0-9]{8}\\.pkt$")) {
			FtnPkt pkt = new FtnPkt();
			pkt.unpack(message.getInputStream());
			unzipped.add(pkt);
		} else if (filename.matches("^[a-f0-9]{8}\\.[a-z0-9][a-z0-9][a-z0-9]$")) {
			ZipInputStream zis = new ZipInputStream(message.getInputStream());
			while (zis.getNextEntry() != null) {
				FtnPkt pkt = new FtnPkt();
				pkt.unpack(zis, false);
				unzipped.add(pkt);
			}
		} else if (message.isSecure()) {
			filename = filename.replaceAll("^[\\./\\\\]+", "_");
			File file = new File(Main.getInbound() + File.separator + filename);
			FileOutputStream fos = new FileOutputStream(file);
			while (message.getInputStream().available() > 0) {
				byte[] block = new byte[1024];
				int len = message.getInputStream().read(block);
				fos.write(block, 0, len);
			}
			fos.close();
			logger.l3("File saved " + file.getAbsolutePath() + " ("
					+ file.length() + ")");
		} else {
			logger.l2("File rejected via unsecure " + filename);
		}
		return unzipped.toArray(new FtnPkt[0]);
	}

	/**
	 * Проверка соответствия маски :)
	 * 
	 * @param route
	 * @param message
	 * @return
	 */
	public static boolean completeMask(Route route, FtnMessage message) {
		boolean ok = true;
		String[] regexp = new String[] { route.getFromAddr(),
				route.getToAddr(), route.getFromName(), route.getToName(),
				route.getSubject() };
		String[] check = new String[] { message.getFromAddr().toString(),
				message.getToAddr().toString(), message.getFromName(),
				message.getToName(), message.getSubject() };
		for (int i = 0; i < 5; i++) {
			if (regexp[i] != null && !regexp[i].equals("*")) {
				logger.l5("Checks " + check[i] + " via regexp " + regexp[i]);
				if (check[i] == null || !check[i].matches(regexp[i])) {
					ok = false;
				}
			}
		}
		return ok;
	}

	/**
	 * Проверка соответствия маски :)
	 * 
	 * @param rewrite
	 * @param message
	 * @return
	 */
	public static boolean completeMask(Rewrite rewrite, FtnMessage message) {
		boolean ok = true;
		String[] regexp = new String[] { rewrite.getOrig_from_addr(),
				rewrite.getOrig_to_addr(), rewrite.getOrig_from_name(),
				rewrite.getOrig_to_name(), rewrite.getOrig_subject() };
		String[] check = new String[] { message.getFromAddr().toString(),
				message.getToAddr().toString(), message.getFromName(),
				message.getToName(), message.getSubject() };
		for (int i = 0; i < 5; i++) {
			if (regexp[i] != null && !regexp[i].equals("*")) {
				logger.l5("Checks " + check[i] + " via " + regexp[i]);
				if (check[i] == null || !check[i].matches(regexp[i])) {
					ok = false;
				}
			}
		}
		return ok;
	}

	/**
	 * Перезапись сообщения
	 * 
	 * @param rewrite
	 * @param message
	 * @return
	 */
	public static void rewrite(Rewrite rewrite, FtnMessage message) {
		String[] fields = new String[] { rewrite.getNew_from_addr(),
				rewrite.getNew_to_addr(), rewrite.getNew_from_name(),
				rewrite.getNew_to_name(), rewrite.getNew_subject() };
		for (int i = 0; i < 5; i++) {
			if (fields[i] != null && !fields[i].equals("*")) {
				switch (i) {
				case 0:
					FtnAddress nfa = new FtnAddress(fields[i]);
					Matcher msgid = Pattern
							.compile(
									"^\001MSGID: " + message.getFromAddr()
											+ " (\\S+)$", Pattern.MULTILINE)
							.matcher(message.getText());
					if (msgid.find()) {
						message.setText(msgid.replaceFirst("\001MSGID: " + nfa
								+ " $1"));
					}
					Matcher origin = Pattern.compile(
							"^ \\* Origin: (.*) \\(" + message.getFromAddr()
									+ "\\)$", Pattern.MULTILINE).matcher(
							message.getText());
					if (origin.find()) {
						message.setText(origin.replaceFirst(" * Origin: $1 ("
								+ nfa + ")"));
					}
					message.setFromAddr(nfa);
					logger.l5("Rewrite fromAddr to " + fields[i]);
					break;
				case 1:
					message.setToAddr(new FtnAddress(fields[i]));
					logger.l5("Rewrite toAddr to " + fields[i]);
					break;
				case 2:
					message.setFromName(fields[i]);
					logger.l5("Rewrite fromAddr to " + fields[i]);
					break;
				case 3:
					message.setToName(fields[i]);
					logger.l5("Rewrite fromAddr to " + fields[i]);
					break;
				case 4:
					message.setSubject(fields[i]);
					logger.l5("Rewrite fromAddr to " + fields[i]);
					break;
				}
			}
		}
	}

	/**
	 * Проверям сообщение на соответствие роботу
	 * 
	 * @param message
	 * @return
	 */
	public static boolean checkRobot(FtnMessage message) {
		boolean isRobot = false;
		String robotname = "";
		if (message.isNetmail()) {
			if (message.getToAddr().equals(Main.info.getAddress())) {
				try {
					Robot robot = ORMManager.INSTANSE.getRobotDAO().getById(
							message.getToName().toLowerCase());
					if (robot != null) {
						robotname = robot.getRobot();
						isRobot = true;
						Class<?> clazz = Class.forName(robot.getClassName());
						IRobot irobot = (IRobot) clazz.newInstance();
						logger.l4("Message " + message.getMsgid()
								+ " sent to robot " + robotname);
						irobot.execute(message);
					}
				} catch (SQLException e) {
					logger.l2("Robot exception (GET) ", e);
				} catch (ClassNotFoundException e) {
					logger.l2("Robot excception (INIT) " + robotname, e);
				} catch (Exception e) {
					logger.l2("Robot excception  " + robotname + " (MESSAGE) ",
							e);
				}
			}
		}
		return isRobot;
	}

	/**
	 * Получаем роутинг для нетмейла
	 * 
	 * @param message
	 * @return
	 */
	public static Link getRouting(FtnMessage message) {
		Link routeVia = null;
		FtnAddress routeTo = new FtnAddress(message.getToAddr().toString());
		routeVia = ORMManager.INSTANSE.getLinkDAO().getFirstAnd("ftn_address",
				"=", routeTo.toString());
		// не наш пойнт
		if (!routeTo.isPointOf(Main.info.getAddress())) {
			routeTo.setPoint(0);
			routeVia = ORMManager.INSTANSE.getLinkDAO().getFirstAnd(
					"ftn_address", "=", routeTo.toString());
			// а теперь - по роутингу
			if (routeVia == null) {
				List<Route> routes = ORMManager.INSTANSE.getRouteDAO()
						.getOrderAnd("nice", true);
				for (Route route : routes) {
					if (completeMask(route, message)) {
						routeVia = route.getRouteVia();
						break;
					}
				}
			}
		}
		return routeVia;
	}

	/**
	 * Пишем ответ на нетмейл
	 * 
	 * @param fmsg
	 * @param subject
	 * @param text
	 * @throws SQLException
	 */
	public static void writeReply(FtnMessage fmsg, String subject, String text) {

		Netmail netmail = new Netmail();
		netmail.setFromFTN(Main.info.getAddress().toString());
		netmail.setFromName(Main.info.getStationName());
		netmail.setToFTN(fmsg.getFromAddr().toString());
		netmail.setToName(fmsg.getFromName());
		netmail.setSubject(subject);
		netmail.setDate(new Date());
		StringBuilder sb = new StringBuilder();
		sb.append(String
				.format("\001REPLY: %s\n\001MSGID: %s %s\n\001PID: %s\n\001TID: %s\nHello, %s!\n\n",
						fmsg.getMsgid(), Main.info.getAddress().toString(),
						generate8d(), Main.info.getVersion(),
						Main.info.getVersion(), netmail.getToName()));
		sb.append(text);
		sb.append("\n\n========== Original message ==========\n");
		sb.append("From: " + fmsg.getFromName() + " (" + fmsg.getFromAddr()
				+ ")\n");
		sb.append("To: " + fmsg.getToName() + " (" + fmsg.getToAddr() + ")\n");
		sb.append("Date: " + fmsg.getDate() + "\n");
		sb.append("Subject: " + fmsg.getSubject() + "\n");
		if (fmsg.getText() != null) {
			sb.append(fmsg.getText().replaceAll("\001", "@")
					.replaceAll("---", "+++")
					.replaceAll(" \\* Origin:", " + Origin:"));
		}
		sb.append("========== Original message ==========\n\n--- "
				+ Main.info.getVersion() + "\n");
		netmail.setText(sb.toString());
		FtnMessage ret = new FtnMessage();
		ret.setFromAddr(new FtnAddress(Main.info.getAddress().toString()));
		ret.setToAddr(fmsg.getFromAddr());
		Link routeVia = getRouting(ret);
		if (routeVia == null) {
			logger.l2("Routing for reply not found" + fmsg.getMsgid());
			return;
		}
		netmail.setRouteVia(routeVia);
		ORMManager.INSTANSE.getNetmailDAO().save(netmail);
		logger.l4("Netmail #" + netmail.getId() + " created");
		if (FtnTools.getOptionBooleanDefTrue(routeVia,
				LinkOption.BOOLEAN_CRASH_NETMAIL)) {
			PollQueue.INSTANSE.add(routeVia);
		}

	}

	/**
	 * Паковка сообщений
	 * 
	 * @param messages
	 * @param to
	 * @param password
	 * @return
	 */
	public static List<Message> pack(List<FtnMessage> messages, Link link) {
		byte[] data;
		List<Message> packed = new ArrayList<Message>();
		FtnAddress to = new FtnAddress(link.getLinkAddress());
		String password = link.getPaketPassword();
		FtnPkt nopack = new FtnPkt(Main.info.getAddress(), to, password,
				new Date());
		FtnPkt pack = new FtnPkt(Main.info.getAddress(), to, password,
				new Date());
		boolean packNetmail = getOptionBooleanDefFalse(link,
				LinkOption.BOOLEAN_PACK_NETMAIL);
		boolean packEchomail = getOptionBooleanDefTrue(link,
				LinkOption.BOOLEAN_PACK_ECHOMAIL);
		for (FtnMessage message : messages) {
			if (message.isNetmail()) {
				if (packNetmail) {
					pack.getMessages().add(message);
				} else {
					nopack.getMessages().add(message);
				}
			} else {
				if (packEchomail) {
					pack.getMessages().add(message);
				} else {
					nopack.getMessages().add(message);
				}
			}
		}
		if (nopack.getMessages().size() > 0) {
			data = nopack.pack();
			Message net = new Message(String.format("%s.pkt", generate8d()),
					data.length);
			net.setInputStream(new ByteArrayInputStream(data));
			packed.add(net);
		}
		if (pack.getMessages().size() > 0) {
			data = pack.pack();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(out);
			zos.setMethod(ZipOutputStream.DEFLATED);
			ZipEntry ze = new ZipEntry(String.format("%s.pkt", generate8d()));
			ze.setMethod(ZipEntry.DEFLATED);
			ze.setSize(data.length);
			CRC32 crc32 = new CRC32();
			crc32.update(data);
			ze.setCrc(crc32.getValue());
			try {
				zos.putNextEntry(ze);
				zos.write(data);
				zos.close();
			} catch (IOException e) {
			}
			byte[] zip = out.toByteArray();
			Message message = new Message(
					String.format("%s.fr0", generate8d()), zip.length);
			message.setInputStream(new ByteArrayInputStream(zip));
			packed.add(message);
		}
		return packed;
	}

	/**
	 * Кривые пакеты - в инбаунд
	 * 
	 * @param pkt
	 */
	public static void moveToBad(FtnPkt pkt) {
		ByteArrayInputStream bis = new ByteArrayInputStream(pkt.pack());
		Message message = new Message(String.format("%s_%d.pkt", generate8d(),
				new Date().getTime() / 1000), bis.available());
		message.setInputStream(bis);
		try {
			unpack(message);
		} catch (IOException e) {
		}
	}

	/**
	 * Делаем реврайт
	 * 
	 * @param message
	 */
	public static void processRewrite(FtnMessage message) {
		List<Rewrite> rewrites = ORMManager.INSTANSE.getRewriteDAO()
				.getOrderAnd(
						"nice",
						true,
						"type",
						"=",
						(message.isNetmail()) ? (Rewrite.Type.NETMAIL)
								: (Rewrite.Type.ECHOMAIL));
		for (Rewrite rewrite : rewrites) {
			if (FtnTools.completeMask(rewrite, message)) {
				logger.l5(((message.isNetmail()) ? "NET" : "ECH")
						+ " - match found, rewriting " + message.getMsgid());
				rewrite(rewrite, message);
				if (rewrite.isLast()) {
					break;
				}
			}
		}
	}

	/**
	 * получение и аутокриейт
	 * 
	 * @param name
	 * @param link
	 * @return
	 */
	public static Echoarea getAreaByName(String name, Link link) {
		Echoarea ret = null;
		name = name.toLowerCase();
		ret = ORMManager.INSTANSE.getEchoareaDAO().getFirstAnd("name", "=",
				name);
		if (ret == null) {
			if (link == null
					|| getOptionBooleanDefFalse(link,
							LinkOption.BOOLEAN_AUTOCREATE_AREA)) {
				ret = new Echoarea();
				ret.setName(name);
				ret.setDescription("Autocreated echoarea");
				ret.setReadlevel(0L);
				ret.setWritelevel(0L);
				ret.setGroup("");
				logger.l3("Echoarea " + name.toUpperCase() + " created");
				ORMManager.INSTANSE.getEchoareaDAO().save(ret);
				if (link != null) {
					Subscription sub = new Subscription();
					sub.setArea(ret);
					sub.setLink(link);
					ORMManager.INSTANSE.getSubscriptionDAO().save(sub);
				}
				Notifier.INSTANSE.notify(new NewEchoareaEvent(name, link));
			}
		} else {
			if (link != null
					&& ORMManager.INSTANSE.getSubscriptionDAO().getFirstAnd(
							"echoarea_id", "=", ret.getId(), "link_id", "=",
							link.getId()) == null) {
				ret = null;
			}
		}
		return ret;
	}

	/**
	 * получение и аутокриейт
	 * 
	 * @param name
	 * @param link
	 * @return
	 */
	public static Filearea getFileareaByName(String name, Link link) {
		Filearea ret = null;
		name = name.toLowerCase();
		ret = ORMManager.INSTANSE.getFileareaDAO().getFirstAnd("name", "=",
				name);
		if (ret == null) {
			if (link == null
					|| getOptionBooleanDefFalse(link,
							LinkOption.BOOLEAN_AUTOCREATE_AREA)) {
				ret = new Filearea();
				ret.setName(name);
				ret.setDescription("Autocreated filearea");
				ret.setReadlevel(0L);
				ret.setWritelevel(0L);
				ret.setGroup("");
				logger.l3("Filearea " + name.toUpperCase() + " created");
				ORMManager.INSTANSE.getFileareaDAO().save(ret);
				if (link != null) {
					FileSubscription sub = new FileSubscription();
					sub.setArea(ret);
					sub.setLink(link);
					ORMManager.INSTANSE.getFileSubscriptionDAO().save(sub);
				}
				Notifier.INSTANSE.notify(new NewFileareaEvent(name, link));
			}
		} else {
			if (link != null
					&& ORMManager.INSTANSE.getFileSubscriptionDAO()
							.getFirstAnd("filearea_id", "=", ret.getId(),
									"link_id", "=", link.getId()) == null) {
				ret = null;
			}
		}
		return ret;
	}

	public static boolean isADupe(Echoarea area, String msgid) {
		if (ORMManager.INSTANSE.getDupeDAO().getFirstAnd("msgid", "=", msgid,
				"echoarea_id", "=", area) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Проверка на дроп нетмейла
	 * 
	 * @param netmail
	 * @param secure
	 * @return
	 */
	public static boolean isNetmailMustBeDropped(FtnMessage netmail,
			boolean secure) {
		boolean validFrom = false;
		boolean validTo = false;
		// к нам на узел
		if (netmail.getToAddr().isPointOf(Main.info.getAddress())) {
			validTo = true;
		} else if (ORMManager.INSTANSE.getLinkDAO().getFirstAnd("ftn_address",
				"=", netmail.getToAddr().toString()) != null) {
			validTo = true;
		} else {
			FtnNdlAddress to = NodelistScanner.getInstance().isExists(
					netmail.getToAddr());
			if (to == null) {
				FtnTools.writeReply(
						netmail,
						"Destination not found",
						"Sorry, but destination of your netmail is not found in nodelist\nMessage rejected");
				logger.l3(String.format(
						"Netmail %s -> %s reject ( dest not found )", netmail
								.getFromAddr().toString(), netmail.getToAddr()
								.toString()));

			} else if (to.getStatus().equals(Status.DOWN)) {
				FtnTools.writeReply(netmail, "Destination is DOWN",
						"Warning! Destination of your netmail is DOWN.\nMessage rejected");
				logger.l3(String.format(
						"Netmail %s -> %s reject ( dest is DOWN )", netmail
								.getFromAddr().toString(), netmail.getToAddr()
								.toString()));
			} else if (to.getStatus().equals(Status.HOLD)) {
				FtnTools.writeReply(netmail, "Destination is HOLD",
						"Warning! Destination of your netmail is HOLD");
				logger.l4(String.format(
						"Netmail %s -> %s warn ( dest is Hold )", netmail
								.getFromAddr().toString(), netmail.getToAddr()
								.toString()));
				validTo = true;
			} else {
				validTo = true;
			}
		}

		if (netmail.getFromAddr().isPointOf(Main.info.getAddress())) {
			validFrom = true;
		} else if (ORMManager.INSTANSE.getLinkDAO().getFirstAnd("ftn_address",
				"=", netmail.getFromAddr().toString()) != null) {
			validFrom = true;
		} else {
			FtnNdlAddress from = NodelistScanner.getInstance().isExists(
					netmail.getFromAddr());
			if (from == null) {
				logger.l3(String.format(
						"Netmail %s -> %s reject ( origin not found )", netmail
								.getFromAddr().toString(), netmail.getToAddr()
								.toString()));
			} else {
				validFrom = true;
			}
		}

		return !(validFrom && validTo);
	}

	public static void writeEchomail(Echoarea area, String subject, String text) {
		Echomail mail = new Echomail();
		mail.setFromFTN(Main.info.getAddress().toString());
		mail.setFromName(Main.info.getStationName());
		mail.setArea(area);
		mail.setDate(new Date());
		mail.setPath("");
		mail.setSeenBy("");
		mail.setToName("All");
		mail.setSubject(subject);
		StringBuilder b = new StringBuilder();
		b.append(String.format(
				"\001MSGID: %s %s\n\001PID: %s\n\001TID: %s\n\n", Main.info
						.getAddress().toString(), FtnTools.generate8d(),
				Main.info.getVersion(), Main.info.getVersion()));
		b.append(text);
		b.append("\n--- " + Main.info.getStationName() + "\n");
		b.append(" * Origin: " + Main.info.getVersion() + " ("
				+ Main.info.getAddress().toString() + ")\n");
		mail.setText(b.toString());
		ORMManager.INSTANSE.getEchomailDAO().save(mail);
		for (Subscription s : ORMManager.INSTANSE.getSubscriptionDAO().getAnd(
				"echoarea_id", "=", area)) {
			ORMManager.INSTANSE.getEchomailAwaitingDAO().save(
					new EchomailAwaiting(s.getLink(), mail));
		}
	}
}
