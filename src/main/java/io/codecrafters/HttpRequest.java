package io.codecrafters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    public static final String METHOD_GET = "GET";

    public HttpRequest(String method, String path, String httpVersion) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
    }

    public boolean containsHeader(String key) {
        return headers.containsKey(key.toLowerCase());
    }

    public String getHeader(String key) {
        return headers.get(key.toLowerCase());
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void setHeader(String key, String value) {
        headers.put(key.toLowerCase(), value);
    }

    private final String httpVersion;
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap();
}
