package jnode.protocol.io;

import java.io.InputStream;

/**
 * 
 * @author kreon
 * 
 */
public interface ProtocolConnector {

	public void initOutgoing(Connector connector);

	public void initIncoming(Connector connector);

	public void avalible(InputStream is);

	public Frame[] getFrames();

	public boolean closed();

	public boolean canSend();

	public void reset();

	public void send(Message message);

	public void eob();

}
