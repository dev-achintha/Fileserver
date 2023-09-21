package server;
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

    public static void handleClientFetchFiles(ClientHandler clientHandler) {
        ArrayList<String> files = databaseHandler.fetchFiles();
        for (String file : files) {
            clientHandler.send(file);
        }
        clientHandler.send("_CATCH_LIST_FILES_END");
    }
    

    public void stopServer() {
        running = false;
        try {
            serverSocket.close();
            System.out.println("serverSocket.close() => JServer => public void stopServer()");
            for (ClientHandler handler : clientHandlers) {
                handler.closeStreams();
            }
            clientHandlers.clear();
            clientList.clear();
            serverGUI.setConnectedClients(0);
            databaseHandler.close();
            System.out.println("databaseHandler.close()");
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
