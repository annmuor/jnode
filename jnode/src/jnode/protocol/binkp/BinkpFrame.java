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
		ByteBuffer buf = ByteBuffer.allocate(1 + ((arg != null) ? arg.length()
				: 0));
		buf.put((byte) command.getCmd());
		if (arg != null) {
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

	@Override
	public String toString() {
		return "[ " + ((isCommand) ? command.toString() + " " + arg : " DATA ")
				+ " ]";
	}

}
