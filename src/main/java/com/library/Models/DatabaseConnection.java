package com.library.Models;

import java.sql.Connection;
import java.sql.DriverManager;
public class DatabaseConnection {
    public static Connection databaseLink = null;

    public static Connection getConnection() {
        if(databaseLink == null){
            String user = "root";
<<<<<<< HEAD
            String password = "thubao12345";
=======
            String password = "Your password goes here";
>>>>>>> 4dfe4beec7ecdba9f655517bed17d8eff7b08fa1
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
