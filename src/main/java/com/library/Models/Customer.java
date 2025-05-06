package com.library.Models;
import java.util.List;
public class Customer extends User{

    private List<Book> favoriteBookList;
    private List<Book> currentlyBorrowedBookList;
    public Customer(){}
    public Customer(String userID, String name, String userName, String password){
        super(userID, name, userName, password);
    }
    public Customer(String userID, String fullName, String username,
                 String password, String phoneNumber, String email) {
        super(userID, fullName, username, password, phoneNumber, email);
    }
}
