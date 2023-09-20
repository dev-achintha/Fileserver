package client;

import javax.swing.*;

import server.JServer;

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
    private JButton handShakeBtn;
    private int lineNumber;

    public ClientGUI() {
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setLocation(680, 100);
        frame.setAlwaysOnTop(true);

        textArea = new JTextArea();
        textArea.setEditable(false);

        uploadButton = new JButton("Upload File");
        handShakeBtn = new JButton("Say Hi");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(uploadButton);
        buttonPanel.add(handShakeBtn);

        connectionIndicator = new JLabel();
        connectionIndicator.setPreferredSize(new Dimension(20, 20));

        filePanel = new JPanel();
        filePanel.setSize(400, 400);

        textArea.add(new JButton("Clear Log"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filePanel, new JScrollPane(textArea));
        splitPane.setResizeWeight(0.5);
        frame.add(splitPane, BorderLayout.CENTER);

        frame.add(connectionIndicator, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        try {
            if (connectToServer()) {

                updateFileList(fetchFiles(in));
                ;

            }
            setConnectionStatus();
        } catch (Exception e) {
            connectionIndicator.setBackground(Color.RED);
            connectionIndicator.setText("DISCONNECTED");
            connectionIndicator.setOpaque(true);
        }

        actionListeners();


    }

    void actionListeners() {
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    sendFile(selectedFile);
                }
            }
        });

        handShakeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println("Hi");
            }
        });
    }

    public void appendText(String text) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        textArea.append("[" + timeStamp + "] " + text + "\n");
    }

    public void setConnectionStatus() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) == null) {
                connectionIndicator.setBackground(Color.GREEN);
                connectionIndicator.setText("CONNECTED");
                connectionIndicator.setOpaque(true);
            }
            connectionIndicator.setOpaque(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> fetchFiles(BufferedReader in) {
        out.println("FETCH_FILES");
        String inputLine;
        ArrayList<String> catchFiles = new ArrayList<String>();
        try {
            while ((inputLine = in.readLine()) != null) {
                textArea.append("RECEIVED: File name => " + inputLine);
                catchFiles.add(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            out.println("UPLOAD " + file.getName());
            out.println(Base64.getEncoder().encodeToString(fileData));
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

    public void append(String text) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        textArea.append("[" + timeStamp + "] " + text + "\n");
    }

    public static void main(String[] args) {
        new ClientGUI();

    }
}
