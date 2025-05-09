package com.library.Models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() {
        String databaseName = "libraryDB";
        String databaseUser = "root";
        String databasePassword = "Gõ password của bạn vào đây"; // Thay đổi password của bạn vào đây
        String databaseUrl = "jdbc:mysql://localhost:3306/" + databaseName;
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}
