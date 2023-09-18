import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class JavaFileRunner extends JFrame {
    private JList<String> fileList;
    private JButton runButton;
    private JButton deleteButton;

    public JavaFileRunner(String[] fileNames) {
        setTitle("Java File Runner");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        fileList = new JList<>(fileNames);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    runSelectedFile();
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteClassFiles();
                }
            }
        });

        runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runSelectedFile();
            }
        });

        deleteButton = new JButton("Delete .class Files");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteClassFiles();
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(280, 100));

        setLayout(new FlowLayout());
        add(scrollPane);
        add(runButton);
        add(deleteButton);
    }

    private void runSelectedFile() {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedFileName = fileList.getSelectedValue();
            String fileName = selectedFileName.replace(".java", "");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessBuilder compilerProcessBuilder = new ProcessBuilder("javac", fileName + ".java");
                        compilerProcessBuilder.redirectErrorStream(true);
                        Process compilerProcess = compilerProcessBuilder.start();
                        int compilerExitCode = compilerProcess.waitFor();

                        if (compilerExitCode == 0) {
                            System.out.println("Compilation successful. Running " + fileName + "...");
                            ProcessBuilder runnerProcessBuilder = new ProcessBuilder("java", fileName);
                            runnerProcessBuilder.redirectErrorStream(true);
                            Process runnerProcess = runnerProcessBuilder.start();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(runnerProcess.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                            }
                        } else {
                            System.out.println("Compilation failed. Please check your code.");
                        }
                    } catch (IOException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void deleteClassFiles() {
        File currentDirectory = new File(".");
        File[] classFiles = currentDirectory.listFiles((dir, name) -> name.endsWith(".class"));

        if (classFiles != null) {
            for (File classFile : classFiles) {
                classFile.delete();
            }
            System.out.println("Deleted all .class files.");
        } else {
            System.out.println("No .class files found in the current directory.");
        }
    }

    public static void main(String[] args) {
        File currentDirectory = new File(".");
        File[] javaFiles = currentDirectory.listFiles((dir, name) -> name.endsWith(".java"));

        if (javaFiles == null || javaFiles.length == 0) {
            System.out.println("No .java files found in the current directory.");
            return;
        }

        String[] fileNames = new String[javaFiles.length];
        for (int i = 0; i < javaFiles.length; i++) {
            fileNames[i] = javaFiles[i].getName();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JavaFileRunner gui = new JavaFileRunner(fileNames);
                gui.setVisible(true);
            }
        });
    }
}
