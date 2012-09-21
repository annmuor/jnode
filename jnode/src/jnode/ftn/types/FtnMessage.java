package jnode.ftn.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.ftn.FtnTools;
import jnode.ftn.exception.LastMessageException;

/**
 * 
 * @author kreon
 * 
 */
public class FtnMessage {
	public static final int ATTR_PVT = 1;
	public static final int ATTR_CRASH = 2;
	public static final int ATTR_RECD = 4;
	public static final int ATTR_SEND = 8;
	public static final int ATTR_FILEATT = 16;
	public static final int ATTR_INTRANS = 32;
	public static final int ATTR_ORPHAN = 64;
	public static final int ATTR_KILLSENT = 128;
	public static final int ATTR_LOCAL = 256;
	public static final int ATTR_HOFOPICKUP = 512;
	public static final int ATTR_FILEREQ = 2048;
	public static final int ATTR_RRQ = 4096;
	public static final int ATTR_ISRR = 8192;
	public static final int ATTR_ARQ = 16384;
	public static final int ATTR_FIUPRQ = 32768;
	private Date date;
	private String fromName;
	private String toName;
	private FtnAddress fromAddr;
	private FtnAddress toAddr;
	private String area;
	private String subject;
	private String text;
	private int attribute;
	private List<Ftn2D> seenby;
	private List<Ftn2D> path;
	private boolean isNetmail;
	private String msgid;

	private static DateFormat format = new SimpleDateFormat(
			"dd MMM yy  HH:mm:ss", Locale.US);

	public FtnMessage() {
		seenby = new ArrayList<Ftn2D>();
		path = new ArrayList<Ftn2D>();
	}

	public FtnMessage(String fromName, String toName, FtnAddress fromAddr,
			String area, String subject, String text) {
		super();
		this.fromName = fromName;
		this.toName = toName;
		this.fromAddr = fromAddr;
		this.area = area;
		this.subject = subject;
		this.text = text;
		date = new Date();
		isNetmail = false;
		seenby = new ArrayList<Ftn2D>();
		path = new ArrayList<Ftn2D>();
	}

