package com.library.Models;

import java.sql.Connection;
import java.sql.DriverManager;
public class DatabaseConnection {
    public static Connection databaseLink = null;

    public static Connection getConnection() {
        if(databaseLink == null){
            String user = "root";
            String password = "Your password goes here";
            String databaseUrl = "jdbc:mysql://localhost:3306/libraryDB";

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                databaseLink =DriverManager.getConnection(databaseUrl, user, password);
            } catch (Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return databaseLink;
    }
    public static void closeConnection() {
        if (databaseLink != null) {
            try {
                databaseLink.close();
                databaseLink = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
