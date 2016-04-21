package integration;

import methods.Echomail;
import methods.User;
import org.junit.Test;
import rest.RestResult;

public class UserPostIT {
    @Test
    public void userPost() throws Exception {

        RestResult loginResult = User.login("2:5020/828.17", "111111");
        String token = (String) loginResult.getPayload().getResult();

        RestResult restResult = Echomail.post(token, "828.local", "субж", "бодя",
               "Kirill Temnenkov", "All++", "2:5020/828.17", "fff", "origggin");
        System.out.println(restResult);

    }
}
