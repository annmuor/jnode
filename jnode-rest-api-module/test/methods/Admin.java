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

    public static RestResult create(String token, String linkName, String linkAddress, String paketPassword, String protocolPassword,
                                    String protocolAddress, int protocolPort){
        RestCommand cmd = new SecureRestCommand(token, String.format("{\n" +
                "    \"method\": \"link.create\",\n" +
                "    \"params\": {\n" +
                "        \"linkName\": \"%s\",\n" +
                "        \"linkAddress\": \"%s\",\n" +
                "        \"paketPassword\": \"%s\",\n" +
                "        \"protocolPassword\": \"%s\",\n" +
                "        \"protocolAddress\": \"%s\",\n" +
                "        \"protocolPort\": %d\n" +
                "\n" +
                "    },\n" +
                "    \"id\": %d,\n" +
                "    \"jsonrpc\": \"2.0\"\n" +
                "}", linkName, linkAddress, paketPassword, protocolPassword, protocolAddress, protocolPort, next()));
        return cmd.execute();
    }

}
