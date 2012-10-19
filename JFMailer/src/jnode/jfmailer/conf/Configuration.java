package jnode.jfmailer.conf;

import jnode.ftn.types.FtnAddress;

public enum Configuration {
	INSTANSE;
	public static final String PREFS_NAME = "JFMAILER";
	public static final String SYSOP = "SYS";
	public static final String LOCATION = "LOC";
	public static final String LOCAL = "LOCAL";
	public static final String REMOTE = "REMOTE";
	public static final String REMOTE_HOST = "REMOTEHOST";
	public static final String REMOTE_PORT = "REMOTEPORT";
	public static final String REMOTE_PASW = "REMOTEPASW";

	private final String NDL = "9600,TCP,BINKP,MO";
	private final String version = "JFMailer/0.2";
	private final String system = "Android";
	private String sysop = "John Smith";
	private String location = "Europe";
	private FtnAddress local = new FtnAddress("2:999/999.99");
	private FtnAddress remote = new FtnAddress("2:999/999");
	private String remoteHost = "f999.n999.z2.binkp.net";
	private Integer remotePort = 24554;
	private String password = "Secr3tP4ssW0rD";

	public String getSysop() {
		return sysop;
	}

	public void setSysop(String sysop) {
		this.sysop = sysop;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public FtnAddress getLocal() {
		return local;
	}

	public void setLocal(FtnAddress local) {
		this.local = local;
	}

	public FtnAddress getRemote() {
		return remote;
	}

	public void setRemote(FtnAddress remote) {
		this.remote = remote;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public Integer getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(Integer remotePort) {
		this.remotePort = remotePort;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNDL() {
		return NDL;
	}

	public String getVersion() {
		return version;
	}

	public String getSystem() {
		return system;
	}

}
