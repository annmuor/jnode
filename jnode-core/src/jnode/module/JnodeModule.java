package jnode.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import jnode.event.IEventHandler;

/**
 * 
 * @author kreon
 * 
 */
public abstract class JnodeModule implements IEventHandler {
	protected Properties properties;

	public JnodeModule(String configFile) throws JnodeModuleException {
		File config = new File(configFile);
		if (!(config.exists() && config.canRead())) {
			throw new JnodeModuleException("Config file " + configFile
					+ " unavailable");
		}
		properties = new Properties();
		try {
			properties.load(new FileInputStream(config));
		} catch (IOException e) {
			throw new JnodeModuleException("Failed to load properties", e);
		}
	}

	public abstract void start();
}
