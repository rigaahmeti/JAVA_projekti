package com.hci.scholarship.model;

public class UserAccount {
    private final String username;
    private final String fullName;
    private final String role;

    public UserAccount(String username, String fullName, String role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
}
