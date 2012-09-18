package jnode.ftn;

/**
 * 
 * @author kreon
 * 
 */
public class Ftn2D {
	private int net;
	private int node;

	public int getNet() {
		return net;
	}

	public void setNet(int net) {
		this.net = net;
	}

	public int getNode() {
		return node;
	}

	public void setNode(int node) {
		this.node = node;
	}

	public Ftn2D() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Ftn2D(int net, int node) {
		super();
		this.net = net;
		this.node = node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + net;
		result = prime * result + node;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ftn2D other = (Ftn2D) obj;
		if (net != other.net)
			return false;
		if (node != other.node)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%d/%d", net, node);
	}

}
