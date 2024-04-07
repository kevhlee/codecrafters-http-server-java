package io.codecrafters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    public HttpResponse(HttpStatus status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public Map<String, Object> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public Object getHeader(String key) {
        return headers.get(key);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setHeader(String key, Object value) {
        headers.put(key, value);
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    private HttpStatus status;
    private final Map<String, Object> headers = new HashMap<>();
    private String body;
}
