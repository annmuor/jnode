package org.jnode.httpd.filters;

import java.util.HashMap;

import jnode.ftn.FtnTools;
import jnode.orm.ORMManager;

import org.jnode.httpd.dto.WebAdmin;
import org.jnode.httpd.util.Base64Util;

import spark.Filter;
import spark.Request;
import spark.Response;

public class SecureFilter extends Filter {
	private HashMap<String, Boolean> cache = new HashMap<>();

	public SecureFilter() {
		super();
	}

	public SecureFilter(String path, String acceptType) {
		super(path, acceptType);
	}

	public SecureFilter(String path) {
		super(path);
	}

	@Override
	public void handle(Request req, Response resp) {
		boolean authenticated = false;
		String authBase64 = req.headers("Authorization");
		if (authBase64 != null) {
			Boolean test = cache.get(authBase64);
			if (test != null && test.booleanValue()) {
				authenticated = true;
			} else {
				try {
					String authText = new String(Base64Util.decode(authBase64
							.split(" ")[1]));
					String[] creds = authText.split(":");
					WebAdmin admin = ORMManager.get(WebAdmin.class)
							.getFirstAnd("username", "=", creds[0]);
					if (admin != null) {
						String password = FtnTools.md5(creds[1]);
						if (password.equals(admin.getPassword())) {
							authenticated = true;
							cache.put(authBase64, authenticated);
						}
					}
				} catch (RuntimeException e) {
				}
			}
		}
		if (!authenticated) {
			resp.header("WWW-Authenticate",
					"Basic realm=\"Secure area for operators only\"");
			halt(401);
		}
	}

}
