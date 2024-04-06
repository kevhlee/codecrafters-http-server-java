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
        var inputStream = socket.getInputStream();
        var outputStream = socket.getOutputStream();

        var lines = readLinesCRLF(inputStream);
        if (lines.isEmpty()) {
            sendResponse(outputStream, HttpStatus.BAD_REQUEST);
            return;
        }

        var requestLine = lines.getFirst().split("\\s+");
        if (requestLine.length != 3) {
            sendResponse(outputStream, HttpStatus.BAD_REQUEST);
            return;
        }

        var requestMethod = requestLine[0];
        var requestPath = requestLine[1];

        switch (requestMethod) {
            case "GET":
                if (!requestPath.equals("/")) {
                    sendResponse(outputStream, HttpStatus.NOT_FOUND);
                    return;
                }
                break;

            default:
                sendResponse(outputStream, HttpStatus.BAD_REQUEST);
                break;
        }

        sendResponse(outputStream, HttpStatus.OK);
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

    private static void sendResponse(OutputStream outputStream, HttpStatus status) throws IOException {
        var response = String.format("HTTP/1.1 %d %s\r\n\r\n", status.getCode(), status.getText());
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }
}
