package rest;

import java.net.HttpURLConnection;
import java.util.Base64;

public class BrokenSecureRestCommand extends RestCommandAbstract {
    private final String token;

    public BrokenSecureRestCommand(String token, String json) {
        super(json);
        this.token = token;
    }

    @Override
    protected void beforeConnect(HttpURLConnection conn) {
        super.beforeConnect(conn);

        String basicAuth = "Basic broken" + new String(Base64.getEncoder().encode(token.getBytes()));
        conn.setRequestProperty ("Authorization", basicAuth);    }

    @Override
    protected String url() {
        return "http://localhost:4567/api/secure";
    }
}
