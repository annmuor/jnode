package rest;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class RestResult {
    private final int httpCode;
    private final JSONRPC2Response payload;

    public RestResult(int httpCode, JSONRPC2Response payload) {
        this.httpCode = httpCode;
        this.payload = payload;
    }

    public static RestResult fail(int code){
        return new RestResult(code, null);
    }

    public int getHttpCode() {
        return httpCode;
    }

    public JSONRPC2Response getPayload() {
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
