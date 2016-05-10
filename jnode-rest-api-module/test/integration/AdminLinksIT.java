package integration;

import methods.Admin;
import net.minidev.json.JSONArray;
import org.jnode.rest.core.Http;
import org.junit.Test;
import rest.RestResult;

import java.util.Map;

import static methods.User.login;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class AdminLinksIT {
    @Test
    public void linksList() throws Exception {

        final String token = getToken();
        RestResult restResult = Admin.list(token);


        assertThat(restResult, is(notNullValue()));
        assertThat(restResult.getHttpCode(), is(Http.OK));
        assertThat(restResult.getPayload(), is(notNullValue()));
        assertThat(restResult.getPayload().getResult(), is(instanceOf(JSONArray.class)));

        JSONArray res = (JSONArray) restResult.getPayload().getResult();

        assertThat(res.size(), is(3));
        assertThat(res.get(0), is(is(instanceOf(Map.class))));

        Map<String, Object> item = (Map<String, Object>) res.get(0);
        assertThat(item.get("paketPassword"), is(equalTo("111111")));

    }

    @Test
    public void linkCreate() throws Exception {
        final String token = getToken();

        RestResult restResult = Admin.create(token, "toster", "2:5020/828.971", "pwd", "pwd2", "example.net", 24554);

        assertThat(restResult, is(notNullValue()));
        assertThat(restResult.getHttpCode(), is(Http.OK));
        assertThat(restResult.getPayload(), is(notNullValue()));
        assertThat(restResult.getPayload(), is(equalTo("Okay")));

        System.out.println(restResult);

    }

    private String getToken() {
        return (String) login("2:5020/828.17", "111111").getPayload().getResult();
    }
}
