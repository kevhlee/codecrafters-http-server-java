import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {
        var port = 4221;

        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);

            // Wait for connection from client.
            try (var clientSocket = serverSocket.accept()) {
                System.out.println("accepted new connection");
            }
        } catch (IOException ioException) {
            System.out.println("IOException: " + ioException.getMessage());
        }
    }
}
