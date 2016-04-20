package rest;

public class GuestLoginRestCommand extends RestCommandAbstract {
    @Override
    protected String url() {
        return "http://localhost:4567/api/unsecure";
    }
}
