package com.library.Models;

public class ADMIN extends User{
    public ADMIN (){}
    public ADMIN(int userID, String name, String userName, String password){
        super(userID, name, userName, password);
    }
    public ADMIN(int userID, String fullName, String username,
                 String password, String phoneNumber, String email) {
        super(userID, fullName, username, password, phoneNumber, email);
    }
    public Customer convertADMINtoCustomer(ADMIN admin) {
        return new Customer(this.getAvatarUrl(),this.getUserID(), this.getName(), this.getUserName(),
                this.getPassword(), this.getPhoneNumber(), this.getEmail());
    }
}
