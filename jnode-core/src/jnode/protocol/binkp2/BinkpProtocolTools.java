package jnode.protocol.binkp2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jnode.dto.Link;
import jnode.ftn.tosser.FtnTosser;
import jnode.protocol.binkp.BinkpCommand;
import jnode.protocol.binkp.BinkpFrame;
import jnode.protocol.io.Message;

public class BinkpProtocolTools {

	public static byte hex2decimal(String s) {
		String digits = "0123456789ABCDEF";
		s = s.toUpperCase();
		byte val = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int d = digits.indexOf(c);
			val = (byte) (16 * val + d);
		}
		return val;
	}

	public static String getAuthPassword(Link foreignLink, boolean secure,
			String cramAlgo, String cramText) {
		MessageDigest md;
		String password = (secure) ? foreignLink.getProtocolPassword() : "-";
		if (!secure || cramAlgo == null || cramText == null) {
			return password;
		} else {
			try {
				md = MessageDigest.getInstance(cramAlgo);
			} catch (NoSuchAlgorithmException e) {
				return password;
			}
			byte[] text = new byte[cramText.length() / 2];
			byte[] key = password.getBytes();
			byte[] k_ipad = new byte[64];
			byte[] k_opad = new byte[64];
			for (int i = 0; i < cramText.length(); i += 2) {
				text[i / 2] = hex2decimal(cramText.substring(i, i + 2));
			}

			for (int i = 0; i < key.length; i++) {
				k_ipad[i] = key[i];
				k_opad[i] = key[i];
			}

			for (int i = 0; i < 64; i++) {
				k_ipad[i] ^= 0x36;
				k_opad[i] ^= 0x5c;
			}
			md.update(k_ipad);
			md.update(text);
			byte[] digest = md.digest();
			md.update(k_opad);
			md.update(digest);
			digest = md.digest();
			StringBuilder builder = new StringBuilder();
			builder.append("CRAM-" + cramAlgo + "-");
			for (int i = 0; i < 16; i++) {
				builder.append(String.format("%02x", digest[i]));
			}
			return builder.toString();
		}
	}

	public static boolean availible(Socket socket) {
		try {
			return socket.getInputStream().available() > 2;
		} catch (IOException e) {
			return false;
		}
	}

	public static BinkpCommand getCommand(int command) {
		for (BinkpCommand c : BinkpCommand.values()) {
			if (c.getCmd() == command) {
				return c;
			}
		}
		return null;
	}

	public static int write(BinkpFrame frame, SocketChannel socket) {
		if (frame != null) {
			try {
				ByteBuffer buf = ByteBuffer.wrap(frame.getBytes());
				socket.write(buf);
				return 1;
			} catch (IOException e) {
				return 0;
			}
		}
		return 0;
	}

	public static boolean messageEquals(Message message, String arg) {
		String[] args = arg.split(" ");
		try {
			Long len = Long.valueOf(args[1]);
			Long unixtime = Long.valueOf(args[2]);
			if (message.getMessageName().equals(args[0])) {
				if (message.getMessageLength() == len.longValue()) {
					if (message.getUnixtime().equals(unixtime)) {
						return true;
					}
				}
			}
		} catch (RuntimeException e) {
		}
		return false;
	}

	public static Message createMessage(String arg) {
		String[] args = arg.split(" ");
		try {
			Long len = Long.valueOf(args[1]);
			Long unixtime = Long.valueOf(args[2]);
			Message message = new Message(args[0], len);
			message.setUnixtime(unixtime);
			return message;
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static String getString(Message message, int skip) {
		return String.format("%s %d %d %d", message.getMessageName(),
				message.getMessageLength(), message.getUnixtime(), skip);
	}

	public static String getString(Message message) {
		return String.format("%s %d %d", message.getMessageName(),
				message.getMessageLength(), message.getUnixtime());
	}

	@SuppressWarnings("resource")
	public static int forwardToTossing(Message message, File file,
			OutputStream os) {
		InputStream is = null;
		try {
			is = (file != null) ? new FileInputStream(file)
					: new ByteArrayInputStream(
							((ByteArrayOutputStream) os).toByteArray());
			message.setInputStream(is);
			int ret = FtnTosser.tossIncoming(message);
			if (file != null) {
				file.delete();
			}
			return ret;
		} catch (IOException e) {
		}
		return 1;
	}
}
