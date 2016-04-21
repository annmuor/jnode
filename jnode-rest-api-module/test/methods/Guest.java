package methods;

import rest.RestCommand;
import rest.RestResult;
import rest.UnsecureRestCommand;

import static methods.Seq.next;

public final class Guest {
    private Guest() {
    }

    public static RestResult login(String username){
        RestCommand guestLoginCmd = new UnsecureRestCommand(String.format("{\n" +
                "    \"method\": \"guest.login\",\n" +
                "    \"params\": {\n" +
                "        \"login\": \"%s\"\n" +
                "    },\n" +
                "    \"id\": %d,\n" +
                "    \"jsonrpc\": \"2.0\"\n" +
                "}", username, next()));
        return guestLoginCmd.execute();
    }
}
