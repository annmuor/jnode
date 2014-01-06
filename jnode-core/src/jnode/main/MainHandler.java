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

	private final Properties config;
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

	public boolean getBooleanProperty(String property, Boolean def) {
		String value = getProperty(property, def.toString());
		return (value.matches("^([tT][rR][uU][eE]|1)$"));
	}

	public int getIntegerProperty(String property, int def) {
		return new Integer(getProperty(property, Integer.toString(def)));
	}

	public static MainHandler getCurrentInstance() {
		return instance;
	}

	public SystemInfo getInfo() {
		return info;
	}

	public static String getVersion() {
		return "jNode 0.6-SNAPSHOT";
	}

}
