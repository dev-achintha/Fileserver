package client;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date; //

public class ClientGUI {
    private JFrame frame;
    private static JTextArea textArea;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private JLabel connectionIndicator;
    private JPanel filePanel;
    private JButton uploadButton;
    private JButton downloaButton;
    private JButton deleteButton;
    private JButton clearLogButton;
    private JList<String> fileList;
    private Font listFont;

    public ClientGUI() {
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 600);
        frame.setLocation(0, 0);
        frame.setAlwaysOnTop(false);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GRAY);

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
        filePanel.setLayout(new BorderLayout());
        filePanel.setPreferredSize(new Dimension(400, 400));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filePanel, new JScrollPane(textArea));
        splitPane.setResizeWeight(0.5);
        frame.add(splitPane, BorderLayout.CENTER);

        Font customFont = new Font("Arial", Font.PLAIN, 16);
        textArea.setFont(customFont);
        uploadButton.setFont(customFont);
        downloaButton.setFont(customFont);
        deleteButton.setFont(customFont);
        clearLogButton.setFont(customFont);
        connectionIndicator.setFont(customFont);
        listFont = new Font("Arial", Font.PLAIN, 16);
        splitPane.setUI(new BasicSplitPaneUI() {
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    public int getDividerSize() {
                        return 10;
                    }
                };
            }
        });

        frame.add(connectionIndicator, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        actionListeners();
        try {
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

        downloaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    sendToServer("DOWNLOAD " + selectedFile);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    int confirm = JOptionPane.showConfirmDialog(frame,
                            "Are you sure you want to delete " + selectedFile + "?", "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        sendToServer("DELETE " + selectedFile);
                    }
                }
            }
        });

        clearLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
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
                return in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        appendText("Receiving :( " + null);
        return null;
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

    private void processMessage(String message) {
        if (message.startsWith("DELETE_CONFIRM")) {
            String[] parts = message.split(" ", 2);
            String fileName = parts[1];
            appendText(fileName + " has been deleted.");
            updateFileList(fetchFiles());
        } else if (message.startsWith("DOWNLOAD ")) {
            String fileName = message.substring(9);
            String base64Data = receiveFromServer();
            receiveFileData(fileName, base64Data);
        } else if (message.startsWith("COMPLETE_UPLOAD_MSG_")) {
            String notification = message.substring(20);
            appendText(notification);
            updateFileList(fetchFiles());
        } else if (message.startsWith("NOT_COMPLETE_UPLOAD_MSG_")) {
            String notification = message.substring(24);
            appendText(notification);
        }
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

    private ArrayList<String> fetchFiles() {
        sendToServer("_FETCH_FILES");
        String inputLine;

        ArrayList<String> catchFiles = new ArrayList<String>();
        while ((inputLine = receiveFromServer()) != null) {
            if (inputLine.startsWith("_CATCH_LIST_FILES_END")) {
                break;
            }
            appendText("RECEIVED: File name => " + inputLine);
            catchFiles.add(inputLine);
        }
        return catchFiles;
    }

    public void updateFileList(ArrayList<String> files) {
        if (files == null) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DefaultListModel<String> fileListModel = new DefaultListModel<>();
                for (String file : files) {
                    fileListModel.addElement(file);
                }
                fileList = new JList<>(fileListModel);
                fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                JScrollPane scrollPane = new JScrollPane(fileList);
                fileList.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                            String selectedFile = fileList.getSelectedValue();
                            if (selectedFile != null) {
                                int confirm = JOptionPane.showConfirmDialog(frame,
                                        "Are you sure you want to delete " + selectedFile + "?", "Confirm Deletion",
                                        JOptionPane.YES_NO_OPTION);
                                if (confirm == JOptionPane.YES_OPTION) {
                                    sendToServer("DELETE " + selectedFile);
                                }
                            }
                        }
                    }
                });
                filePanel.removeAll();
                filePanel.add(scrollPane, BorderLayout.CENTER);
                filePanel.revalidate();
                filePanel.repaint();
                fileList.setFont(listFont);
            }
        });
    }

    public void sendFile(File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            sendToServer("UPLOAD " + file.getName());
            sendToServer(Base64.getEncoder().encodeToString(fileData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveFileData(String fileName, String base64Data) {
        byte[] fileData = Base64.getDecoder().decode(base64Data);

        FileDialog fileDialog = new FileDialog(frame, "Save File", FileDialog.SAVE);
        fileDialog.setFile(fileName);
        fileDialog.setVisible(true);

        String selectedFile = fileDialog.getFile();
        String selectedDirectory = fileDialog.getDirectory();

        if (selectedFile != null && selectedDirectory != null) {
            String filePath = selectedDirectory + selectedFile;

            try {
                FileOutputStream fos = new FileOutputStream(filePath);
                fos.write(fileData);
                fos.close();
                appendText("File saved: " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void appendText(String text) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        textArea.append("[" + timeStamp + "] " + text + "\n");
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
