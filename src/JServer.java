import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class JServer extends Thread {
    private ServerSocket serverSocket;
    private int portNumber;
    private List<ClientHandler> clientHandlers;
    ServerGUI serverGUI;
    private boolean running;
    private static DatabaseHandler databaseHandler;

    public JServer(int portNumber, ServerGUI serverGUI) {
        this.portNumber = portNumber;
        this.serverGUI = serverGUI;
        this.clientHandlers = new ArrayList<>();
        this.databaseHandler = new DatabaseHandler(); 
    }

    public void run() {
        running = true;
        try {
            serverSocket = new ServerSocket(portNumber);
            serverGUI.appendText("Server is running and listening on port " + portNumber);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                serverGUI.appendText("New connection from " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);
                clientHandler.start();

                serverGUI.setConnectedClients(clientHandlers.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientUpload(String fileName, byte[] fileData) {
        int userId = 1;
        databaseHandler.insertFile(userId, fileName, fileData);
    }

    static void handleClientFetchFiles(PrintWriter out) {
        ArrayList<String> files = databaseHandler.fetchFiles();
        out.println("FILES " + files.size());
        for (String file : files) {
            out.println(file);
        }
    }

    public void stopServer() {
        running = false;
        try {
            serverSocket.close();
            for (ClientHandler handler : clientHandlers) {
                handler.close();
            }
            clientHandlers.clear();
            serverGUI.setConnectedClients(0);
            databaseHandler.close();
            serverGUI.appendText("Server stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        serverGUI.setConnectedClients(clientHandlers.size());
    }
}

