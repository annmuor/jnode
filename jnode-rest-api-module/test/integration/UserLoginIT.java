package integration;

import org.jnode.rest.core.Http;
import org.junit.Test;
import rest.RestCommand;
import rest.RestResult;
import rest.UnsecureRestCommand;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class UserLoginIT {
    @Test
    public void userLogin() throws Exception {
         String json = "{\n" +
                 "    \"method\": \"user.login\",\n" +
                 "    \"params\": {\n" +
                 "        \"login\": \"2:5020/828.17\",\n" +
                 "        \"password\": \"111111\"\n" +
                 "    },\n" +
                 "    \"id\": 123,\n" +
                 "    \"jsonrpc\": \"2.0\"\n" +
                 "}";

        RestCommand userLoginCmd = new UnsecureRestCommand(json);
        RestResult restResult = userLoginCmd.execute();

        assertThat(restResult, is(notNullValue()));
        assertThat(restResult.getHttpCode(), is(Http.OK));
        assertThat(restResult.getPayload(), is(notNullValue()));
        assertThat(restResult.getPayload().getResult(), is(instanceOf(String.class)));

    }
}
