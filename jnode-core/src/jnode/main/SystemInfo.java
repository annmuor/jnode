package jnode.main;

import java.util.ArrayList;
import java.util.List;

import jnode.ftn.types.FtnAddress;

/**
 * 
 * @author kreon
 * 
 */
public final class SystemInfo {
	private static final String INFO_SYSOP = "info.sysop";
	private static final String INFO_LOCATION = "info.location";
	private static final String INFO_STATIONNAME = "info.stationname";
	private static final String INFO_NDL = "info.ndl";
	private static final String INFO_ADDRESS = "info.address";
	private static final String INFO_DEFAULT_ZONE = "info.zone";
	private String sysop;
	private String location;
	private String stationName;
	private List<FtnAddress> addressList;
	private Integer zone;
	private String NDL;

	public SystemInfo(MainHandler handler) {
		sysop = handler.getProperty(INFO_SYSOP, "Nobody");
		location = handler.getProperty(INFO_LOCATION, "Nowhere");
		stationName = handler.getProperty(INFO_STATIONNAME, "Noname");
		NDL = handler.getProperty(INFO_NDL, "MO,TCP,BINKP");
		zone = new Integer(handler.getProperty(INFO_DEFAULT_ZONE, "2"));

		String[] addra = handler.getProperty(INFO_ADDRESS, "2:9999/9999")
				.replaceAll("[^\\/0-9,:\\.]", "").split(",");
		addressList = new ArrayList<FtnAddress>();
		for (String address : addra) {
			addressList.add(new FtnAddress(address));
		}

	}

	public String getSysop() {
		return sysop;
	}

	public String getLocation() {
		return location;
	}

	public String getStationName() {
		return stationName;
	}

	public List<FtnAddress> getAddressList() {
		return addressList;
	}

	public String getNDL() {
		return NDL;
	}

	public Integer getZone() {
		return zone;
	}
}