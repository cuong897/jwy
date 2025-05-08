package com.library.Models;

import java.sql.Connection;
import java.sql.DriverManager;
public class DatabaseConnection {
    public static Connection databaseLink;

    public static Connection getConnection() {
        String databaseName = "LibraryDB";
        String databaseUser = "root";
        String databasePassword = "enter your_password_here";

        String databaseUrl = "jdbc:mysql://localhost:3306/" + databaseName;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return databaseLink;
    }
}
