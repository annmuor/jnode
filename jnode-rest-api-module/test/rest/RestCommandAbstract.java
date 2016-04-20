package rest;

import org.jnode.rest.core.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class RestCommandAbstract implements RestCommand {

    private static final int HTTP_OK = 200;
    private static final String MEDIA_TYPE = "application/json; charset=UTF-8";

    protected abstract String url();

    @Override
    public RestResult execute() {

        try {
            URL url = new URL(url());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", MEDIA_TYPE);
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestMethod("POST");
            conn.connect();

            OutputStream os = conn.getOutputStream();
            String json = "{\n" +
                    "    \"method\": \"guest.login\",\n" +
                    "    \"params\": {\n" +
                    "        \"login\": \"1\"\n" +
                    "    },\n" +
                    "    \"id\": 123,\n" +
                    "    \"jsonrpc\": \"2.0\"\n" +
                    "}";
            os.write(json.getBytes("UTF-8"));
            os.close();



            try {
                final int responseCode = conn.getResponseCode();
                if (responseCode != HTTP_OK) {
                    return RestResult.fail(responseCode);
                }
                String payload = IOUtils.readFullyAsString(conn.getInputStream(), "UTF-8");
                return new RestResult(responseCode, payload);
            } finally {
                conn.disconnect();
            }


        } catch (IOException e) {
            e.printStackTrace();
            return RestResult.fail(-1);
        }


    }


}
