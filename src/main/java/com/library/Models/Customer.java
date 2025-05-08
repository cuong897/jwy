package com.library.Models;
import java.util.ArrayList;
import java.util.List;
public class Customer extends User{

    public boolean removeBorrowedBookByIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }

        Book bookToRemove = null;
        for (Book book : this.currentlyBorrowedBookList) {
            if (book.getIsbn() != null && isbn.equals(book.getIsbn())) {
                bookToRemove = book;
                break; // dung lai khi tim thay quyen dau tein
            }
        }

        if (bookToRemove != null) {
            return this.currentlyBorrowedBookList.remove(bookToRemove);
        }
        return false;
    }
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

    public Customer(String avatarUrl, int userID, String name, String userName, String password, String phoneNumber, String email) {
        super(avatarUrl, userID, name, userName, password, phoneNumber, email);}
}
