package rest;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import org.jnode.rest.core.Http;
import org.jnode.rest.core.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class RestCommandAbstract implements RestCommand {

    private static final String MEDIA_TYPE = "application/json; charset=UTF-8";

    protected RestCommandAbstract(String json) {
        this.json = json;
    }

    protected abstract String url();
    private final String json;

    protected void beforeConnect(HttpURLConnection conn){
    }

    @Override
    public RestResult execute() {

        try {
            URL url = new URL(url());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", MEDIA_TYPE);
            beforeConnect(conn);
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestMethod("POST");
            conn.connect();

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();

            try {
                final int responseCode = conn.getResponseCode();
                if (responseCode != Http.OK){
                    String errPayload = IOUtils.readFullyAsString(conn.getErrorStream(), "UTF-8");
                    if (errPayload != null){
                        JSONRPC2Response resp = JSONRPC2Response.parse(errPayload);
                        return new RestResult(responseCode, resp);
                    } else {
                        return new RestResult(responseCode, null);
                    }
                }
                String payload = IOUtils.readFullyAsString(conn.getInputStream(), "UTF-8");
                JSONRPC2Response resp = JSONRPC2Response.parse(payload);
                return new RestResult(responseCode, resp);
            } catch (JSONRPC2ParseException e) {
                e.printStackTrace();
                return RestResult.fail(-2);
            } finally {
                conn.disconnect();
            }


        } catch (IOException e) {
            e.printStackTrace();
            return RestResult.fail(-1);
        }


    }


}
