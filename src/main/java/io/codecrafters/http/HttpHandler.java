package io.codecrafters.http;

/**
 * @author Kevin Lee
 */
@FunctionalInterface
public interface HttpHandler {

	void handle(HttpContext httpContext);

}
