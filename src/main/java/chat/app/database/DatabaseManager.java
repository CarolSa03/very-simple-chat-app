package chat.app.database;

import java.sql.*;

public class DatabaseManager {

    private Connection connection;

    public void connect() {
        try {
            String url = "jdbc:sqlite:chat-app.db";
            connection = DriverManager.getConnection(url);
            System.out.println("Connected to SQLite database.");

            // Create users table if it doesn't exist
            createUsersTable();
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private void createUsersTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Users table is ready.");
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    public boolean addUser(String username, String password) {
        String insertSQL = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    public boolean validateUser(String username, String password) {
        String selectSQL = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error validating user: " + e.getMessage());
            return false;
        }
    }

    public boolean validateUsername(String username) {
        String selectSQL = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }

    public void clearUsersTable() {
        String deleteSQL = "DELETE FROM users";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(deleteSQL);
            System.out.println("All users have been removed from the table.");
        } catch (SQLException e) {
            System.out.println("Error clearing users table: " + e.getMessage());
        }
    }

}

