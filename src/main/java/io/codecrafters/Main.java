package io.codecrafters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

/**
 * @author Kevin Lee
 */
public class Main {
	public static void main(String[] args) throws IOException {
		Path baseDirPath = Path.of("./");
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--directory") && i < args.length - 1) {
				baseDirPath = Path.of(args[i + 1]);
				break;
			}
		}

		HttpServer httpServer = new HttpServer(baseDirPath, 4221)
			.setExecutorService(Executors::newVirtualThreadPerTaskExecutor)
			.setTimeout(60000);

		httpServer.setHandler("/", (httpContext) -> {
			httpContext.setResponseStatus(HttpStatus.OK);
		});

		httpServer.setHandler("/echo/**", (httpContext) -> {
			String requestPath = httpContext.getRequestPath();
			int index = requestPath.indexOf("/echo/");
			if (index != -1) {
				String body = requestPath.substring(index + 6);
				httpContext.setResponseBody(body);
				httpContext.setResponseHeader("Content-Length", body.length());
				httpContext.setResponseHeader("Content-Type", "text/plain");
			}
		});

		httpServer.setHandler("/user-agent", (httpContext) -> {
			String body = httpContext.getRequestHeader("User-Agent");
			httpContext.setResponseBody(body);
			httpContext.setResponseHeader("Content-Length", body.length());
			httpContext.setResponseHeader("Content-Type", "text/plain");
		});

		httpServer.setHandler("/files/**", new HttpHandler() {
			@Override
			public void handle(HttpContext httpContext) {
				String requestPath = httpContext.getRequestPath();
				int index = requestPath.indexOf("files/");
				if (index == -1) {
					httpContext.setResponseStatus(HttpStatus.NOT_FOUND);
					return;
				}

				String fileName = requestPath.substring(index + 6);
				switch (httpContext.getRequestMethod()) {
					case HttpRequest.METHOD_GET -> handleGet(httpContext, fileName);
					case HttpRequest.METHOD_POST -> handlePost(httpContext, fileName);
				}
			}

			private void handleGet(HttpContext httpContext, String fileName) {
				File file = httpContext.getFile(fileName);
				if (!file.exists()) {
					httpContext.setResponseStatus(HttpStatus.NOT_FOUND);
					return;
				}

				try {
					String body = Files.readString(file.toPath());
					httpContext.setResponseBody(body);
					httpContext.setResponseHeader("Content-Length", body.length());
					httpContext.setResponseHeader("Content-Type", "application/octet-stream");
					httpContext.setResponseStatus(HttpStatus.OK);
				} catch (IOException ioException) {
					httpContext.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

			private void handlePost(HttpContext httpContext, String fileName) {
				String body = httpContext.getRequestBody();
				try {
					httpContext.writeFile(fileName, body);
					httpContext.setResponseStatus(HttpStatus.CREATED);
				} catch (IOException ioException) {
					httpContext.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		});

		httpServer.start();
	}
}
