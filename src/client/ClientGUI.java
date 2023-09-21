package client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

public class ClientGUI {
    private JFrame frame;
    private static JTextArea textArea;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private JLabel connectionIndicator;
    // private ClientConnectionChecker connectionChecker;
    private JPanel filePanel;
    private JButton uploadButton;
    private JButton downloaButton;
    private JButton deleteButton;
    private JButton clearLogButton;

    public ClientGUI() {
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setLocation(680, 100);
        // frame.setAlwaysOnTop(true);

        textArea = new JTextArea();
        textArea.setEditable(false);

        uploadButton = new JButton("Upload File");
        downloaButton = new JButton("Download");
        deleteButton = new JButton("Delete");
        clearLogButton = new JButton("Clear Log");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloaButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearLogButton);

        connectionIndicator = new JLabel();
        connectionIndicator.setPreferredSize(new Dimension(20, 20));

        filePanel = new JPanel();
        filePanel.setSize(400, 400);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filePanel, new JScrollPane(textArea));
        splitPane.setResizeWeight(0.5);
        frame.add(splitPane, BorderLayout.CENTER);

        frame.add(connectionIndicator, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        actionListeners();
        try {
            // if (connectToServer()) {

            //     updateFileList(fetchFiles());
            //     ;

            // }
            socket = new Socket("localhost", 5002);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            updateFileList(fetchFiles());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        new Thread(this::listenForMessages).start();
        new Thread(new ConnectionChecker()).start();

    }

    void actionListeners() {
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileDialog fileDialog = new FileDialog(frame, "Select File");
                fileDialog.setMode(FileDialog.LOAD);
                fileDialog.setVisible(true);
        
                String filename = fileDialog.getFile();
                String directory = fileDialog.getDirectory();
        
                if (filename != null && directory != null) {
                    File selectedFile = new File(directory + filename);
                    sendFile(selectedFile);
                }
            }
        });
    }

    public void sendToServer(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String receiveFromServer() {
        try {
            if (in != null) {
                // appendText("Receiving "+in.readLine());
                return in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        appendText("Receiving :( "+null);
        return null;
    }

    public void appendText(String text) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        textArea.append("[" + timeStamp + "] " + text + "\n");
    }

    public void setConnectionStatus(boolean isConnected) {
        if (isConnected) {
            connectionIndicator.setBackground(Color.GREEN);
            connectionIndicator.setText("CONNECTED");
        } else {
            connectionIndicator.setBackground(Color.RED);
            connectionIndicator.setText("DISCONNECTED");
        }
        connectionIndicator.setOpaque(true);
    }

    private void listenForMessages() {
        try {
            while (true) {
                String inputLine = in.readLine();
                if (inputLine != null) {
                    processMessage(inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String message) {
        
    }

    
    private ArrayList<String> fetchFiles() {
        sendToServer("_FETCH_FILES");
        String inputLine;

        ArrayList<String> catchFiles = new ArrayList<String>();
        while ((inputLine = receiveFromServer()) != null) {
            if(inputLine.startsWith("_CATCH_LIST_FILES_END")){
                break;
            }
            appendText("RECEIVED: File name => " + inputLine);
            catchFiles.add(inputLine);
        }
        return catchFiles;
    }    

    public void updateFileList(ArrayList<String> files) {
        DefaultListModel<String> fileListModel = new DefaultListModel<>();
        for (String file : files) {
            fileListModel.addElement(file);
        }
        JList<String> fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(fileList);
        filePanel.setLayout(new BorderLayout());
        filePanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void sendFile(File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            sendToServer("UPLOAD " + file.getName());
            // System.out.println("UPLOAD " + file.getName());
            sendToServer(Base64.getEncoder().encodeToString(fileData));
            // System.out.println(Base64.getEncoder().encodeToString(fileData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean connectToServer() {
        try {
            socket = new Socket("localhost", 5002);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private class ConnectionChecker implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
    
                    if (socket == null || !socket.isConnected()) {
                        setConnectionStatus(false);
                        boolean reconnected = connectToServer();
                        if (reconnected) {
                            setConnectionStatus(true);
                        }
                    } else {
                        setConnectionStatus(true);
                    }
                } catch (InterruptedException e) {
                    setConnectionStatus(false);
                }
            }
        }
    }
    

    public static void main(String[] args) {
        new ClientGUI();

    }
}
