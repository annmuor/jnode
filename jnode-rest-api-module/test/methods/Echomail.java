package methods;

import rest.RestCommand;
import rest.RestResult;
import rest.SecureRestCommand;

import static methods.Seq.next;

public final class Echomail {
    private Echomail() {
    }

    public static RestResult post(String token, String echoarea, String subject,
                                  String body, String fromName, String toName, String fromFtn,
                                  String tearline, String origin){

        String json = String.format("{\n" +
                "    \"method\": \"echomail.post\",\n" +
                "    \"params\": {\n" +
                "        \"echoarea\": \"%s\",\n" +
                "        \"body\": \"%s\",\n" +
                "        \"subject\": \"%s\",\n" +
                "        \"fromName\": \"%s\",\n" +
                "        \"toName\": \"%s\",\n" +
                "        \"fromFTN\": \"%s\",\n" +
                "        \"tearline\": \"%s\",\n" +
                "        \"origin\": \"%s\"\n" +
                "\n" +
                "    },\n" +
                "    \"id\": %d,\n" +
                "    \"jsonrpc\": \"2.0\"\n" +
                "}", echoarea, body, subject, fromName, toName, fromFtn, tearline, origin, next());

        RestCommand cmd = new SecureRestCommand(token, json);
        return cmd.execute();

    }
}
