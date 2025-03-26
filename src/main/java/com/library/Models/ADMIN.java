package com.library.Models;

public class ADMIN extends User{
    public ADMIN (){}
    public ADMIN(String userID, String name, String userName, String password){
        super(userID, name, userName, password);
    }
    public ADMIN(String userID, String fullName, String username,
                 String password, String phoneNumber, String email) {
        super(userID, fullName, username, password, phoneNumber, email);
    }
}
