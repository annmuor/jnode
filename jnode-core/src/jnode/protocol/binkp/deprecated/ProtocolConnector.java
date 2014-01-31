package jnode.protocol.binkp.deprecated;

import java.io.InputStream;

import jnode.protocol.binkp.BinkpFrame;
import jnode.protocol.io.Message;

/**
 * 
 * @author kreon
 * 
 */
@Deprecated
public interface ProtocolConnector {

	public void initOutgoing(Connector connector);

	public void initIncoming(Connector connector);

	public int availible(InputStream is);

	public BinkpFrame[] getFrames();

	public boolean closed();

	public boolean canSend();

	public void reset();

	public void send(Message message);

	public void eob();

	public boolean getIncoming();

	public boolean getSuccess();

	public int getBytesReceived();

	public int getBytesSent();

}
