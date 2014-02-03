package jnode.protocol.binkp.types;

/**
 * 
 * @author kreon
 * 
 */
public enum BinkpCommand {
	M_NUL(0), M_ADR(1), M_PWD(2), M_FILE(3), M_OK(4), M_EOB(5), M_GOT(6), M_ERR(7), M_BSY(8), M_GET(9), M_SKIP(10), M_PROCESS_FILE(
			99);
	private final int cmd;

	private BinkpCommand(int cmd) {
		this.cmd = cmd;
	}

	public int getCmd() {
		return cmd;
	}

	@Override
	public String toString() {
		return String.format("%s", this.name());
	}

}
