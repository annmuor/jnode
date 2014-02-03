package org.jnode.httpd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import jnode.ftn.FtnTools;

public class HTML {
	private static final int MAX_SIZE = 65535;
	private static String header = null;
	private static String footer = null;
	private static String menu = null;
	private static String secureMenu = null;
	private static String externalPath;

	private StringBuilder data;

	private HTML() {
		if (header == null) {
			header = String.format(getContents("/parts/header.html"), FtnTools
					.getPrimaryFtnAddress().toString());
		}
		if (footer == null) {
			footer = getContents("/parts/footer.html");
		}
		if (menu == null) {
			menu = getContents("/parts/menu.html");
		}
		if (secureMenu == null) {
			secureMenu = getContents("/parts/secure_menu.html");
		}
		data = new StringBuilder();
	}

	public HTML header() {
		data.append(header);
		return this;
	}

	public HTML menu() {
		data.append(menu);
		return this;
	}

	public HTML footer() {
		data.append(footer);
		return this;
	}

	public HTML secureMenu() {
		data.append(secureMenu);
		return this;
	}

	public static HTML start(boolean secure) {
		HTML html = new HTML();
		html.header().menu();
		if (secure) {
			html.secureMenu();
		}
		return html;
	}

	public String get() {
		return data.toString();
	}

	public HTML append(String html) {
		data.append(html);
		return this;
	}

	@SuppressWarnings("resource")
	public static String getContents(String path) {
		// check for external
		InputStream is = null;
		if (externalPath != null) {
			File s = new File(externalPath + File.separator + path);
			try {
				is = new FileInputStream(s);
			} catch (FileNotFoundException e) {
			}
		}
		if (is == null) {
			String search = "www" + path;
			is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(search);
		}
		byte[] buf = new byte[MAX_SIZE];
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			try {
				int n = 0;
				do {
					n = is.read(buf);
					if (n > 0) {
						sb.append(new String(buf, 0, n, "UTF-8"));
					}
				} while (n > 0);
				is.close();
			} catch (IOException e) {
			}
			return sb.toString();
		} else {
		}
		return "";
	}

	public static void setExternalPath(String externalPath) {
		HTML.externalPath = externalPath;
	}

}
