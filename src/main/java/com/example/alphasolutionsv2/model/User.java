package com.example.alphasolutionsv2.model;

public class User {
    private long userId;
    private String username;
    private String email;
    private String password;
    private Role role;  // Changed from String to Role object

    public User() {
        // Default constructor
    }

    public User(long userId, String username, String email, String password, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Add toString method to provide a nice string representation
    @Override
    public String toString() {
        return username + " (" + (role != null ? role.getRoleName() : "Ingen rolle") + ")";
    }
}
