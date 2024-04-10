package io.codecrafters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Kevin Lee
 */
public class HttpContext {
	protected HttpContext(Path baseDirPath, HttpRequest httpRequest, HttpResponse httpResponse) {
		this.baseDirPath = baseDirPath;
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
	}

	//
	// Request
	//

	public String getRequestBody() {
		return httpRequest.getBody();
	}

	public String getRequestMethod() {
		return httpRequest.getMethod();
	}

	public String getRequestHeader(String key) {
		return httpRequest.getHeader(key);
	}

	public String getRequestPath() {
		return httpRequest.getPath();
	}

	//
	// Response
	//

	public void setResponseBody(String body) {
		httpResponse.setBody(body);
	}

	public void setResponseHeader(String key, Object value) {
		httpResponse.setHeader(key, value);
	}

	public void setResponseStatus(HttpStatus httpStatus) {
		httpResponse.setStatus(httpStatus);
	}

	//
	// I/O
	//

	public File getFile(String fileName) {
		return baseDirPath.resolve(fileName).toFile();
	}

	public void writeFile(String fileName, String content) throws IOException {
		Files.write(baseDirPath.resolve(fileName), content.getBytes());
	}

	private final Path baseDirPath;
	private final HttpRequest httpRequest;
	private final HttpResponse httpResponse;
}
