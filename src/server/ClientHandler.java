package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

import javax.swing.JOptionPane;

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
                server.serverGUI
                        .appendText("Received from " + socket.getInetAddress().getHostAddress() + ": " + inputLine);
                processMessage(inputLine);
            }
        } finally {
            closeStreams();
        }
    }

    private void processMessage(String message) {
        if (message.startsWith("DOWNLOAD ")) {
            String fileName = message.substring(9);
            sendFileToClient(fileName);
        } else if (message.startsWith("_FETCH_FILES")) {
            server.handleClientFetchFiles(this);
        } else if (message.startsWith("UPLOAD ")) {
            String fileName = message.substring(7);
            byte[] fileData = Base64.getDecoder().decode(receive());
            if(server.handleClientUpload(fileName, fileData)) {
                send("COMPLETE_UPLOAD_MSG_"+fileName+" successfully uploaded.");
            } else {
                send("NOT_COMPLETE_UPLOAD_MSG_"+fileName+" upload successful.");
            }
        } else if (message.startsWith("DELETE ")) {
            String fileName = message.substring(7);
            server.databaseHandler.deleteFile(fileName);
            send("DELETE_CONFIRM " + fileName);
        } else {
            server.serverGUI.appendText(message);
        }
    }

    public void sendFileToClient(String fileName) {
        byte[] fileData = server.databaseHandler.getFileData(fileName);
        if (fileData != null) {
            send("DOWNLOAD " + fileName); // Send the file name to client
            send(Base64.getEncoder().encodeToString(fileData)); // Send the file data to client
        }
    }

    public void send(String message) {
        if (out != null) {
            server.serverGUI.appendText("Sending " + message);
            out.println(message);
            server.serverGUI.appendText("Sent " + message);
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
