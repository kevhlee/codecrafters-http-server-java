package io.codecrafters.http;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Kevin Lee
 */
public class HttpResponse {
	public HttpResponse(HttpStatus httpStatus) {
		status = httpStatus;
	}

	public String getBody() {
		return body;
	}

	public Object getHeader(String key) {
		return headers.get(key);
	}

	public Map<String, Object> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setHeader(String key, Object value) {
		headers.put(key.toLowerCase(), value);
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	private String body;
	private final Map<String, Object> headers = new TreeMap<>();
	private HttpStatus status;
}
