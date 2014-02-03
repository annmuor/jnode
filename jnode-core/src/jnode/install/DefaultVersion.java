package jnode.install;

import java.util.Date;

import jnode.dto.Version;

public class DefaultVersion extends Version {
	private static DefaultVersion self;

	public static Version getSelf() {
		if (self == null) {
			synchronized (DefaultVersion.class) {
				self = new DefaultVersion();
			}
		}
		return self;
	}

	private DefaultVersion() {
		setMajorVersion(1L);
		setMinorVersion(5L);
		setInstalledAt(new Date());
	}

	@Override
	public String toString() {
		return String.format("%d.%d", getMajorVersion(), getMinorVersion());
	}

}
