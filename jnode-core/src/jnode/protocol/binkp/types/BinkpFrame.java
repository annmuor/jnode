package jnode.protocol.binkp.types;

/**
 * 
 * @author kreon
 * 
 */
public class BinkpFrame {
	private static final int MAX_SIZE = 32767;
	private BinkpCommand command;
	private byte[] frame;
	private String arg;

	public BinkpFrame(BinkpCommand command) {
		this(command, null);
	}

	public BinkpFrame(BinkpCommand command, String arg) {
		this.arg = arg;
		this.command = command;
		int arglen = (arg == null) ? 0 : arg.getBytes().length;
		frame = new byte[arglen + 3];
		System.arraycopy(arg.getBytes(), 0, frame, 3, arglen);
		frame[2] = (byte) (command.getCmd() & 0xff);
		init();
	}

	public BinkpFrame(byte[] filedata) {
		frame = new byte[filedata.length + 2];
		System.arraycopy(filedata, 0, frame, 2, filedata.length);
		init();
	}

	public BinkpFrame(byte[] filedata, int len) {
		frame = new byte[filedata.length + 2];
		System.arraycopy(filedata, 0, frame, 2, len);
		init();
	}

	public BinkpCommand getCommand() {
		return command;
	}

	public String getArg() {
		return arg;
	}

	private void init() {
		int datalen = frame.length - 2;
		if (datalen > MAX_SIZE) {
			// тут ругаться матом
		} else {
			datalen &= 0x7fff;
		}
		if (this.command != null) {
			datalen |= 0x8000;
		}
		frame[0] = (byte) ((datalen >> 8) & 0xff);
		frame[1] = (byte) (datalen & 0xff);
	}

	public byte[] getBytes() {
		return (frame != null) ? frame : new byte[0];
	}

	private String displayFrame() {
		byte[] d = frame;
		StringBuilder sb = new StringBuilder();
		sb.append("length = ");
		sb.append(d.length);
		sb.append(", ");
		sb.append(DisplayByteArrayHelper.bytesToHex(d, 10));
		return sb.toString();
	}

	@Override
	public String toString() {
		return "[ "
				+ ((command != null) ? command.toString() + " " + arg
						: displayFrame()) + " ]";
	}

	// http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	private static class DisplayByteArrayHelper {
		private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

		private static String bytesToHex(byte[] bytes, int limit) {

			int realLen = Math.min(bytes.length, limit);

			if (realLen <= 0) {
				return "";
			}

			final char[] hexChars = new char[realLen * 2];
			int v;
			for (int j = 0; j < realLen; ++j) {
				v = bytes[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			}
			return new String(hexChars);
		}
	}
}
