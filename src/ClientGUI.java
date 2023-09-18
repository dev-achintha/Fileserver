import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;

public class ClientGUI {
    private JFrame frame;
    private JTextArea textArea;
    private Socket socket;
    private static PrintWriter out;
    private DefaultListModel<String> fileListModel;
    private JList<String> fileList;

    public ClientGUI() {
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        createGUI();

        frame.setVisible(true);
    }

    private void createGUI() {
        JButton uploadButton = new JButton("Upload File");
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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(uploadButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);        

        JPanel filePanel = new JPanel(new BorderLayout());
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        filePanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        frame.add(filePanel, BorderLayout.EAST);
    }

    private static void fetchFiles() {
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
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                textArea.append(serverResponse + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ClientGUI client = new ClientGUI();
        client.connectToServer();
        fetchFiles();

    }
}
