package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler extends Thread {
    private Socket socket;
    private JServer server;
    protected static PrintWriter out;
    protected BufferedReader in;

    public ClientHandler(Socket socket, JServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            String inputLine;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                server.serverGUI.appendText(
                        "Received from " + socket.getInetAddress().getHostAddress() + ": " + inputLine);
                if (inputLine.startsWith("FETCH_FILES")) {
                    JServer.handleClientFetchFiles(out);
                    server.serverGUI.appendText(">>"+inputLine);
                } else {
                    server.serverGUI.appendText(inputLine);
                }
            }
        } catch (IOException e) {
        } finally {
            closeStreams();
        }
    }

    protected void closeStreams() {
        try {
            if (out != null) {
                out.close();
                System.out.println("out.close() => protected void closeStreams()");
            }
            if (in != null) {
                // in().close();
                System.out.println("in.close() => protected void closeStreams()");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("socket.close() => protected void closeStreams()");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
