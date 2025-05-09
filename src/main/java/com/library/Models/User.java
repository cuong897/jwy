package com.library.Models;


public class User {
    private String avatarUrl;
    private int userID;
    private String name;
    private String userName;
    private String password;
    private String phoneNumber;
    private String email;
    protected Role role;
    public enum Role {
        CUSTOMER,
        ADMIN
    }
    public User(){}

      public User (User other) {
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

    public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
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

        public void setEmail(String email) {
            this.email = email;
        }


        public boolean IsNameValid(String name){
            if(name == null || name.trim().isEmpty()){
                return false;
                }
                return true;

            }
    public User(String avatarUrl, int userID, String name, String userName, String password, String phoneNumber, String email) {
        this.avatarUrl = avatarUrl;
        this.userID = userID;
        this.name = name;
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
}