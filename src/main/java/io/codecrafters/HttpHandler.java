package io.codecrafters;

/**
 * @author Kevin Lee
 */
@FunctionalInterface
public interface HttpHandler {
	void handle(HttpContext httpContext);
}
