package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler extends Thread {
    private Socket socket;
    private JServer server;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, JServer server) {
        this.socket = socket;
        this.server = server;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String inputLine;
            while ((inputLine = receive()) != null) {
                server.serverGUI.appendText("Received from " + socket.getInetAddress().getHostAddress() + ": " + inputLine);
                if (inputLine.startsWith("_FETCH_FILES")) {
                    server.handleClientFetchFiles(this);
                    // out = null;
                } else {
                    server.serverGUI.appendText(inputLine);
                }
            }
        } finally {
            closeStreams();
        }
    }

    public void send(String message) {
        if (out != null) {
            server.serverGUI.appendText("Sending "+message);
            out.println(message);
        }
    }

    public String receive() {
        try {
            if (in != null) {
                return in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void closeStreams() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
