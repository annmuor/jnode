package jnode.stat;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import jnode.event.ConnectionEndEvent;
import jnode.event.IEvent;
import jnode.event.IEventHandler;
import jnode.event.Notifier;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.stat.threads.StatPoster;
import jnode.store.XMLSerializer;

public class ConnectionStat implements IStatPoster, IEventHandler {
    private static final Logger logger = Logger.getLogger(ConnectionStat.class);
	private final String statPath = FtnTools.getInbound() + File.separator
			+ "connstat.xml";

	public static class ConnectionStatDataElement  {
        public String linkStr;
        public int bytesReceived;
        public int bytesSended;
        public int incomingOk;
        public int incomingFailed;
        public int outgoingOk;
        public int outgoingFailed;
	}

    private List<ConnectionStatDataElement> load() {
        synchronized (ConnectionStat.class){
            return internalLoad();
        }
    }

    private List<ConnectionStatDataElement> loadAndDrop(){
        synchronized (ConnectionStat.class){
            List<ConnectionStatDataElement> result = internalLoad();
            try {
                XMLSerializer.write(new ArrayList<ConnectionStatDataElement>(), statPath);
            } catch (FileNotFoundException e) {
                logger.l2(MessageFormat.format("file {0} not found, fail clear data", statPath), e);
            }
            return result;
        }
    }

    private void store(ConnectionEndEvent evt, ConnectionStatDataElement element){
        synchronized (ConnectionStat.class){
            List<ConnectionStatDataElement> elements = internalLoad();
            int pos = findPos(evt, elements);
            if (pos == -1){
                elements.add(element);
            } else {
                elements.set(pos, element);
            }
            try {
                XMLSerializer.write(elements, statPath);
            } catch (FileNotFoundException e) {
                logger.l2(MessageFormat.format("file {0} not found, fail store data", statPath), e);
            }
        }
    }

    private List<ConnectionStatDataElement> internalLoad()  {
        List<ConnectionStatDataElement> result;
        try {
            result = new File(statPath).exists() ?
                    (List<ConnectionStatDataElement>) XMLSerializer.read(statPath) :
                    new ArrayList<ConnectionStatDataElement>();
        } catch (FileNotFoundException e) {
            logger.l2("file with stat connection ACCIDENTALLY not found", e);
            return new ArrayList<ConnectionStatDataElement>();
        }
        return result;
    }

