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

package jnode.ftn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import jnode.core.ConcurrentDateFormatAccess;
import jnode.core.FileUtils;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.EchomailAwaiting;
import jnode.dto.FileForLink;
import jnode.dto.FileSubscription;
import jnode.dto.Filearea;
import jnode.dto.Filemail;
import jnode.dto.FilemailAwaiting;
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
import jnode.ftn.tosser.FtnTosser;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.threads.PollQueue;
import jnode.main.threads.TosserQueue;
import jnode.ndl.FtnNdlAddress;
import jnode.ndl.FtnNdlAddress.Status;
import jnode.ndl.NodelistScanner;
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
	private static final String BINKP_INBOUND = "binkp.inbound";
	private static final String NETMAIL_VALID = "netmail.only_valid";
	private static final String SEEN_BY = "SEEN-BY:";
	private static final String PATH = "\001PATH:";
	public static final Charset CP_866 = Charset.forName("CP866");
	private static final String ROUTE_VIA = "\001Via %s "
			+ MainHandler.getVersion() + " %s";
	public static final ConcurrentDateFormatAccess FORMAT = new ConcurrentDateFormatAccess(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
	private static final Logger logger = Logger.getLogger(FtnTools.class);

	private static final Hashtable<String, IRobot> robotMaps = new Hashtable<>();

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
	public static byte[] substr(String s, int maxlen) {
		byte[] bytes = s.getBytes(CP_866);
		int len = (bytes.length > maxlen) ? maxlen : bytes.length;
		byte[] ret = new byte[len];
		for (int i = 0; i < len; i++) {
			ret[i] = bytes[i];
		}
		return ret;
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
		return new String(bos.toByteArray(), CP_866);
	}

	/**
	 * Превращает строки синбаев в лист 2D адресов
	 * 
	 * @param seenByLines
	 * @return
	 */
	public static List<Ftn2D> readSeenBy(String seenByLines) {
		List<Ftn2D> seen = new ArrayList<>();
		String[] seenBy = seenByLines.split("[ \n]");
		int net = 0;
		for (String parts : seenBy) {
			if (parts == null || parts.length() < 1 || parts.equals(SEEN_BY)) {
				continue;
			} else {

				try {
					String[] part = parts.split("/");
					int node;
					if (part.length == 2) {
						net = Integer.valueOf(part[0]);
						node = Integer.valueOf(part[1]);
					} else {
						node = Integer.valueOf(part[0]);
					}
					seen.add(new Ftn2D(net, node));
				} catch (NumberFormatException e) {
					logger.l2(MessageFormat.format(
							"Error: fail write seen {0} for lines {1}", parts,
							seenByLines), e);
				}

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
		logger.l5("WriteSeenBy: " + seenby);
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
		if (ret.length() == 0) {
			return "";
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
		List<Ftn2D> seen = new ArrayList<>();
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
		logger.l5("WritePath: " + path);
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
		if (ret.length() == 0) {
			return "";
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
		List<Ftn2D> ret = new ArrayList<>();

		if (list2d == null || list2d.length() == 0
				|| list2d.trim().length() == 0) {
			return ret;
		}

		String lastNet = null;

		for (String l2d : list2d.split(" ")) {
			String[] part = l2d.split("/");

			String net;
			String node;

			switch (part.length) {
			case 2:
				// 5020/841
				net = part[0];
				node = part[1];
				break;
			case 1:
				// 841
				net = lastNet;
				node = part[0];
				break;
			default:
				throw new IllegalArgumentException(MessageFormat.format(
						"fail parse 2d address [{0}] in list [{1}]", l2d,
						list2d));
			}

			try {
				ret.add(Ftn2D.fromString(net, node));
				lastNet = net;
			} catch (IllegalArgumentException e) {
				lastNet = null;
				logger.l1(
						MessageFormat
								.format("fail parse 2d address [{0}] in list [{1}] - illegal arguments",
										l2d, list2d), e);
			} catch (RuntimeException e2) {
				lastNet = null;
				logger.l1(
						MessageFormat
								.format("fail parse 2d address [{0}] in list [{1}] - unexpected error",
										l2d, list2d), e2);
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
		List<FtnAddress> ret = new ArrayList<>();
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
	public static String getOption(Link link, String option) {
		String value = "";
		if (link.getId() != null) {
			LinkOption opt = ORMManager.get(LinkOption.class).getFirstAnd(
					"link_id", "=", link, "name", "=", option.toLowerCase());
			if (opt != null) {
				value = opt.getValue();
			}
		}
		return value;
	}

	public static String getOptionString(Link link, String option) {
		return getOption(link, option);
	}

	public static boolean getOptionBooleanDefFalse(Link link, String option) {
		String s = getOption(link, option);
		return s.equalsIgnoreCase("TRUE") || s.equalsIgnoreCase("ON");
	}

	public static boolean getOptionBooleanDefTrue(Link link, String option) {
		String s = getOption(link, option);
		return !(s.equalsIgnoreCase("FALSE") || s.equalsIgnoreCase("OFF"));
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
		for (FtnAddress address : MainHandler.getCurrentInstance().getInfo()
				.getAddressList()) {
			text.append(String.format(ROUTE_VIA, address.toString(),
					FORMAT.format(new Date())));
		}
		message.setText(text.toString());
		return message;
	}

	/**
	 * Конвертер
	 * 
	 * @param mail
	 * @return
	 */
	public static Netmail ftnMessageToNetmail(FtnMessage mail) {
		Netmail netmail = new Netmail();
		netmail.setAttr(mail.getAttribute());
		netmail.setDate(mail.getDate());
		netmail.setFromFTN(mail.getFromAddr().toString());
		netmail.setFromName(mail.getFromName());
		netmail.setToFTN(mail.getToAddr().toString());
		netmail.setToName(mail.getToName());
		netmail.setSubject(mail.getSubject());
		netmail.setText(mail.getText());
		return netmail;
	}

	/**
	 * Распаковка из зип-архива
	 * 
	 * @param message
	 * @return
	 */
	public static void unpack(Message message) throws IOException {
		String filename = message.getMessageName().toLowerCase();
		if (filename.matches("^[a-f0-9]{8}\\.pkt$")) {
			File out = createInboundFile(message.isSecure());
			FileOutputStream fos = new FileOutputStream(out);
			int len = 0;
			do {
				byte[] buf = new byte[1024];
				len = message.getInputStream().read(buf);
				if (len > 0) {
					fos.write(buf, 0, len);
				}
			} while (len > 0);
			fos.close();
		} else if (filename
				.matches("^\\w{8}\\.(mo|tu|we|th|fr|sa|su)[0-9a-z]$")) {
			unpackBundle(message);
		} else if (message.isSecure()) {
			File f = guessFilename(filename, false);
			if (f != null) {
				FileOutputStream fos = new FileOutputStream(f);
				int len = 0;
				do {
					byte[] buf = new byte[1024];
					len = message.getInputStream().read(buf);
					if (len > 0) {
						fos.write(buf, 0, len);
					}
				} while (len > 0);
				fos.close();
				logger.l3("File saved " + f.getAbsolutePath() + " ("
						+ f.length() + ")");
			}
		} else {
			logger.l2("File rejected via unsecure " + filename);
		}
	}

	protected static void unpackBundle(Message message) throws IOException {
		logger.l4("Unpacking " + message.getMessageName());
		ZipInputStream zis = new ZipInputStream(message.getInputStream());
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			String name = ze.getName().toLowerCase();
			logger.l5("found " + name);
			int idx = name.lastIndexOf('/');
			if (idx >= 0) {
				name = name.substring(idx + 1);
			}
			idx = name.lastIndexOf('\\');
			if (idx >= 0) {
				name = name.substring(idx + 1);
			}
			if (name.matches("^[a-f0-9]{8}\\.pkt$")) {
				File out = createInboundFile(message.isSecure());
				FileOutputStream fos = new FileOutputStream(out);
				int len = 0;
				do {
					byte[] buf = new byte[1024];
					len = zis.read(buf);
					if (len > 0) {
						fos.write(buf, 0, len);
					}
				} while (len > 0);
				fos.close();
				logger.l4(name + " was written as " + out.getName());
			} else {
				logger.l3(name + " was deleted as unknown");
			}
		}
		zis.close();
	}

	public static File guessFilename(String filename, boolean read) {
		filename = filename.replaceAll("^[\\./\\\\]+", "_");
		File f = new File(getInbound() + File.separator + filename);
		boolean ninetoa = false;
		boolean ztonull = false;
		boolean underll = false;
		while ((read) ? !f.exists() : f.exists()) {
			if ((ninetoa && ztonull) || underll) {
				logger.l4(read ? "Files not found"
						: "Delete something to continue");
				f = null;
				break;
			} else {
				char[] array = filename.toCharArray();
				char c = array[array.length - 1];
				if ((c >= '0' && c <= '8') || (c >= 'a' && c <= 'y')) {
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
				f = new File(getInbound() + File.separator + filename);
			}
		}
		return f;
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
					if (message.getMsgid() != null) {
						Matcher msgid = Pattern.compile(
								"^" + message.getFromAddr() + " (\\S+)$")
								.matcher(message.getMsgid());
						if (msgid.find()) {
							String msg = msgid.replaceFirst(nfa + " $1");
							message.setText(message.getText().replace(
									message.getMsgid(), msg));
							message.setMsgid(msg);

						}
					} // TODO : netmail msgid
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
			if (MainHandler.getCurrentInstance().getInfo().getAddressList()
					.contains(message.getToAddr())) {
				// TODO: optiomize
				IRobot oRobot = robotMaps
						.get(message.getToName().toLowerCase());
				if (oRobot == null) {
					try {
						Robot robot = ORMManager.get(Robot.class).getById(
								message.getToName().toLowerCase());
						if (robot != null) {
							robotname = robot.getRobot();
							Class<?> clazz = Class
									.forName(robot.getClassName());
							oRobot = (IRobot) clazz.newInstance();
							robotMaps.put(robotname, oRobot);
							logger.l4("Message " + message.getMsgid()
									+ " sent to robot " + robotname);
						}
					} catch (ClassNotFoundException e) {
						logger.l2("Robot class not found (INIT) " + robotname,
								e);
					} catch (Exception e) {
						logger.l2(
								"Robot excception  " + robotname + " (INIT) ",
								e);
					}
				}
				if (oRobot != null) {
					isRobot = true;
					try {
						oRobot.execute(message);
					} catch (Exception e) {
						logger.l2("Robot excception  " + robotname
								+ " (PROCCESS) ", e);
					}
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
		Link routeVia;
		FtnAddress routeTo = message.getToAddr().clone();
		routeVia = getLinkByFtnAddress(routeTo);
		// check our point
		if (!isOurPoint(routeTo)) {
			routeTo.setPoint(0);
			routeVia = getLinkByFtnAddress(routeTo);
			// а теперь - по роутингу
			if (routeVia == null) {
				List<Route> routes = ORMManager.get(Route.class).getOrderAnd(
						"nice", true);
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

	public static Link getRoutingFallback(FtnMessage message,
			Link previousRouteVia) {
		Link routeVia = null;
		FtnAddress routeTo = message.getToAddr().clone();
		if (isOurPoint(routeTo)) {
			return null;
		}
		// direct link can be down for us - use cross way
		List<Route> routes = ORMManager.get(Route.class).getOrderAnd("nice",
				true);
		for (Route route : routes) {
			if (completeMask(route, message)) {
				if (route.getRouteVia().equals(previousRouteVia)) {
					continue;
				}
				routeVia = route.getRouteVia();
				break;
			}
		}
		if (routeVia == null) {
			routeVia = previousRouteVia;
		}
		return routeVia;
	}

	public static Link getLinkByFtnAddress(FtnAddress addr) {
		return ORMManager.get(Link.class).getFirstAnd("ftn_address", "=",
				addr.toString());
	}

	public static Link getLinkByFtnAddress(List<FtnAddress> addrs) {
		List<String> saddrs = new ArrayList<>();
		for (FtnAddress a : addrs) {
			saddrs.add(a.toString());
		}
		return ORMManager.get(Link.class).getFirstAnd("ftn_address", "in",
				saddrs);
	}

	public static boolean isOurPoint(FtnAddress routeTo) {
		boolean ourPoint = false;
		for (FtnAddress testAddress : MainHandler.getCurrentInstance()
				.getInfo().getAddressList()) {
			if (routeTo.isPointOf(testAddress)) {
				ourPoint = true;
				break;
			}
		}
		return ourPoint;
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
		FtnAddress from = getPrimaryFtnAddress();
		StringBuilder sb = new StringBuilder();
		if (fmsg.getMsgid() != null) {
			sb.append("\001REPLY: " + fmsg.getMsgid() + "\n");
		}
		sb.append(text);
		sb.append(quote(fmsg));
		writeNetmail(from, fmsg.getFromAddr(), fmsg.getToName(),
				fmsg.getFromName(), subject, sb.toString());
	}

	public static String quote(FtnMessage fmsg) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n========== Original message ==========\n");
		sb.append("From: " + fmsg.getFromName() + " (" + fmsg.getFromAddr()
				+ ")\n");
		if (fmsg.isNetmail()) {
			sb.append("To: " + fmsg.getToName() + " (" + fmsg.getToAddr()
					+ ")\n");
		} else {
			sb.append("Area: " + fmsg.getArea() + "\nTo: " + fmsg.getToName()
					+ "\n");
		}
		sb.append("Date: " + fmsg.getDate() + "\n");
		sb.append("Subject: " + fmsg.getSubject() + "\n");
		if (fmsg.getText() != null) {
			sb.append(fmsg.getText().replaceAll("\001", "@")
					.replaceAll("---", "+++")
					.replaceAll(" \\* Origin:", " + Origin:"));
		}
		sb.append("========== Original message ==========\n");
		return sb.toString();
	}

	public static void writeNetmail(FtnAddress from, FtnAddress to,
			String fromName, String toName, String subject, String text) {
		writeNetmail(from, to, fromName, toName, subject, text, 0, true);
	}

	/**
	 * Создание нетмейла
	 * 
	 * @param from
	 * @param to
	 * @param fromName
	 * @param toName
	 * @param subject
	 * @param text
	 */
	public static void writeNetmail(FtnAddress from, FtnAddress to,
			String fromName, String toName, String subject, String text,
			int attr, boolean route) {
		FtnMessage message = new FtnMessage();

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\001MSGID: %s %s\n\001PID: %s\n\001TID: %s\n",
				from.toString(), generate8d(), MainHandler.getVersion(),
				MainHandler.getVersion()));
		sb.append(text);
		sb.append("\n\n--- " + MainHandler.getVersion() + "\n");
		message.setDate(new Date());
		message.setToAddr(to);
		message.setFromAddr(from);
		message.setToName(toName);
		message.setFromName(fromName);
		message.setSubject(subject);
		message.setText(sb.toString());
		message.setNetmail(true);
		message.setAttribute(attr);
		processRewrite(message);
		Link routeVia = null;
		if (route) {
			routeVia = getRouting(message);
			if (routeVia == null) {
				logger.l2("Routing not found for " + to);
			}
		}
		Netmail net = ftnMessageToNetmail(message);
		net.setRouteVia(routeVia);
		ORMManager.get(Netmail.class).save(net);
		logger.l4("Netmail #" + net.getId() + " created");
		if (routeVia != null) {
			if (FtnTools.getOptionBooleanDefTrue(routeVia,
					LinkOption.BOOLEAN_CRASH_NETMAIL)) {
				PollQueue.getSelf().add(routeVia);
			}
		}
	}

	public static File createOutboundFile(Link link) {
		String template;
		if (link != null && link.getId() != null) {
			template = "out_" + link.getId() + ".%d";
		} else {
			template = "out_random.%d";
		}
		int i = 0;
		File f = new File(getInbound() + File.separator
				+ String.format(template, i));
		while (f.exists()) {
			i++;
			f = new File(getInbound() + File.separator
					+ String.format(template, i));
		}
		return f;
	}

	private static File createInboundFile(boolean secure) {
		String template = ((secure) ? "s" : "u") + "inb%d.pkt";
		int i = 0;
		File f = new File(getInbound() + File.separator
				+ String.format(template, i));
		while (f.exists()) {
			i++;
			f = new File(getInbound() + File.separator
					+ String.format(template, i));
		}
		return f;
	}

	public static String getInbound() {
		return MainHandler.getCurrentInstance().getProperty(BINKP_INBOUND,
				System.getProperty("file.tmp"));
	}

	private static File createZipFile(FtnPkt header, Link link,
			List<FtnMessage> messages) throws IOException {
		File np = createOutboundFile(link);
		FileOutputStream out = new FileOutputStream(np);
		ZipOutputStream zos = new ZipOutputStream(out);
		zos.setMethod(ZipOutputStream.DEFLATED);
		ZipEntry ze = new ZipEntry(String.format("%s.pkt", generate8d()));
		ze.setMethod(ZipEntry.DEFLATED);
		CRC32 crc32 = new CRC32();
		zos.putNextEntry(ze);
		int len = 0;
		byte[] data = header.pack();
		len += data.length;
		crc32.update(data);
		zos.write(data);
		for (FtnMessage m : messages) {
			data = m.pack();
			len += data.length;
			crc32.update(data);
			zos.write(data);
		}
		data = header.finalz();
		len += data.length;
		crc32.update(data);
		zos.write(data);
		ze.setSize(len);
		ze.setCrc(crc32.getValue());
		zos.close();
		out.close();
		return np;
	}

	/**
	 * Эхобандл
	 * 
	 * @return
	 */
	public static String generateEchoBundle() {
		String suffix = "";
		switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			suffix = "mo";
			break;
		case Calendar.TUESDAY:
			suffix = "tu";
			break;
		case Calendar.WEDNESDAY:
			suffix = "we";
			break;
		case Calendar.THURSDAY:
			suffix = "th";
			break;
		case Calendar.FRIDAY:
			suffix = "fr";
			break;
		case Calendar.SATURDAY:
			suffix = "sa";
			break;
		case Calendar.SUNDAY:
			suffix = "su";
			break;
		}
		int d = (int) (Math.random() * 9);
		return generate8d() + "." + suffix + d;
	}

	/**
	 * Паковка сообщений На удаление !
	 * 
	 * @param messages
	 * @param link
	 * @return
	 */
	@Deprecated
	protected static List<Message> pack(List<FtnMessage> messages, Link link) {
		boolean packNetmail = getOptionBooleanDefFalse(link,
				LinkOption.BOOLEAN_PACK_NETMAIL);
		boolean packEchomail = getOptionBooleanDefTrue(link,
				LinkOption.BOOLEAN_PACK_ECHOMAIL);

		List<Message> ret = new ArrayList<>();
		List<FtnMessage> packedEchomail = new ArrayList<>();
		List<FtnMessage> unpackedEchomail = new ArrayList<>();
		List<FtnMessage> packedNetmail = new ArrayList<>();
		List<FtnMessage> unpackedNetmail = new ArrayList<>();
		FtnAddress to = new FtnAddress(link.getLinkAddress());
		String password = link.getPaketPassword();
		FtnPkt header = new FtnPkt(selectOurAka(link), to, password, new Date());

		for (FtnMessage message : messages) {
			if (message.isNetmail()) {
				if (packNetmail) {
					packedNetmail.add(message);
				} else {
					unpackedNetmail.add(message);
				}
			} else {
				if (packEchomail) {
					packedEchomail.add(message);
				} else {
					unpackedEchomail.add(message);
				}
			}
		}

		if (!packedNetmail.isEmpty()) {

			try {
				for (FtnMessage net : packedNetmail) {
					FtnPkt head = new FtnPkt(header.getFromAddr(), to,
							password, new Date());
					Message m = new Message(createZipFile(head, link,
							Arrays.asList(net)));
					m.setMessageName(generateEchoBundle());
					ret.add(m);
				}
			} catch (Exception e) {
				logger.l1(
						"Error while writing netmail to link #" + link.getId(),
						e);
			}

		}
		if (!packedEchomail.isEmpty()) {
			try {
				Message m = new Message(createZipFile(header, link,
						packedEchomail));
				m.setMessageName(generateEchoBundle());
				ret.add(m);
			} catch (Exception e) {
				logger.l1(
						"Error while writing echomail to link #" + link.getId(),
						e);
			}
		}
		if (!unpackedNetmail.isEmpty()) {
			try {
				for (FtnMessage net : unpackedNetmail) {
					FtnPkt head = new FtnPkt(header.getFromAddr(), to,
							password, new Date());
					File out = createOutboundFile(link);
					FileOutputStream fos = new FileOutputStream(out);
					fos.write(head.pack());
					fos.write(net.pack());
					fos.write(head.finalz());
					fos.close();
					Message m = new Message(out);
					m.setMessageName(generate8d() + ".pkt");
					ret.add(m);
				}
			} catch (Exception e) {
				logger.l1(
						"Error while writing netmail to link #" + link.getId(),
						e);
			}
		}
		if (!unpackedEchomail.isEmpty()) {
			try {
				File out = createOutboundFile(link);
				FileOutputStream fos = new FileOutputStream(out);
				fos.write(header.pack());
				for (FtnMessage m : unpackedEchomail) {
					fos.write(m.pack());
				}
				fos.write(header.finalz());
				fos.close();
				Message m = new Message(out);
				m.setMessageName(generate8d() + ".pkt");
				ret.add(m);
			} catch (Exception e) {
				logger.l1(
						"Error while writing netmail to link #" + link.getId(),
						e);
			}
		}

		return ret;
	}

	public static FtnAddress selectOurAka(Link link) {
		if (MainHandler.getCurrentInstance().getInfo().getAddressList().size() < 2) {
			return getPrimaryFtnAddress();
		}
		FtnAddress ret = getPrimaryFtnAddress();
		FtnAddress addr = new FtnAddress(link.getLinkAddress());
		if (addr.getPoint() > 0) {
			for (FtnAddress address : MainHandler.getCurrentInstance()
					.getInfo().getAddressList()) {
				if (addr.isPointOf(address)) { // если это пойнт - то
												// посылаем с того
												// адреса, на
												// который он
												// привязан
					ret = address;
					break;
				}
			}
		} else {
			String ourAka = FtnTools.getOption(link, LinkOption.STRING_OUR_AKA);
			if (ourAka != null) {
				try {
					FtnAddress _our = new FtnAddress(ourAka);
					if (MainHandler.getCurrentInstance().getInfo()
							.getAddressList().contains(_our)) {
						ret = _our;
					}
				} catch (NumberFormatException e) {
				}
			}
		}
		logger.l5("Using aka " + ret + " for " + link.getLinkAddress() + "");
		return ret;
	}

	public static String getOptionForAddr(FtnAddress toAddr, String name) {
		Link link = getLinkByFtnAddress(toAddr);
		if (link != null) {
			return getOption(link, name);
		}
		return null;
	}

	/**
	 * Кривые пакеты - в инбаунд -- на удаление
	 * 
	 * @param pkt
	 */
	@Deprecated
	protected static void moveToBad(FtnPkt pkt) {
		ByteArrayInputStream bis = new ByteArrayInputStream(pkt.pack());
		Message message = new Message(String.format("%s_%d.pkt", generate8d(),
				new Date().getTime() / 1000), bis.available());
		message.setInputStream(bis);
		try {
			unpack(message);
		} catch (IOException e) {
			logger.l2("fail move to bad", e);
		}
	}

	/**
	 * Делаем реврайт
	 * 
	 * @param message
	 */
	public static void processRewrite(FtnMessage message) {
		List<Rewrite> rewrites = ORMManager.get(Rewrite.class).getOrderAnd(
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
		Echoarea ret;
		name = name.toLowerCase();
		ret = ORMManager.get(Echoarea.class).getFirstAnd("name", "=", name);
		if (ret == null) {
			if (link == null
					|| getOptionBooleanDefFalse(link,
							LinkOption.BOOLEAN_AUTOCREATE_AREA)) {
				ret = new Echoarea();
				ret.setName(name);
				ret.setDescription("Autocreated echoarea");
				ret.setReadlevel((link != null) ? getOptionLong(link,
						LinkOption.LONG_LINK_LEVEL) : 0);
				ret.setWritelevel((link != null) ? getOptionLong(link,
						LinkOption.LONG_LINK_LEVEL) : 0);
				ret.setGroup((link != null) ? getOptionString(link,
						LinkOption.SARRAY_LINK_GROUPS).split(" ")[0] : "");
				logger.l3("Echoarea " + name.toUpperCase() + " created");
				ORMManager.get(Echoarea.class).save(ret);
				if (link != null) {
					Subscription sub = new Subscription();
					sub.setArea(ret);
					sub.setLink(link);
					ORMManager.get(Subscription.class).save(sub);
				}
				Notifier.INSTANSE.notify(new NewEchoareaEvent(name, link));
			}
		} else {
			if (link != null
					&& ORMManager.get(Subscription.class).getFirstAnd(
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
		Filearea ret;
		name = name.toLowerCase();
		ret = ORMManager.get(Filearea.class).getFirstAnd("name", "=", name);
		if (ret == null) {
			if (link == null
					|| getOptionBooleanDefFalse(link,
							LinkOption.BOOLEAN_AUTOCREATE_AREA)) {
				ret = new Filearea();
				ret.setName(name);
				ret.setDescription("Autocreated filearea");
				ret.setReadlevel((link != null) ? getOptionLong(link,
						LinkOption.LONG_LINK_LEVEL) : 0);
				ret.setWritelevel((link != null) ? getOptionLong(link,
						LinkOption.LONG_LINK_LEVEL) : 0);
				ret.setGroup((link != null) ? getOptionString(link,
						LinkOption.SARRAY_LINK_GROUPS).split(" ")[0] : "");
				logger.l3("Filearea " + name + " created");
				ORMManager.get(Filearea.class).save(ret);
				if (link != null) {
					FileSubscription sub = new FileSubscription();
					sub.setArea(ret);
					sub.setLink(link);
					ORMManager.get(FileSubscription.class).save(sub);
				}
				Notifier.INSTANSE.notify(new NewFileareaEvent(name, link));
			}
		} else {
			if (link != null
					&& ORMManager.get(FileSubscription.class).getFirstAnd(
							"filearea_id", "=", ret.getId(), "link_id", "=",
							link.getId()) == null) {
				ret = null;
			}
		}
		return ret;
	}

	public static boolean isADupe(Echoarea area, String msgid) {
		return ORMManager.get(Echomail.class).getFirstAnd("msgid", "=", msgid,
				"echoarea_id", "=", area) != null;
	}

	/**
	 * Проверка на дроп нетмейла
	 * 
	 * 
	 * @param netmail
	 * @return
	 */
	public static boolean checkNetmailMustDropped(FtnMessage netmail) {
		// дополнительная проверка
		if (!MainHandler.getCurrentInstance().getBooleanProperty(NETMAIL_VALID,
				true)) {
			return false;
		}
		boolean validFrom = false;
		boolean validTo = false;
		// к нам на узел
		if (isOurPoint(netmail.getToAddr())) {
			validTo = true;
		} else if (getLinkByFtnAddress(netmail.getToAddr()) != null) {
			validTo = true;
		} else if (getLinkByFtnAddress(netmail.getToAddr().cloneNode()) != null) {
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

			} else {
				validTo = true;
				if (to.getStatus().equals(Status.DOWN)) {
					FtnTools.writeReply(netmail, "Destination is DOWN",
							"Warning! Destination of your netmail is DOWN.");
					logger.l3(String.format(
							"Netmail %s -> %s reject ( dest is DOWN )", netmail
									.getFromAddr().toString(), netmail
									.getToAddr().toString()));
					validTo = true;
				} else if (to.getStatus().equals(Status.HOLD)) {
					FtnTools.writeReply(netmail, "Destination is HOLD",
							"Warning! Destination of your netmail is HOLD");
					logger.l4(String.format(
							"Netmail %s -> %s warn ( dest is Hold )", netmail
									.getFromAddr().toString(), netmail
									.getToAddr().toString()));

				}
			}
		}

		if (isOurPoint(netmail.getFromAddr())) {
			validFrom = true;
		} else if (getLinkByFtnAddress(netmail.getFromAddr()) != null) {
			validFrom = true;
		} else if (getLinkByFtnAddress(netmail.getFromAddr().cloneNode()) != null) {
			validTo = true;
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
		writeEchomail(area, subject, text, MainHandler.getCurrentInstance()
				.getInfo().getStationName(), "All");
	}

	/**
	 * Эхомейл
	 * 
	 * @param area
	 * @param subject
	 * @param text
	 * @param fromName
	 * @param toName
	 */
	public static void writeEchomail(Echoarea area, String subject,
			String text, String fromName, String toName) {
		Echomail mail = new Echomail();
		mail.setFromFTN(getPrimaryFtnAddress().toString());
		mail.setFromName(fromName);
		mail.setArea(area);
		mail.setDate(new Date());
		mail.setPath("");
		mail.setSeenBy("");
		mail.setToName(toName);
		mail.setSubject(subject);
		mail.setMsgid(getPrimaryFtnAddress().toString() + " "
				+ FtnTools.generate8d());
		StringBuilder b = new StringBuilder();
		b.append(String.format("\001PID: %s\n\001TID: %s\n\n",
				MainHandler.getVersion(), MainHandler.getVersion()));
		b.append(text);
		b.append("\n--- "
				+ MainHandler.getCurrentInstance().getInfo().getStationName()
				+ "\n");
		b.append(" * Origin: " + MainHandler.getVersion() + " ("
				+ getPrimaryFtnAddress().toString() + ")\n");
		mail.setText(b.toString());
		ORMManager.get(Echomail.class).save(mail);
		if (mail.getId() != null) {
			for (Subscription s : ORMManager.get(Subscription.class).getAnd(
					"echoarea_id", "=", area)) {
				ORMManager.get(EchomailAwaiting.class).save(
						new EchomailAwaiting(s.getLink(), mail));
			}
		}
	}

	public static FtnAddress getPrimaryFtnAddress() {
		return MainHandler.getCurrentInstance().getInfo().getAddressList()
				.get(0);
	}

	/**
	 * Отправка файла в фэху
	 * 
	 * @param area
	 * @param attach
	 * @param description
	 */
	public static void hatchFile(Filearea area, File attach, String filename,
			String description) {
		Filemail mail = new Filemail();
		mail.setFilearea(area);
		mail.setFiledesc(description);
		mail.setFilename(filename);
		mail.setCreated(new Date());
		mail.setOrigin(getPrimaryFtnAddress().toString());
		mail.setPath("PATH " + getPrimaryFtnAddress().toString() + " "
				+ (mail.getCreated().getTime() / 1000) + " "
				+ mail.getCreated().toString() + "\r\n");
		mail.setSeenby("");
		String path = getFilePath(area.getName(), filename);
		File newFile = new File(path);
		if (FileUtils.move(attach, newFile, true)) {
			mail.setFilepath(newFile.getAbsolutePath());
		} else {
			mail.setFilepath(attach.getAbsolutePath());
			logger.l2("Failed to rename " + attach.getAbsolutePath() + " to "
					+ path);
		}
		ORMManager.get(Filemail.class).save(mail);

		for (FileSubscription sub : ORMManager.get(FileSubscription.class)
				.getAnd("filearea_id", "=", area)) {
			ORMManager.get(FilemailAwaiting.class).save(
					new FilemailAwaiting(sub.getLink(), mail));
			if (getOptionBooleanDefFalse(sub.getLink(),
					LinkOption.BOOLEAN_CRASH_FILEMAIL)) {
				PollQueue.getSelf().add(sub.getLink());
			}
		}
	}

	public static String getFilePath(String area, String attach) {
		String areaPath = FtnTosser.getFileechoPath() + File.separator + area;
		File f = new File(areaPath);
		if (!f.isDirectory()) {
			if (f.exists()) {
				f.renameTo(new File(areaPath + "." + generate8d()));
				f = new File(areaPath);
			}
			f.mkdirs();
		}
		return (f.getAbsolutePath() + File.separator + attach).toLowerCase();
	}

	public static String md5(String protocolPassword) {
		MessageDigest mdEnc;
		try {
			mdEnc = MessageDigest.getInstance("MD5");
			mdEnc.update(protocolPassword.getBytes(), 0,
					protocolPassword.length());
			String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
			return "MD5-" + md5;
		} catch (NoSuchAlgorithmException e) {
			return "PLAIN-" + protocolPassword;
		}

	}

	public static byte[] objectToBytes(Object object) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(object);
			os.close();
			return bos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	public static void delete(Filearea area) {
		synchronized (Filearea.class) {
			if (area != null) {
				area.setGroup("_TO_DELETE_XXX"); // to prevent suncribing while
													// deleting
				ORMManager.get(Filearea.class).update(area);
				ORMManager.get(FileSubscription.class).delete("filearea_id",
						"=", area);
				List<Filemail> toDelete = ORMManager.get(Filemail.class)
						.getAnd("filearea_id", "=", area);
				for (Filemail mail : toDelete) {
					ORMManager.get(FilemailAwaiting.class).delete(
							"filemail_id", "=", mail);
					ORMManager.get(Filemail.class).delete(mail);
				}
				ORMManager.get(Filearea.class).delete(area);
				logger.l2("Filearea " + area.getName() + " deleted");
			}
		}

	}

	public static void delete(Echoarea area) {
		synchronized (Echoarea.class) {
			if (area != null) {
				area.setGroup("_TO_DELETE_XXX"); // to prevent suncribing while
													// deleting
				ORMManager.get(Echoarea.class).update(area);
				ORMManager.get(Subscription.class).delete("echoarea_id", "=",
						area);
				List<Echomail> toDelete = ORMManager.get(Echomail.class)
						.getAnd("echoarea_id", "=", area);
				for (Echomail mail : toDelete) {
					ORMManager.get(EchomailAwaiting.class).delete(
							"echomail_id", "=", mail);
					ORMManager.get(Echomail.class).delete(mail);
				}
				ORMManager.get(Echoarea.class).delete(area);
				logger.l2("Echoarea " + area.getName() + " deleted");
			}
		}

	}

	public static void delete(Link link) {
		synchronized (TosserQueue.getInstanse()) {
			if (link != null) {
				ORMManager.get(LinkOption.class).delete("link_id", "=", link);
				link.setProtocolHost("-");
				link.setProtocolPort(0);
				link.setProtocolPassword("_TO_DELETE_XXX");
				// to prevent connect
				ORMManager.get(Link.class).update(link);
				ORMManager.get(EchomailAwaiting.class).delete("link_id", "=",
						link);
				ORMManager.get(FilemailAwaiting.class).delete("link_id", "=",
						link);
				ORMManager.get(Subscription.class).delete("link_id", "=", link);
				ORMManager.get(FileSubscription.class).delete("link_id", "=",
						link);
				ORMManager.get(FileForLink.class).delete("link_id", "=", link);
				ORMManager.get(Route.class).delete("route_via", "=", link);
				ORMManager.get(Netmail.class).update("route_via", 0,
						"route_via", "=", link);
				ORMManager.get(Link.class).delete(link);
				logger.l2("Link " + link.getLinkAddress() + " deleted");
			}
		}

	}

	public static void setOption(Link link, String name, String value) {
		LinkOption option = ORMManager.get(LinkOption.class).getFirstAnd(
				"link_id", "=", link, "name", "=", name);
		if (option == null) {
			option = new LinkOption(link, name, value);
		} else {
			option.setValue(value);
		}
		ORMManager.get(LinkOption.class).saveOrUpdate(option);
	}

}
