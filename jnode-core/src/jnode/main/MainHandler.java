package jnode.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Хранение конфигурации
 * 
 * @author kreon
 * 
 */
public class MainHandler {

	private Properties config;
	private static MainHandler instance = null;
	private SystemInfo info;

	MainHandler(String configFile) throws IOException {
		this.config = new Properties();
		this.config.load(new FileInputStream(configFile));
		MainHandler.instance = this;
		info = new SystemInfo(this);
	}

	public String getProperty(String property, String def) {
		return config.getProperty(property, def);
	}

	public boolean haveProperty(String property) {
		return config.containsKey(property);
	}

	public boolean getBooleanProperty(String property, boolean def) {
		return new Boolean(getProperty(property, new Boolean(def).toString()));
	}

	public int getIntegerProperty(String property, int def) {
		return new Integer(getProperty(property, new Integer(def).toString()));
	}

	public static MainHandler getCurrentInstance() {
		return instance;
	}

	public SystemInfo getInfo() {
		return info;
	}

	public static String getVersion() {
		return "";
	}

}
