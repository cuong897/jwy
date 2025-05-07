package com.library.Models;

public class User {
    private String avatarUrl;
    private int userID;
    private String name;
    private String userName;
    private String password;
    private String phoneNumber;
    private String email;
    private String role;
    public User(){}
    public User (User other) {
        this.userID = other.userID;
        this.name = other.name;
        this.userName = other.userName;
        this.password = other.password;
        this.phoneNumber = other.phoneNumber;
        this.email = other.email;
        this.role = role;
    }
    public User (int userID, String name, String userName, String password) {
        this.userID = userID;
        this.name = name;
        this.userName = userName;
        this.password = password;
    }
    public User (int userID, String name, String userName, String password, String phoneNumber, String email) {
        this.userID = userID;
        this.name = name;
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean IsNameValid(String name){
        if(name == null || name.trim().isEmpty()){
            return false;
        }else if(name.length() > 50){
            System.out.println("Ten khong duoc vuot qua 50 ky tu");
            return false;
        }
        for(char c : name.toCharArray()){
            if(!Character.isLetter(c) && !Character.isWhitespace(c)) return false;
        }
        return true;
    }
    public boolean isPasswordValid(String password){
        if(password.trim().isEmpty()) return false;
        return (password.length() >= 8);
    }

    public boolean IsEmailValid(String email){
        int count1 = 0,count2 = 0;
        for(char c: email.toCharArray() ){
            if(c == '@') count1++;
            if(c == '.') count2++;
        }
        if(count1 != 1 || count2 != 1) return false;
        int index1 = email.indexOf('@');
        int index2 = email.indexOf('.');
        if(index1 <= 0) return false;
        return (index2 > index1 + 1 && index2 < email.length() - 1);
    }
    public boolean IsPhoneNumberValid(String phoneNumber){
        for(char c : phoneNumber.toCharArray()){
            if(!Character.isDigit(c)) return false;
        }
        return phoneNumber.length() == 10;
    }
    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
