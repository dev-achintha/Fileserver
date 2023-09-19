import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerGUI {
    private JFrame frame;
    private JTextArea textArea;
    private JButton startButton;
    private JButton stopButton;
    private JLabel connectedClientsLabel;
    private JServer server;

    public ServerGUI() {
        frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocation(1000, 300);
        frame.setAlwaysOnTop(true);

        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        buttonPanel.add(startButton);
        stopButton = new JButton("Stop");
        buttonPanel.add(stopButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        connectedClientsLabel = new JLabel("Connected Clients: 0");
        frame.add(connectedClientsLabel, BorderLayout.NORTH);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        frame.setVisible(true);
    }

    private void startServer() {
        server = new JServer(5002, this);
        server.start();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopServer() {
        if (server != null) {
            server.stopServer();
            server = null;
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public void appendText(String text) {
        textArea.append(text + "\n");
    }

    public void setConnectedClients(int count) {
        connectedClientsLabel.setText("Connected Clients: " + count);
    }

    public static void main(String[] args) {
        ServerGUI servergui = new ServerGUI();
        servergui.startServer();
    }
}
