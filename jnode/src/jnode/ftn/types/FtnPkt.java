package jnode.ftn.types;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jnode.ftn.FtnTools;
import jnode.ftn.exception.LastMessageException;

/**
 * 
 * @author kreon
 * 
 */
public class FtnPkt {
	private FtnAddress fromAddr;
	private FtnAddress toAddr;
	private String password;
	private List<FtnMessage> messages;
	private Date date;
	private static DateFormat format = new SimpleDateFormat(
			"yyyy MM dd HH mm ss", Locale.US);

	public FtnAddress getFromAddr() {
		return fromAddr;
	}

	public FtnAddress getToAddr() {
		return toAddr;
	}

	public String getPassword() {
		return password;
	}

	public Date getDate() {
		return date;
	}

	public static DateFormat getFormat() {
		return format;
	}

	public FtnPkt() {
		messages = new ArrayList<FtnMessage>();
	}

	public FtnPkt(FtnAddress fromAddr, FtnAddress toAddr, String password,
			Date date) {
		super();
		messages = new ArrayList<FtnMessage>();
		this.fromAddr = fromAddr;
		this.toAddr = toAddr;
		this.password = password;
		this.date = date;
	}

	public List<FtnMessage> getMessages() {
		return messages;
	}

	public byte[] pack() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(bos);
		try {
			os.writeShort(FtnTools.revShort(fromAddr.getNode()));
			os.writeShort(FtnTools.revShort(toAddr.getNode()));
			String date = format.format(this.date); // here
			{
				int n = 0;
				for (String d : date.split(" ")) {
					short s = new Short(d);
					if (n == 1) {
						s--;
					}
					os.writeShort(FtnTools.revShort(s));
					n++;
				}
			}
			os.write(new byte[] { 0, 0, 2, 0 });
			os.writeShort(FtnTools.revShort(fromAddr.getNet()));
			os.writeShort(FtnTools.revShort(toAddr.getNet()));
			os.write(new byte[] { (byte) 255, 0 }); // prodcode 19FF ver 0.4
			os.write(FtnTools.substr(password, 8));
			for (int i = password.length(); i < 8; i++) {
				os.write(0);
			}
			os.writeShort(FtnTools.revShort(fromAddr.getZone()));
			os.writeShort(FtnTools.revShort(toAddr.getZone()));
			os.write(new byte[] { 0, 0, 0, 1, 19, 4, 1, 0 });// prodcode 19FF
																// ver 0.4
			os.writeShort(FtnTools.revShort(fromAddr.getZone()));
			os.writeShort(FtnTools.revShort(toAddr.getZone()));
			os.writeShort(FtnTools.revShort(fromAddr.getPoint()));
			os.writeShort(FtnTools.revShort(toAddr.getPoint()));
			os.write(new byte[] { 0, 0, 0, 0 });
			if (messages != null) {
				for (FtnMessage message : messages) {
					byte[] msg = message.pack();
					os.write(msg);
				}
			}
			os.write(new byte[] { 0, 0 });
			os.close();
		} catch (IOException e) {
			//
		}
		return bos.toByteArray();
	}

	public void unpack(InputStream iz) {
		unpack(iz, true);
	}

	public void unpack(InputStream iz, boolean close) {
		DataInputStream is = new DataInputStream(iz);
		fromAddr = new FtnAddress();
		toAddr = new FtnAddress();
		try {
			fromAddr.setNode(FtnTools.revShort(is.readShort()));
			toAddr.setNode(FtnTools.revShort(is.readShort()));
			{
				short date[] = new short[6];
				for (int i = 0; i < 6; i++) {
					date[i] = FtnTools.revShort(is.readShort());
				}
				try {
					Calendar calendar = Calendar.getInstance();
					calendar.set(date[0], date[1], date[2], date[3], date[4],
							date[5]);
					this.date = calendar.getTime();
				} catch (Exception e) {
					this.date = new Date(0);
				}
			}
			is.skip(4);
			fromAddr.setNet(FtnTools.revShort(is.readShort()));
			toAddr.setNet(FtnTools.revShort(is.readShort()));
			is.skip(2);
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				for (int i = 0; i < 8; i++) {
					int c = is.read();
					if (c != 0) {
						bos.write(c);
					}
				}
				bos.close();
				password = new String(bos.toByteArray());
			}
			is.skip(12);
			fromAddr.setZone(FtnTools.revShort(is.readShort()));
			toAddr.setZone(FtnTools.revShort(is.readShort()));
			fromAddr.setPoint(FtnTools.revShort(is.readShort()));
			toAddr.setPoint(FtnTools.revShort(is.readShort()));
			is.skip(4);
			try {
				while (true) {
					FtnMessage mess = new FtnMessage();
					mess.unpack(iz);
					messages.add(mess);
				}

			} catch (LastMessageException e) {
				if (close) {
					iz.close();
				}
			}
		} catch (IOException e) {

		}
	}

	@Override
	public String toString() {
		return String.format(
				"PKT From: %s\nPKT To: %s\nPKT Date: %s\nPKT Password: %s\n",
				fromAddr, toAddr, date, password);
	}
}
