package server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseHandler {
    private Connection connection;
    private String connectionStatus;
    private ClientHandler clientHandler;
    public DatabaseHandler() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:storage.db");
            // System.out.println("Connected to the database.");
            connectionStatus = "Connection to database is successful.";
        } catch (SQLException e) {
            connectionStatus = "Something went wrong. Connection to database failed.";
            e.printStackTrace();
        }
    }

    public void insertFile(String fileName, byte[] fileData) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO Files (FileName, FileData) VALUES (?, ?)");
            statement.setString(1, fileName);
            statement.setBytes(2, fileData);
            statement.executeUpdate();
            statement.close();
            clientHandler.send(fileName+" successfully uploaded.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    public ArrayList<String> fetchFiles() {
        ArrayList<String> files = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT FileName FROM Files");
            while (resultSet.next()) {
                String fileName = resultSet.getString("FileName");
                files.add(fileName);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }

    public byte[] getFileData(String fileName) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT FileData FROM Files WHERE FileName = ?");
            statement.setString(1, fileName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBytes("FileData");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    String status() {
        return connectionStatus;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            // e.printStackTrace();
        }
    }
}
