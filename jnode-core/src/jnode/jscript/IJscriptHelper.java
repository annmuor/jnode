package jnode.jscript;

public abstract class IJscriptHelper {
	public abstract class Version {
		public abstract int getMajor();

		public abstract int getMinor();

		@Override
		public String toString() {
			return String.format("v.%d.%d", getMajor(), getMinor());
		}
	}

	public abstract Version getVersion();

	public String toString() {
		return String.format("%s %s", getClass().getSimpleName(), getVersion()
				.toString());
	}
}
