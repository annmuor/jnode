package rest;

public class GuestLoginRestCommand extends RestCommandAbstract {
    public GuestLoginRestCommand(String json) {
        super(json);
    }

    @Override
    protected String url() {
        return "http://localhost:4567/api/unsecure";
    }
}
