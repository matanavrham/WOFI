package com.example.wofi;

public class User {
    public String username;
    public String email;
    public String phone;
    public String userType;
    public String profession;
    public String userId;

    // נדרש ע"י Firebase
    public User() {
    }

    public User(String username, String email, String phone, String userType, String profession) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.profession = profession;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
