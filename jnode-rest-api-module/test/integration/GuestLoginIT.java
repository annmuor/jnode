package integration;

import org.junit.Test;
import rest.GuestLoginRestCommand;
import rest.RestCommand;
import rest.RestResult;

public class GuestLoginIT {

    @Test
    public void login() throws Exception {
        RestCommand guestLoginCmd = new GuestLoginRestCommand();
        RestResult restResult = guestLoginCmd.execute();
        System.out.println(restResult);
    }
}
