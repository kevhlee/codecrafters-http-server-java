package io.codecrafters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author Kevin Lee
 */
public class HttpServer {
	public HttpServer(Path baseDirPath, int port) {
		this.baseDirPath = baseDirPath;
		this.port = port;
	}

	public HttpServer setExecutorService(Supplier<ExecutorService> supplier) {
		executorServiceSupplier = supplier;
		return this;
	}

	public void setHandler(String path, HttpHandler handler) {
		if (path == null || path.isBlank()) {
			throw new IllegalArgumentException("Invalid path: '" + path + "'");
		}
		handlers.put(path, handler);
	}

	public HttpServer setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public void start() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(port);
			 ExecutorService executorService = executorServiceSupplier.get()) {

			serverSocket.setReuseAddress(true);
			serverSocket.setSoTimeout(timeout);

			LOGGER.info("HTTP server started");

			while (true) {
				Socket socket = serverSocket.accept();

				executorService.submit(() -> {
					try (socket) {
						handleRequest(socket);
					} catch (Exception exception) {
						LOGGER.severe(exception.getMessage());
					}
				});
			}
		} catch (SocketTimeoutException socketTimeoutException) {
			LOGGER.info("HTTP server shut down");
		}
	}

	private HttpResponse createHttpResponse(HttpStatus httpStatus) {
		HttpResponse httpResponse = new HttpResponse(httpStatus);
		httpResponse.setHeader(HttpHeaders.CONNECTION, "close");
		httpResponse.setHeader(HttpHeaders.CONTENT_LENGTH, "0");
		return httpResponse;
	}

	private HttpHandler getHttpHandler(String path) {
		// TODO: Use an actual path matcher that handles globs

		HttpHandler httpHandler = handlers.get(path);
		if (httpHandler != null) {
			return httpHandler;
		}

		while (!path.isEmpty()) {
			httpHandler = handlers.get(path + "/**");
			if (httpHandler != null) {
				return httpHandler;
			}

			int index = path.lastIndexOf('/');
			if (index == -1) {
				break;
			}
			path = path.substring(0, index);
		}

		return null;
	}

	private void handleRequest(Socket socket) throws IOException {
		HttpRequest httpRequest = parseHttpRequest(socket.getInputStream());
		HttpResponse httpResponse = createHttpResponse(HttpStatus.NOT_FOUND);

		if (httpRequest != null) {
			HttpHandler httpHandler = getHttpHandler(httpRequest.getPath());
			if (httpHandler != null) {
				httpResponse.setStatus(HttpStatus.OK);
				httpHandler.handle(new HttpContext(baseDirPath, httpRequest, httpResponse));
			}
		} else {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST);
		}

		sendHttpResponse(socket.getOutputStream(), httpResponse);
	}

	private HttpRequest parseHttpRequest(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String requestLine = bufferedReader.readLine();
		if (requestLine == null || requestLine.isEmpty()) {
			return null;
		}

		String[] requestParts = requestLine.split("\\s+");
		if (requestParts.length != 3) {
			return null;
		}

		HttpRequest httpRequest = new HttpRequest(
			requestParts[0], requestParts[1], requestParts[2]);

		String headerLine;
		while ((headerLine = bufferedReader.readLine()) != null) {
			if (headerLine.isEmpty()) {
				break;
			}

			String[] headerParts = headerLine.split(":\\s*");
			httpRequest.setHeader(headerParts[0], headerParts[1]);
		}

		StringBuilder body = new StringBuilder();
		while (bufferedReader.ready()) {
			body.append((char) bufferedReader.read());
		}
		httpRequest.setBody(body.toString());

		return httpRequest;
	}

	private void sendHttpResponse(OutputStream outputStream, HttpResponse httpResponse)
		throws IOException {

		String body = httpResponse.getBody();
		HttpStatus status = httpResponse.getStatus();
		Map<String, Object> headers = httpResponse.getHeaders();

		BufferedWriter bufferedWriter =
			new BufferedWriter(new OutputStreamWriter(outputStream));

		bufferedWriter.append("HTTP/1.1 ");
		bufferedWriter.append(String.valueOf(status.getCode()));
		bufferedWriter.append(" ");
		bufferedWriter.append(status.getText());
		bufferedWriter.append("\r\n");

		for (Map.Entry<String, Object> header : headers.entrySet()) {
			bufferedWriter.append(header.getKey());
			bufferedWriter.append(": ");
			bufferedWriter.append(String.valueOf(header.getValue()));
			bufferedWriter.append("\r\n");
		}

		bufferedWriter.append("\r\n");

		if (body != null) {
			bufferedWriter.append(body);
		}

		bufferedWriter.flush();
	}

	private static final Logger LOGGER = Logger.getLogger(HttpServer.class.getName());

	private final Path baseDirPath;
	private Supplier<ExecutorService> executorServiceSupplier = Executors::newWorkStealingPool;
	private final Map<String, HttpHandler> handlers = new HashMap<>();
	private final int port;
	private int timeout;
}
