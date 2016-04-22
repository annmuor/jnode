package integration;

import org.jnode.rest.core.Http;
import org.jnode.rest.core.RPCError;
import org.junit.Test;
import rest.RestCommand;
import rest.RestResult;
import rest.UnsecureRestCommand;

import static methods.Guest.login;
import static methods.Seq.next;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class GuestLoginIT {

    @Test
    public void guestLogin() throws Exception {
        RestResult restResult = login("guest1");

        assertThat(restResult, is(notNullValue()));
        assertThat(restResult.getHttpCode(), is(Http.OK));
        assertThat(restResult.getPayload(), is(notNullValue()));
        assertThat(restResult.getPayload().getResult(), is(instanceOf(String.class)));
    }

    @Test
    public void userEmptyLogin() throws Exception {
        RestResult restResult = login("");

        System.out.println(restResult);
        assertThat(restResult, is(notNullValue()));
        assertThat(restResult.getHttpCode(), is(Http.OK));
        assertThat(restResult.getPayload(), is(notNullValue()));
        assertThat(restResult.getPayload().getError(), is(notNullValue()));
        assertThat(restResult.getPayload().getError().getCode(), is(RPCError.CODE_EMPTY_LOGIN));
    }

    @Test
    public void userNullLogin() throws Exception {

        RestCommand guestLoginCmd = new UnsecureRestCommand(String.format("{\n" +
                "    \"method\": \"guest.login\",\n" +
                "    \"params\": {\n" +
                "        \"login\": null\n" +
                "    },\n" +
                "    \"id\": %d,\n" +
                "    \"jsonrpc\": \"2.0\"\n" +
                "}", next()));
        RestResult restResult = guestLoginCmd.execute();


        System.out.println(restResult);
        assertThat(restResult, is(notNullValue()));
        assertThat(restResult.getHttpCode(), is(Http.OK));
        assertThat(restResult.getPayload(), is(notNullValue()));
        assertThat(restResult.getPayload().getError(), is(notNullValue()));
        assertThat(restResult.getPayload().getError().getCode(), is(RPCError.CODE_INVALID_PARAMS));
    }
}
