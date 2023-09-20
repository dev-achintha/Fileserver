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
    private JLabel connectionIndicator;
    private ClientConnectionChecker connectionChecker;
    private JPanel filePanel;

    public ClientGUI() {
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setLocation(680, 100);
        frame.setAlwaysOnTop(true);

        textArea = new JTextArea();
        textArea.setEditable(false);
        
        JButton uploadButton = new JButton("Upload File");
        JButton handShakeBtn = new JButton("Say Hi");
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(uploadButton);
        buttonPanel.add(handShakeBtn);
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
        
        connectionIndicator = new JLabel();
        connectionIndicator.setPreferredSize(new Dimension(20, 20));
        
        filePanel = new JPanel();
        filePanel.setSize(400, 400);
        filePanel.setBackground(Color.BLUE);

        textArea.add(new JButton("Clear Log"));
        
        frame.add(new JScrollPane(textArea), BorderLayout.EAST);
        frame.add(connectionIndicator, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(filePanel, BorderLayout.CENTER);

        // frame.pack();
        frame.setVisible(true);

        connectionChecker = new ClientConnectionChecker(this); // Added
        new Thread(connectionChecker).start();

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

    private void fetchFiles() {
        out.println("FETCH_FILES");
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

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 5002);
            textArea.append("Connection to server successful.." + "\n");
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            fetchFiles();
            String inputLine;
            ArrayList<String> catchFiles = new ArrayList<String>();
            while ((inputLine = in.readLine()) != null) {
                catchFiles.add(inputLine);
            }
            updateFileList(catchFiles);
            
            // String serverResponse;
            // while ((serverResponse = in.readLine()) != null) {
            // textArea.append(serverResponse + "\n");
            // }
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public void append(String text) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        textArea.append("[" + timeStamp + "] " + text + "\n");
    }

    public static void main(String[] args) {
        ClientGUI clientGui = new ClientGUI();
        clientGui.connectToServer();

    }
}
