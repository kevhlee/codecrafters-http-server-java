package io.codecrafters.http;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kevin Lee
 */
public class HttpRequest {
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";

	public HttpRequest(String method, String path, String httpVersion) {
		this.httpVersion = httpVersion;
		this.method = method;
		this.path = path;
	}

	public String getBody() {
		return body;
	}

	public String getHeader(String key) {
		return headers.get(key.toLowerCase());
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

	protected Map<String, String> getHeaders() {
		return headers;
	}

	protected void setBody(String body) {
		this.body = body;
	}

	protected void setHeader(String key, String value) {
		headers.put(key.toLowerCase(), value);
	}

	private String body;
	private final Map<String, String> headers = new HashMap<>();
	private final String httpVersion;
	private final String method;
	private final String path;
}
