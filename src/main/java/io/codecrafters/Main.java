package io.codecrafters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        var port = 4221;

        try (var serverSocket = new ServerSocket(port);
             var executorService = Executors.newWorkStealingPool(4)) {

            serverSocket.setReuseAddress(true);

            while (true) {
                executorService.submit(new ConnectionHandler(serverSocket.accept()));
            }
        } catch (IOException ioException) {
            System.out.println("IOException: " + ioException.getMessage());
        }
    }

    private static void handleRequest(Socket socket) throws IOException {
        var inputStream = socket.getInputStream();
        var outputStream = socket.getOutputStream();

        var httpRequest = parseHttpRequest(inputStream);
        var httpResponse = new HttpResponse(HttpStatus.BAD_REQUEST);

        httpResponse.setHeader("content-type", "text/plain");
        httpResponse.setHeader("content-length", 0);

        if (httpRequest == null) {
            sendResponse(outputStream, httpResponse);
            return;
        }

        switch (httpRequest.getMethod()) {
            case HttpRequest.METHOD_GET -> {
                var path = httpRequest.getPath();
                if (path.equals("/")) {
                    httpResponse.setStatus(HttpStatus.OK);
                } else if (path.equals("/user-agent")) {
                    httpResponse.setStatus(HttpStatus.OK);
                    if (httpRequest.containsHeader("user-agent")) {
                        var body = httpRequest.getHeader("user-agent");
                        httpResponse.setHeader("content-length", body.length());
                        httpResponse.setBody(body);
                    }
                } else if (path.startsWith("/echo")) {
                    httpResponse.setStatus(HttpStatus.OK);
                    if (path.length() > 5) {
                        var body = path.substring(6);
                        httpResponse.setHeader("content-length", body.length());
                        httpResponse.setBody(body);
                    }
                } else {
                    httpResponse.setStatus(HttpStatus.NOT_FOUND);
                }
            }
        }

        sendResponse(outputStream, httpResponse);
    }

    private static HttpRequest parseHttpRequest(InputStream inputStream) throws IOException {
        var lines = readLinesCRLF(inputStream);
        if (lines.isEmpty()) {
            return null;
        }

        var iterator = lines.iterator();
        var requestLine = iterator.next().split("\\s+");
        if (requestLine.length != 3) {
            return null;
        }

        var httpRequest = new HttpRequest(requestLine[0], requestLine[1], requestLine[2]);
        while (iterator.hasNext()) {
            var line = iterator.next();
            var index = line.indexOf(':');
            if (index == -1) {
                continue;
            }

            var key = line.substring(0, index);
            var value = line.substring(index + 1);

            httpRequest.setHeader(key.strip(), value.strip());
        }
        return httpRequest;
    }

    private static List<String> readLinesCRLF(InputStream inputStream) throws IOException {
        var sb = new StringBuilder();
        var lines = new ArrayList<String>();

        for (int b = inputStream.read(); b >= 0; b = inputStream.read()) {
            if (b == '\r') {
                b = inputStream.read();
                if (b == '\n') {
                    if (sb.length() == 0) {
                        break;
                    }
                    lines.add(sb.toString());
                    sb.setLength(0);
                } else {
                    // TODO: Is this a valid request at this point?
                    sb.append('\r');
                    sb.append((char) b);
                }
            } else {
                sb.append((char) b);
            }
        }

        return lines;
    }

    private static void sendResponse(OutputStream outputStream, HttpResponse httpResponse) throws IOException {
        var sb = new StringBuilder();

        sb.append(HTTP_VERSION);
        sb.append(" ");
        sb.append(httpResponse.getStatus().getCode());
        sb.append(" ");
        sb.append(httpResponse.getStatus().getText());
        sb.append(NEWLINE_CRLF);

        for (var header : httpResponse.getHeaders().entrySet()) {
            sb.append(header.getKey());
            sb.append(": ");
            sb.append(header.getValue());
            sb.append(NEWLINE_CRLF);
        }

        sb.append(NEWLINE_CRLF);

        if (httpResponse.getBody() != null) {
            sb.append(httpResponse.getBody());
        }

        outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    private record ConnectionHandler(Socket socket) implements Runnable {
        @Override
        public void run() {
            try (socket) {
                handleRequest(socket);
            } catch (IOException ioException) {
                System.out.println("IOException: " + ioException.getMessage());
            }
        }
    }

    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String NEWLINE_CRLF = "\r\n";
}
