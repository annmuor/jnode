package integration;

import org.jnode.rest.core.Http;
import org.junit.Test;
import rest.RestResult;

import static methods.Guest.login;
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
}
