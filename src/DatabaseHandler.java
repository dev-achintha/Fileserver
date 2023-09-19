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

    public void insertFile(int userId, String fileName, byte[] fileData) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Files (UserID, FileName, FileData) VALUES (?, ?, ?)");
            statement.setInt(1, userId);
            statement.setString(2, fileName);
            statement.setBytes(3, fileData);
            statement.executeUpdate();
            statement.close();
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
