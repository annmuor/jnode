package jnode.protocol.binkp;

import java.nio.ByteBuffer;

import jnode.protocol.io.Frame;

/**
 * 
 * @author kreon
 * 
 */
public class BinkpFrame implements Frame {
	private boolean isCommand; // if false - is a file
	private BinkpCommand command;
	private byte[] data;
	private ByteBuffer frame;
	private String arg;

	public BinkpFrame(BinkpCommand command) {
		this(command, null);
	}

	public BinkpFrame(BinkpCommand command, String arg) {
		this.arg = arg;
		isCommand = true;
		this.command = command;
		int arglen = (arg == null) ? 0 : arg.getBytes().length;
		ByteBuffer buf = ByteBuffer.allocate(1 + arglen);
		buf.put((byte) command.getCmd());
		if (arglen > 0) {
			buf.put(arg.getBytes());
		}
		this.data = buf.array();
		init();
	}

	public BinkpFrame(byte[] filedata) {
		isCommand = false;
		this.data = filedata;
		init();
	}

	public BinkpCommand getCommand() {
		return command;
	}

	public byte[] getData() {
		return data;
	}

	public boolean isCommand() {
		return isCommand;
	}

	public String getArg() {
		return arg;
	}

	private void init() {
		if (data == null || data.length == 0) {
			return;
		}
		int len = data.length;
		if (isCommand) {
			len |= 0x8000;
		} else {
			len &= 0x7fff;
		}
		frame = ByteBuffer.allocate(2 + data.length);
		frame.putShort((short) len);
		frame.put(data);
	}

	@Override
	public byte[] getBytes() {
		return (frame != null) ? frame.array() : new byte[0];
	}

    private String displayFrame(){
        byte[] d = getBytes();
        StringBuilder sb = new StringBuilder();
        sb.append("length = ");
        sb.append(d.length);
        sb.append(", ");
        sb.append(DisplayByteArrayHelper.bytesToHex(d, 10));
        return sb.toString();
    }

	@Override
	public String toString() {
		return "[ " + ((isCommand) ? command.toString() + " " + arg : displayFrame())
				+ " ]";
	}


    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static class DisplayByteArrayHelper{
        private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
        private static String bytesToHex(byte[] bytes, int limit) {

            int realLen = Math.min(bytes.length, limit);

            if (realLen <= 0){
                return "";
            }

            final char[] hexChars = new char[realLen * 2];
            int v;
            for ( int j = 0; j < realLen; ++j ) {
                v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
    }
}
