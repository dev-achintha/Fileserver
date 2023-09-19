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
    ArrayList<String> clientList = new ArrayList<>();


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
            serverGUI.appendText(databaseHandler.status());
            while (running) {
                Socket clientSocket = serverSocket.accept();
                if(clientList.isEmpty() || !clientList.contains(clientSocket.getInetAddress().getHostAddress())) {
                    clientList.add(clientSocket.getInetAddress().getHostAddress());
                    serverGUI.appendText("New connection from " + clientSocket.getInetAddress().getHostAddress());
                }

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);
                clientHandler.start();

                serverGUI.setConnectedClients(clientHandlers.size());
            }
        } catch (IOException e) {
            serverGUI.appendText("Listening on port " + portNumber + " stopped");
        }
    }

    private void handleClientUpload(String fileName, byte[] fileData) {
        int userId = 1;
        databaseHandler.insertFile(userId, fileName, fileData);
    }

    static void handleClientFetchFiles(PrintWriter out) {
        ArrayList<String> files = databaseHandler.fetchFiles();
        // out.println("Fetching details of files: " + files.size());
        // out.println("_CATCH_LIST_FILES_START");
        for (int i = 0; i < files.size(); i++) {
            String file = files.get(i);
            out.println(file);
        }
        // out.println("_CATCH_LIST_FILES_END");
    }

    public void stopServer() {
        running = false;
        try {
            serverSocket.close();
            for (ClientHandler handler : clientHandlers) {
                handler.close();
            }
            clientHandlers.clear();
            clientList.clear();
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
