import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        var port = 4221;

        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);

            // Wait for connection from client.
            try (var clientSocket = serverSocket.accept();
                 var outputStream = clientSocket.getOutputStream()) {

                outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        } catch (IOException ioException) {
            System.out.println("IOException: " + ioException.getMessage());
        }
    }
}
