package integration;

import org.jnode.rest.core.Http;
import org.junit.Test;
import rest.GuestLoginRestCommand;
import rest.RestCommand;
import rest.RestResult;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class GuestLoginIT {

    @Test
    public void login() throws Exception {
        String json = "{\n" +
                "    \"method\": \"guest.login\",\n" +
                "    \"params\": {\n" +
                "        \"login\": \"1\"\n" +
                "    },\n" +
                "    \"id\": 123,\n" +
                "    \"jsonrpc\": \"2.0\"\n" +
                "}";
        RestCommand guestLoginCmd = new GuestLoginRestCommand(json);
        RestResult restResult = guestLoginCmd.execute();

        assertThat(restResult, is(notNullValue()));
        assertThat(restResult.getHttpCode(), is(Http.OK));
        assertThat(restResult.getPayload(), is(notNullValue()));
        assertThat(restResult.getPayload().getResult(), is(instanceOf(String.class)));


    }
}
