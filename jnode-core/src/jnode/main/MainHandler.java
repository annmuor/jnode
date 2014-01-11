package jnode.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import jnode.install.DefaultVersion;

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

	public MainHandler(String configFile) throws IOException {
		this.config = new Properties();
		this.config.load(new FileInputStream(configFile));
		MainHandler.instance = this;
		info = new SystemInfo(this);
	}

	public MainHandler(Properties properties) {
		this.config = properties;
		MainHandler.instance = this;
		info = new SystemInfo(this);
	}

	public void setProperty(String key, String value) {
		config.setProperty(key, value);
	}

	public String getProperty(String property, String def) {
		return config.getProperty(property, def);
	}

	public boolean haveProperty(String property) {
		return config.containsKey(property);
	}

	public boolean getBooleanProperty(String property, Boolean def) {
		String value = getProperty(property, def.toString());
		try {
			int x = Integer.valueOf(value);
			if (x > 0) {
				return true;
			}
		} catch (NumberFormatException ignore) {
		}
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
		return "jNode ver. " + new DefaultVersion().toString();
	}

}
