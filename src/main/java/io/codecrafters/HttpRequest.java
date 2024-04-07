package io.codecrafters;

public record HttpRequest(String method, String path, String httpVersion) {
    public static final String METHOD_GET = "GET";
}
