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

import com.j256.ormlite.dao.GenericRawResults;

import jnode.dto.Echoarea;
import jnode.dto.Link;
import jnode.dto.LinkOption;
import jnode.dto.Netmail;
import jnode.dto.Rewrite;
import jnode.dto.Robot;
import jnode.dto.Route;
import jnode.dto.Subscription;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
import jnode.logger.Logger;
import jnode.main.Main;
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
	private final static DateFormat format = new SimpleDateFormat(
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
	 * Опции для линков
	 * 
	 * @param link
	 * @param option
	 * @return
	 */
	private static String getOption(Link link, String option) {
		String value = "";
		try {
			GenericRawResults<String[]> res = ORMManager.INSTANSE
					.linkoption()
					.queryRaw(
							String.format(
									"SELECT value FROM linkoptions WHERE link_id=%d AND name='%s'",
									link.getId(), option.toLowerCase()));
			String[] q = res.getFirstResult();
			if (q != null) {
				value = q[0];
			}
		} catch (SQLException e) {
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
	public static FtnPkt[] unpack(Message message) {
		ArrayList<FtnPkt> unzipped = new ArrayList<FtnPkt>();
		String filename = message.getMessageName().toLowerCase();
		if (filename.matches("^[a-f0-9]{8}\\.pkt$")) {
			FtnPkt pkt = new FtnPkt();
			pkt.unpack(message.getInputStream());
			unzipped.add(pkt);
		} else if (filename.matches("^[a-f0-9]{8}\\.[a-z0-9][a-z0-9][a-z0-9]$")) {
			try {
				ZipInputStream zis = new ZipInputStream(
						message.getInputStream());
				while (zis.getNextEntry() != null) {
					FtnPkt pkt = new FtnPkt();
					pkt.unpack(zis, false);
					unzipped.add(pkt);
				}
			} catch (IOException e) {
				logger.error("Не удалось распаковать " + filename);
			}
		} else {
			filename = filename.replaceAll("^[\\./\\\\]+", "_");
			File file = new File(Main.getInbound() + File.separator + filename);
			try {
				FileOutputStream fos = new FileOutputStream(file);
				while (message.getInputStream().available() > 0) {
					byte[] block = new byte[1024];
					int len = message.getInputStream().read(block);
					fos.write(block, 0, len);
				}
				fos.close();
				logger.info("Получен файл " + file.getAbsolutePath() + " ("
						+ file.length() + ")");
			} catch (IOException e) {
				logger.error("Не удалось записать файл " + filename + ": "
						+ e.getMessage());
			}
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
				logger.debug("Проверяем " + check[i] + " на соответствие "
						+ regexp[i]);
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
				logger.debug("Проверяем " + check[i] + " на соответствие "
						+ regexp[i]);
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
					logger.debug("Перезаписываем fromAddr на " + fields[i]);
					break;
				case 1:
					message.setToAddr(new FtnAddress(fields[i]));
					logger.debug("Перезаписываем toAddr на " + fields[i]);
					break;
				case 2:
					message.setFromName(fields[i]);
					logger.debug("Перезаписываем fromAddr на " + fields[i]);
					break;
				case 3:
					message.setToName(fields[i]);
					logger.debug("Перезаписываем fromAddr на " + fields[i]);
					break;
				case 4:
					message.setSubject(fields[i]);
					logger.debug("Перезаписываем fromAddr на " + fields[i]);
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
					Robot robot = ORMManager.INSTANSE.robot().queryForId(
							message.getToName().toLowerCase());
					if (robot != null) {
						robotname = robot.getRobot();
						isRobot = true;
						Class<?> clazz = Class.forName(robot.getClassName());
						IRobot irobot = (IRobot) clazz.newInstance();
						logger.debug("Сообщение " + message.getMsgid()
								+ " передано роботу " + robotname);
						irobot.execute(message);
					}
				} catch (SQLException e) {
					logger.error("Ошибка при получении робота");
				} catch (ClassNotFoundException e) {
					logger.error("Ошибка при инициализации робота " + robotname);
					e.printStackTrace();
				} catch (Exception e) {
					logger.error("Ошибка при обработке сообщения робота "
							+ robotname);
					e.printStackTrace();
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
		{
			try {
				List<Link> lnk = ORMManager.INSTANSE.link().queryForEq(
						"ftn_address", routeTo.toString());
				if (lnk.isEmpty()) {
					if (routeTo.getPoint() > 0) {
						routeTo.setPoint(0);
						lnk = ORMManager.INSTANSE.link().queryForEq(
								"ftn_address", routeTo.toString());
						if (!lnk.isEmpty()) {
							routeVia = lnk.get(0);
						}
					}
				} else {
					routeVia = lnk.get(0);
				}
			} catch (SQLException e) {
				logger.error("Ошибка при поиска routeVia", e);
			}
		}
		if (routeVia == null) {
			try {
				List<Route> routes = ORMManager.INSTANSE.route().queryBuilder()
						.orderBy("nice", true).query();
				for (Route route : routes) {
					if (completeMask(route, message)) {
						routeVia = route.getRouteVia();
						break;
					}
				}
			} catch (SQLException e) {
				logger.error("Ошибка при получении роутинга", e);
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
	public static void writeReply(FtnMessage fmsg, String subject, String text)
			throws SQLException {

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
		sb.append(fmsg.getText().replaceAll("\001", "@"));
		sb.append("========== Original message ==========\n\n--- "
				+ Main.info.getVersion() + "\n");
		netmail.setText(sb.toString());
		FtnMessage ret = new FtnMessage();
		ret.setFromAddr(new FtnAddress(Main.info.getAddress().toString()));
		ret.setToAddr(fmsg.getFromAddr());
		Link routeVia = getRouting(ret);
		if (routeVia == null) {
			logger.error("Не могу найти роутинг для ответа на сообщение"
					+ fmsg.getMsgid());
			return;
		}
		netmail.setRouteVia(routeVia);
		ORMManager.INSTANSE.netmail().create(netmail);
		logger.debug("Создан Netmail #" + netmail.getId());
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
	 * Получаем подписчиков
	 * 
	 * @param area
	 * @param link
	 * @return
	 */
	public static List<Link> getSubscribers(Echoarea area, Link link) {
		List<Link> links = new ArrayList<Link>();
		try {
			List<Subscription> subs = ORMManager.INSTANSE.subscription()
					.queryForEq("echoarea_id", area);
			for (Subscription s : subs) {
				if (!s.getLink().equals(link))
					links.add(ORMManager.INSTANSE.link().queryForSameId(
							s.getLink()));
			}
		} catch (Exception e) {
			logger.warn("Не могу получить список подписчиков эхи "
					+ area.getName());
		}
		return links;
	}

	public static List<Link> getSubscribers(Echoarea area) {
		return getSubscribers(area, null);
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
		unpack(message);
	}

}
