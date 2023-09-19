import javax.swing.*;
import javax.swing.text.BadLocationException;

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
    private DefaultListModel<String> fileListModel;
    private JList<String> fileList;
    private JLabel connectionIndicator; 
    private ClientConnectionChecker connectionChecker; 

    public ClientGUI() {
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocation(1080, 100);
        frame.setAlwaysOnTop(true);

        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        connectionIndicator = new JLabel();
        connectionIndicator.setPreferredSize(new Dimension(20, 20));
        frame.add(connectionIndicator, BorderLayout.NORTH);

        createGUI();
        frame.setVisible(true);

        connectionChecker = new ClientConnectionChecker(this); // Added
        new Thread(connectionChecker).start();

    }

    private void createGUI() {
        JButton uploadButton = new JButton("Upload File");
        JButton handShakeBtn = new JButton("Say Hi");
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
                out.println("HI");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(uploadButton);
        buttonPanel.add(handShakeBtn);
        frame.add(buttonPanel, BorderLayout.SOUTH);        

        JPanel filePanel = new JPanel(new BorderLayout());
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        filePanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        frame.add(filePanel, BorderLayout.EAST);
    }

    public void setConnectionStatus(boolean isConnected) {
        if (isConnected) {
            connectionIndicator.setBackground(Color.GREEN);
        } else {
            connectionIndicator.setBackground(Color.RED);
        }
        connectionIndicator.setOpaque(true);
    }

    private void fetchFiles() {
        out.println("FETCH_FILES");
    }

    public void updateFileList(ArrayList<String> files) {
        fileListModel.clear();
        for (String file : files) {
            fileListModel.addElement(file);
        }
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

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 5002);
            textArea.append("Connection to server successful.."+"\n");
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                textArea.append(serverResponse + "\n");
            }
        } catch (IOException e) {
            textArea.append("Connection to server failed.."+"\n");
            // e.printStackTrace();
        }
    }

    public void append(String text) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        textArea.append("[" + timeStamp + "] " + text + "\n");
    }

    public static void main(String[] args) {
        ClientGUI clientGui = new ClientGUI();
        System.out.println(0);
        clientGui.connectToServer();

    }
}