    private int findPos(ConnectionEndEvent evt, List<ConnectionStatDataElement> elements){
        int pos = -1;
        for (int i = 0; i < elements.size(); ++i) {
            ConnectionStatDataElement element = elements.get(i);
            if (evt.getAddress() == null) {
                if (element.linkStr == null) {
                    pos = i;
                    break;
                }
            } else if (element.linkStr != null) {
                if (element.linkStr.equals(evt.getAddress().toString())) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }

	public ConnectionStat() {
		Notifier.INSTANSE.register(ConnectionEndEvent.class, this);
	}

	public void handle(IEvent event) {
		synchronized (ConnectionStat.class) {

			if (event instanceof ConnectionEndEvent) {
				ConnectionEndEvent evt = (ConnectionEndEvent) event;
				ConnectionStatDataElement current;

                List<ConnectionStatDataElement> elements = load();

                int pos = findPos(evt, elements);
                if (pos == -1){
                    current = new ConnectionStatDataElement();
                    current.linkStr = evt.getAddress() != null ? evt.getAddress().toString() : null;
                } else {
                    current = elements.get(pos);
                }

				if (evt.isIncoming()) {
					if (evt.isSuccess()) {
						current.incomingOk++;
					} else {
						current.incomingFailed++;
					}
				} else {
					if (evt.isSuccess()) {
						current.outgoingOk++;
					} else {
						current.outgoingFailed++;
					}
				}
				current.bytesReceived += evt.getBytesReceived();
				current.bytesSended += evt.getBytesSended();

                store(evt, current);
			}
		}
	}

	@Override
	public String getSubject() {
		return "Daily connection stat";
	}

	@Override
	public String getText() {
		List<ConnectionStatDataElement> elements = loadAndDrop();
		int iOkT = 0;
		int iFaT = 0;
		int oOkT = 0;
		int oFaT = 0;
		int bsT = 0;
		int brT = 0;
		StringBuilder sb = new StringBuilder();
		Collections.sort(elements, new Comparator<ConnectionStatDataElement>() {

			@Override
			public int compare(ConnectionStatDataElement arg0,
					ConnectionStatDataElement arg1) {
				FtnAddress a1 = new FtnAddress(arg0.linkStr);
				FtnAddress a2 = new FtnAddress(arg1.linkStr);
				if (a1 == null && a2 != null) {
					return 1;
				} else if (a2 == null && a1 != null) {
					return -1;
				} else if (a1 == null && a2 == null) {
					return 0;
				} else {
					return new FtnTools.Ftn4DComparator().compare(a1, a2);
				}
			}

		});
		// 19
		sb.append("|");
		sb.append("Link");
		for (int i = 4; i < 19; i++) {
			sb.append(' ');
		}
		sb.append("|");
		sb.append("I_OK");
		for (int i = 4; i < 5; i++) {
			sb.append(' ');
		}
		sb.append("|");
		sb.append("I_FA");
		for (int i = 4; i < 5; i++) {
			sb.append(' ');
		}
		sb.append("|");
		sb.append("O_OK");
		for (int i = 4; i < 5; i++) {
			sb.append(' ');
		}
		sb.append("|");
		sb.append("O_FA");
		for (int i = 4; i < 5; i++) {
			sb.append(' ');
		}
		sb.append("|");
		sb.append("BR");
		for (int i = 2; i < 9; i++) {
			sb.append(' ');
		}
		sb.append("|");
		sb.append("BS");
		for (int i = 2; i < 9; i++) {
			sb.append(' ');
		}
		sb.append("|\n");
		for (int i = 0; i < 65; i++) {
			sb.append('-');
		}
		sb.append('\n');
		for (ConnectionStatDataElement element : elements) {
            FtnAddress link = new FtnAddress(element.linkStr);
			String linkName = (link != null) ? link.toString()
					: "Unknown";
			iOkT += element.incomingOk;
			iFaT += element.incomingFailed;
			oOkT += element.outgoingOk;
			oFaT += element.outgoingFailed;
			bsT += element.bytesSended;
			brT += element.bytesReceived;

			String iOk = String.valueOf(element.incomingOk);
			String iFa = String.valueOf(element.incomingFailed);
			String oOk = String.valueOf(element.outgoingOk);
			String oFa = String.valueOf(element.outgoingFailed);
			String recv = b2s(element.bytesReceived);
			String sent = b2s(element.bytesSended);
			sb.append("|");
			sb.append(linkName);
			for (int i = linkName.length(); i < 19; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(iOk);
			for (int i = iOk.length(); i < 5; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(iFa);
			for (int i = iFa.length(); i < 5; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(oOk);
			for (int i = oOk.length(); i < 5; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(oFa);
			for (int i = oFa.length(); i < 5; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(recv);
			for (int i = recv.length(); i < 9; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(sent);
			for (int i = sent.length(); i < 9; i++) {
				sb.append(' ');
			}
			sb.append("|\n");
		}
		for (int i = 0; i < 65; i++) {
			sb.append('-');
		}
		sb.append('\n');
		{
			String linkName = "Summary";
			String iOk = String.valueOf(iOkT);
			String iFa = String.valueOf(iFaT);
			String oOk = String.valueOf(oOkT);
			String oFa = String.valueOf(oFaT);
			String recv = b2s(brT);
			String sent = b2s(bsT);
			sb.append("|");
			sb.append(linkName);
			for (int i = linkName.length(); i < 19; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(iOk);
			for (int i = iOk.length(); i < 5; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(iFa);
			for (int i = iFa.length(); i < 5; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(oOk);
			for (int i = oOk.length(); i < 5; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(oFa);
			for (int i = oFa.length(); i < 5; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(recv);
			for (int i = recv.length(); i < 9; i++) {
				sb.append(' ');
			}
			sb.append("|");
			sb.append(sent);
			for (int i = sent.length(); i < 9; i++) {
				sb.append(' ');
			}
			sb.append("|\n");
		}
		for (int i = 0; i < 65; i++) {
			sb.append('-');
		}
		sb.append('\n');
		return sb.toString();
	}

	private String b2s(int bytes) {
		String format = "%4.2f %s";
		String type = (bytes > 1024) ? (bytes > 1048576) ? (bytes > 1073741824) ? "Gb"
				: "Mb"
				: "Kb"
				: "B";
		float byts = bytes
				/ ((bytes > 1024) ? (bytes > 1048576) ? (bytes > 1073741824) ? 1073741824.0f
						: 1048576.0f
						: 1024.0f
						: 1.0f);
		return String.format(format, byts, type).replace(',', '.');
	}

	@Override
	public void init(StatPoster poster) {

	}
}
