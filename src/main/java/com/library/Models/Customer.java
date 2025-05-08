package com.library.Models;
import java.util.ArrayList;
import java.util.List;
public class Customer extends User{

    private List<Book> favoriteBookList;
    private List<Book> currentlyBorrowedBookList = new ArrayList<>();
    public Customer(){}
    public Customer(int userID, String name, String userName, String password){
        super(userID, name, userName, password);
    }
    public Customer(int userID, String fullName, String username,
                 String password, String phoneNumber, String email) {
        super(userID, fullName, username, password, phoneNumber, email);
    }

    public ArrayList<Book> getCurrentlyBorrowedBookList() {
        return (ArrayList<Book>) currentlyBorrowedBookList;
    }

    public void setCurrentlyBorrowedBookList(ArrayList<Book> currentlyBorrowedBookList) {
        this.currentlyBorrowedBookList = currentlyBorrowedBookList;
    }
}
