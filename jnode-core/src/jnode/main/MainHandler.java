/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
		return "jNode ver. " + DefaultVersion.getSelf();
	}

}
