package cz.lidinsky.spinel;

import java.net.Socket;

public class TestClient {

  public static void main(String[] args) throws Exception {
    System.out.println("test start");
    try (
        Socket socket = new Socket("localhost", 12340);
        SpinelInputStream is = new SpinelInputStream(socket.getInputStream());
        SpinelOutputStream os = new SpinelOutputStream(socket.getOutputStream());
        ) {
      socket.setSoTimeout(5000);
      SpinelMessage request = new SpinelMessage(12, 20);
      os.write(request);
      SpinelMessage response = is.readMessage();
      System.out.println(response.toString());
      Thread.sleep(1000);
      request = new SpinelMessage(50, 20);
      os.write(request);
      response = is.readMessage();
      System.out.println(response.toString());
      Thread.sleep(1000);
      socket.close();
    } finally {
      System.out.println("test stop");
    }

  }

}
