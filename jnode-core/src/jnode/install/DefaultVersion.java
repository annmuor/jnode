package jnode.install;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		setMinorVersion(1L);
		setInstalledAt(new Date());
	}

	@Override
	public String toString() {
		return String.format("%d.%d", getMajorVersion(), getMinorVersion());
	}



	public static List<String> updateFromVersion(Version ver) {
		List<String> ret = new ArrayList<>();
		if(ver.equals("1.0")) {
			ret.add("ALTER TABLE netmail ADD last_modified BIGINT NOT NULL DEFAULT 0;");
			ver.setMinorVersion(1L);
		}
//		if(ver.equals("1.1")) {
//			System.out.println("upgrading from version 1.1");
//		}
		return ret;
		
	}
}
