package io.codecrafters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var port = 4221;

        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);

            try (var clientSocket = serverSocket.accept()) {
                handleRequest(clientSocket);
            }
        } catch (IOException ioException) {
            System.out.println("IOException: " + ioException.getMessage());
        }
    }

    private static void handleRequest(Socket socket) throws IOException {
        try (var inputStream = socket.getInputStream();
             var outputStream = socket.getOutputStream()) {

            var httpRequest = parseHttpRequest(inputStream);
            var httpResponse = new HttpResponse(HttpStatus.BAD_REQUEST);

            if (httpRequest != null) {
                switch (httpRequest.method()) {
                    case HttpRequest.METHOD_GET -> {
                        var path = httpRequest.path();
                        if (path.equals("/")) {
                            httpResponse.setStatus(HttpStatus.OK);
                        } else {
                            httpResponse.setStatus(HttpStatus.NOT_FOUND);
                        }
                    }
                }
            }

            sendResponse(outputStream, httpResponse);
        }
    }

    private static HttpRequest parseHttpRequest(InputStream inputStream) throws IOException {
        var lines = readLinesCRLF(inputStream);
        if (lines.isEmpty()) {
            return null;
        }

        var requestLine = lines.getFirst().split("\\s+");
        if (requestLine.length != 3) {
            return null;
        }
        return new HttpRequest(requestLine[0], requestLine[1], requestLine[2]);
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
        sb.append(NEWLINE_CRLF);

        outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String NEWLINE_CRLF = "\r\n";
}
