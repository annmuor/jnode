package rest;

public class RestResult {
    private final int httpCode;
    private final String payload;

    public RestResult(int httpCode, String payload) {
        this.httpCode = httpCode;
        this.payload = payload;
    }

    public static RestResult fail(int code){
        return new RestResult(code, null);
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "RestResult{" +
                "httpCode=" + httpCode +
                ", payload='" + payload + '\'' +
                '}';
    }
}
