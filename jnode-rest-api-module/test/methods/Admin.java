package methods;

import rest.RestCommand;
import rest.RestResult;
import rest.SecureRestCommand;

import static methods.Seq.next;

public final class Admin {
    private Admin() {
    }

    public static RestResult list(String token){
        RestCommand cmd = new SecureRestCommand(token, String.format("{\n" +
                "    \"method\": \"link.list\",\n" +
                "    \"params\": {\n" +
                "    },\n" +
                "    \"id\": %d,\n" +
                "    \"jsonrpc\": \"2.0\"\n" +
                "}", next()));
        return cmd.execute();
    }
}
