import java.io.IOException;
import java.net.Socket;

public class ClientConnectionChecker implements Runnable {
    private ClientGUI clientGUI;
    private int i;

    public ClientConnectionChecker(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    public void run() {
        while (true) {
            try {
                Socket testSocket = new Socket("localhost", 5002);
                testSocket.close();
                clientGUI.setConnectionStatus(true);
            } catch (IOException e) {
                clientGUI.setConnectionStatus(false);
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
