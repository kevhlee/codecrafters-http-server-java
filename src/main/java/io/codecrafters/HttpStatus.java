package io.codecrafters;

/**
 * @author Kevin Lee
 */
public enum HttpStatus {
	//
	// 2xx - Success
	//

	OK(200, "OK"),
	CREATED(201, "Created"),

	//
	// 4xx - Client error
	//

	BAD_REQUEST(400, "Bad Request"),
	NOT_FOUND(404, "Not Found"),

	//
	// 5xx - Server error
	//

	INTERNAL_SERVER_ERROR(500, "Internal Server Error");

	HttpStatus(int code, String text) {
		this.code = code;
		this.text = text;
	}

	public int getCode() {
		return code;
	}

	public String getText() {
		return text;
	}

	private final int code;
	private final String text;
}
