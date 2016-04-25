package integration;

import methods.Echomail;
import methods.User;
import net.minidev.json.JSONObject;
import org.jnode.rest.core.Http;
import org.jnode.rest.core.RPCError;
import org.junit.Test;
import rest.BrokenSecureRestCommand;
import rest.RestCommand;
import rest.RestResult;

import static methods.Guest.login;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class UserPostIT {
    @Test
    public void userPost() throws Exception {

        RestResult loginResult = User.login("2:5020/828.17", "111111");
        String token = (String) loginResult.getPayload().getResult();

        RestResult postResult = Echomail.post(token, "828.local", "субж", "бодя",
               "Kirill Temnenkov", "All++", "2:5020/828.117", "fff", "origggin");

        assertThat(postResult, is(notNullValue()));
        assertThat(postResult.getHttpCode(), is(Http.OK));
        assertThat(postResult.getPayload(), is(notNullValue()));
        assertThat(postResult.getPayload().getResult(), is(instanceOf(Long.class)));

        Long id = (Long) postResult.getPayload().getResult();

        RestResult getResult = Echomail.get(token, id);

        assertThat(getResult, is(notNullValue()));
        assertThat(getResult.getHttpCode(), is(Http.OK));
        assertThat(getResult.getPayload(), is(notNullValue()));
        assertThat(getResult.getPayload().getResult(), is(instanceOf(JSONObject.class)));

        JSONObject msg = (JSONObject) getResult.getPayload().getResult();

        assertThat(msg.get("area"), is(instanceOf(JSONObject.class)));
        assertThat(((JSONObject)msg.get("area")).get("name"), is(equalTo("828.local")));
        assertThat(msg.get("toName"), is(equalTo("All++")));
        assertThat(msg.get("subject"), is(equalTo("субж")));
        assertThat(msg.get("fromName"), is(equalTo("Kirill Temnenkov")));
        assertThat(msg.get("fromFTN"), is(equalTo("2:5020/828.17")));
        assertThat((String)msg.get("text"), is(containsString("бодя")));
        assertThat((String)msg.get("text"), is(containsString("fff")));
        assertThat((String)msg.get("text"), is(containsString("origggin")));

    }

    @Test
    public void unauthUserPost() throws Exception {
        RestResult postResult = Echomail.post("badToken", "828.local", "субж", "бодя",
                "Kirill Temnenkov", "All++", "2:5020/828.117", "fff", "origggin");
        assertThat(postResult, is(notNullValue()));
        assertThat(postResult.getHttpCode(), is(Http.NOT_AUTH));
    }

    @Test
    public void badEchoPost() throws Exception {

        RestResult loginResult = User.login("2:5020/828.17", "111111");
        String token = (String) loginResult.getPayload().getResult();

        RestResult postResult = Echomail.post(token, "828.nolocal", "субж", "бодя",
                "Kirill Temnenkov", "All++", "2:5020/828.117", "fff", "origggin");

        assertThat(postResult, is(notNullValue()));
        assertThat(postResult.getHttpCode(), is(Http.OK));
        assertThat(postResult.getPayload(), is(notNullValue()));
        assertThat(postResult.getPayload().getError(), is(notNullValue()));

        assertThat(postResult.getPayload().getError().getCode(), is(RPCError.CODE_ECHOAREA_NOT_FOUND));
    }

    @Test
    public void badAuth() throws Exception {

        RestCommand cmd = new BrokenSecureRestCommand("dummy", "dummy2");

        RestResult postResult = cmd.execute();

        assertThat(postResult, is(notNullValue()));
        assertThat(postResult.getHttpCode(), is(Http.BAD_REQUEST));
        assertThat(postResult.getPayload(), is(notNullValue()));
        assertThat(postResult.getPayload().getError(), is(notNullValue()));
        assertThat(postResult.getPayload().getError().getCode(), is(RPCError.CODE_BAD_AUTH_HEADER));
    }

    @Test
    public void accessDenied() throws Exception {

        RestResult loginResult = login("guest2");
        String token = (String) loginResult.getPayload().getResult();

        RestResult restResult = Echomail.post(token, "828.nolocal", "субж", "бодя",
                "Kirill Temnenkov", "All++", "2:5020/828.117", "fff", "origggin");

        System.out.println(restResult);
        assertThat(restResult, is(notNullValue()));
        assertThat(restResult.getHttpCode(), is(Http.OK));
        assertThat(restResult.getPayload(), is(notNullValue()));
        assertThat(restResult.getPayload().getError(), is(notNullValue()));
        assertThat(restResult.getPayload().getError().getCode(), is(RPCError.CODE_ACCESS_DENIED));
    }

    @Test
    public void nopointPost() throws Exception {


    }
}
