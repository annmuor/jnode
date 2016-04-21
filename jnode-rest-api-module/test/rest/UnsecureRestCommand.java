package rest;

public class UnsecureRestCommand extends RestCommandAbstract {
    public UnsecureRestCommand(String json) {
        super(json);
    }

    @Override
    protected String url() {
        return "http://localhost:4567/api/unsecure";
    }
}
