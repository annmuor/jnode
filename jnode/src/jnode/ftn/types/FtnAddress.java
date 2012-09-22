package jnode.ftn.types;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.main.Main;

/**
 * 
 * @author kreon
 * 
 */
public class FtnAddress implements Serializable {

	private static final long serialVersionUID = 1L;
	protected int zone;
	protected int net;
	protected int node;
	protected int point;

	public FtnAddress(String addr) {
		Pattern p = Pattern
				.compile("^(\\d)?:?(\\d{1,5})/(\\d{1,5})\\.?(\\d{1,5})?@?(\\S+)?$");
		Matcher m = p.matcher(addr);
		if (m.matches()) {
			if (m.group(1) != null && m.group(1).length() > 0) {
				zone = new Integer(m.group(1));
			} else {
				zone = Main.info.getAddress().zone;
			}
			net = new Integer(m.group(2));
			node = new Integer(m.group(3));
			if (m.group(4) != null && m.group(4).length() > 0) {
				point = new Integer(m.group(4));
			} else {
				point = 0;
			}
		} else {
			throw new NumberFormatException();
		}
	}

	public FtnAddress() {
		zone = Main.info.getAddress().zone;
		net = 0;
		node = 0;
		point = 0;
	}

	@Override
	public String toString() {
		return (point > 0) ? String.format("%d:%d/%d.%d", zone, net, node,
				point) : String.format("%d:%d/%d", zone, net, node);
	}

	public String intl() {
		return String.format("%d:%d/%d", zone, net, node);
	}

	public String topt() {
		if (point != 0) {
			return String.format("\001TOPT %d\r", point);
		} else {
			return "";
		}
	}

	public String fmpt() {
		if (point != 0) {
			return String.format("\001FMPT %d\r", point);
		} else {
			return "";
		}
	}

	public short getZone() {
		return (short) zone;
	}

	public short getNet() {
		return (short) net;
	}

	public short getNode() {
		return (short) node;
	}

	public short getPoint() {
		return (short) point;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public void setNet(int net) {
		this.net = net;
	}

	public void setNode(int node) {
		this.node = node;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + net;
		result = prime * result + node;
		result = prime * result + point;
		result = prime * result + zone;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FtnAddress))
			return false;
		FtnAddress other = (FtnAddress) obj;
		if (net != other.net)
			return false;
		if (node != other.node)
			return false;
		if (point != other.point)
			return false;
		if (zone != other.zone)
			return false;
		return true;
	}

	public boolean isPointOf(FtnAddress boss) {
		if (boss.zone == this.zone) {
			if (boss.net == this.net) {
				if (boss.node == this.node) {
					if (boss.point != this.point) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public FtnAddress clone() {
		FtnAddress n = new FtnAddress();
		n.zone = zone;
		n.net = net;
		n.node = node;
		n.point = point;
		return n;
	}

}
