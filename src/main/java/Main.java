import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

public class Main {
  
  public static void main(String[] args) {
      System.out.println("Logs from your program will appear here!");
      int port = 6379;
      for (int i = 0; i < args.length; i++) {
          if (args[i].equals("--port")) {
              if (i + 1 >= args.length) {
                  throw new IllegalArgumentException("Missing port number");
              }
              port = Integer.parseInt(args[i + 1]);
              break;
          }
      }
      try (ServerSocket serverSocket = new ServerSocket(port)) {
          serverSocket.setReuseAddress(true);
          while (true) {
              Socket clientSocket = serverSocket.accept();
              new Thread(() -> {
                  try {
                      ClientHandler.handleClient(clientSocket);
                  } catch (TimeoutException ex) {
                      System.getLogger(Main.class.getName())
                          .log(System.Logger.Level.ERROR, (String) null, ex);
                  }
              }).start();
          }
      } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
      }
  }

}