package com.library.Models;

import java.sql.DriverManager;
import java.sql.Connection;

public class TestDBConnection {
    public static void main(String[] args) {
        String user = "root";
        String password = "thubao12345";
        String databaseUrl = "jdbc:mysql://localhost:3306/libraryDB";

        try {
            Connection connection = DriverManager.getConnection(databaseUrl, user, password);
            if (connection != null){
                System.out.println("Ket noi thanh cong");
            }else System.out.println("Ket noi that bai");
        } catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