	public FtnMessage(String fromName, String toName, FtnAddress fromAddr,
			FtnAddress toAddr, String subject, String text) {
		super();
		this.fromName = fromName;
		this.toName = toName;
		this.fromAddr = fromAddr;
		this.toAddr = toAddr;
		this.subject = subject;
		this.text = text;
		date = new Date();
		isNetmail = true;
		seenby = new ArrayList<Ftn2D>();
		path = new ArrayList<Ftn2D>();
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public FtnAddress getFromAddr() {
		return fromAddr;
	}

	public void setFromAddr(FtnAddress fromAddr) {
		this.fromAddr = fromAddr;
	}

	public FtnAddress getToAddr() {
		return toAddr;
	}

	public void setToAddr(FtnAddress toAddr) {
		this.toAddr = toAddr;
		area = "";
		isNetmail = true;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
		isNetmail = false;
		toAddr = new FtnAddress();
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Ftn2D> getSeenby() {
		return seenby;
	}

	public void setSeenby(List<Ftn2D> seenby) {
		this.seenby = seenby;
	}

	public List<Ftn2D> getPath() {
		return path;
	}

	public void setPath(List<Ftn2D> path) {
		this.path = path;
	}

	public String getMsgid() {
		return msgid;
	}

	public boolean isNetmail() {
		return isNetmail;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public byte[] pack() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(bos);
		try {
			os.write(new byte[] { 2, 0 });
			os.writeShort(FtnTools.revShort(fromAddr.getNode()));
			os.writeShort((isNetmail) ? FtnTools.revShort(toAddr.getNode()) : 0);
			os.writeShort(FtnTools.revShort(fromAddr.getNet()));
			os.writeShort((isNetmail) ? FtnTools.revShort(toAddr.getNet()) : 0);
			if (isNetmail) {
				attribute &= ATTR_PVT;
				os.writeShort(FtnTools.revShort((short) attribute)); // attributes
			} else {
				os.write(new byte[] { 1, 0 });
			}
			os.write(new byte[] { 0, 0 });
			os.write(FtnTools.substr(format.format(date), 19));
			os.write(0);
			os.write(FtnTools.substr(toName, 35));
			os.write(0);
			os.write(FtnTools.substr(fromName, 35));
			os.write(0);
			os.write(FtnTools.substr(subject, 71));
			os.write(0);
			if (!isNetmail) {
				os.writeBytes(String.format("AREA:%s\r", area));
			} else {
				os.writeBytes(String.format("\001INTL %s %s\r", toAddr.intl(),
						fromAddr.intl()));
				os.writeBytes(fromAddr.fmpt());
				os.writeBytes(toAddr.topt());
			}
			StringBuilder sb = new StringBuilder();
			sb.append(text);
			if (sb.charAt(sb.length() - 1) != '\n') {
				sb.append('\n');
			}
			if (!isNetmail) {
				sb.append(FtnTools.writeSeenBy(seenby));
				sb.append(FtnTools.writePath(path));
			}
			os.write(sb.toString().replaceAll("\n", "\r")
					.getBytes(FtnTools.cp866));
			os.write(0);
			os.close();
		} catch (IOException e) {
			// zzz
		}
		return bos.toByteArray();
	}

	public void unpack(byte[] data) throws LastMessageException {
		try {
			InputStream is = new ByteArrayInputStream(data);
			unpack(is);
			is.close();
		} catch (IOException e) {

		}

	}

	public void unpack(InputStream iz) throws LastMessageException {
		DataInputStream is = new DataInputStream(iz);
		fromAddr = new FtnAddress();
		toAddr = new FtnAddress();
		try {
			if (is.read() == 2 && is.read() == 0) { // 2.0 msg
				fromAddr.setNode(FtnTools.revShort(is.readShort()));
				toAddr.setNode(FtnTools.revShort(is.readShort()));
				fromAddr.setNet(FtnTools.revShort(is.readShort()));
				toAddr.setNet(FtnTools.revShort(is.readShort()));
				attribute = FtnTools.revShort(is.readShort());
				is.skip(2);
				date = format.parse(FtnTools.readUntillNull(is));
				toName = FtnTools.readUntillNull(is);
				fromName = FtnTools.readUntillNull(is);
				subject = FtnTools.readUntillNull(is);
				String lines[] = FtnTools.readUntillNull(is).split("\r");
				StringBuilder builder = new StringBuilder();
				int linenum = 0;
				boolean eofKluges = false;
				boolean preOrigin = false;
				boolean afterOrigin = false;
				Pattern netmail = Pattern
						.compile("^\001(INTL|FMPT|TOPT) (.*)$");
				Pattern origin = Pattern.compile("^ \\* Origin: ([\\S\\t ]*)$");
				Pattern msgid = Pattern.compile("^\001MSGID: (.*)$");
				StringBuilder seenby = new StringBuilder();
				StringBuilder path = new StringBuilder();
				for (String line : lines) {
					linenum++;
					if (linenum == 1) {
						if (line.matches("^AREA:\\S+$")) {
							isNetmail = false;
							area = line.replaceFirst("^AREA:", "");
							continue;
						} else {
							isNetmail = true;
						}
					}

					if (!eofKluges && linenum > 1 && !line.matches("^\001.*$")) {
						eofKluges = true;
					}

					if (!eofKluges) {
						Matcher m = msgid.matcher(line);
						if (m.matches()) {
							this.msgid = m.group(1).toUpperCase();
						}
						if (isNetmail) {
							m = netmail.matcher(line);
							if (m.matches()) {
								String kluge = m.group(1);
								String arg = m.group(2);
								if (kluge.equals("INTL")) {
									String tmp[] = arg.split(" ");
									toAddr = new FtnAddress(tmp[0]);
									fromAddr = new FtnAddress(tmp[1]);
								} else if (kluge.equals("TOPT")) {
									toAddr.setPoint(new Integer(arg));
								} else if (kluge.equals("FMPT")) {
									fromAddr.setPoint(new Integer(arg));
								}
								continue;
							}
						}
						builder.append(line);
						builder.append('\n');

					} else if (preOrigin && !isNetmail) {
						if (line.startsWith("SEEN-BY: ")) {
							afterOrigin = true;
						} else {
							preOrigin = false;
						}
						if (afterOrigin) {
							if (line.startsWith("SEEN-BY: ")) {
								seenby.append(line);
								seenby.append('\n');
							} else if (line.startsWith("\001PATH: ")) {
								path.append(line);
								path.append('\n');
							}
						}
					} else {
						if (!isNetmail) {
							Matcher m = origin.matcher(line);
							if (m.matches()) {
								Pattern f = Pattern
										.compile("([1-5]?:?\\d{1,5}/\\d{1,5}(\\.\\d{1,5})?)");
								preOrigin = true;
								String orig = m.group(1);
								Matcher fm = f.matcher(orig);
								String ftnAddr = "";
								while (fm.find()) {
									ftnAddr = fm.group(1);
								}
								this.fromAddr = new FtnAddress(ftnAddr);
							}
						}
						builder.append(line);
						builder.append('\n');
					}
				}
				this.seenby = FtnTools.readSeenBy(seenby.toString());
				this.path = FtnTools.readPath(path.toString());
				text = builder.toString();
			} else {
				throw new LastMessageException("2.0 is not out version");
			}
		} catch (IOException e) {
			throw new LastMessageException(e);
		} catch (ParseException e) {
			throw new LastMessageException(e);
		} catch (LastMessageException e) {
			throw new LastMessageException(e);
		}
	}

	@Override
	public String toString() {
		return String
				.format("Message %s -> %s\nFrom: %s\nTo: %s\nDate: %s\nSubject: %s\nArea: %s\n-------------\n%s\n-------------\n",
						fromAddr, toAddr, fromName, toName, date, subject,
						area, text);
	}
}
