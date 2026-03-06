package com.personaldiary.models;

import com.google.firebase.Timestamp;

public class User {

    private String name;
    private String email;
    private Timestamp createdAt;

    public User() {}

    public User(String name, String email, Timestamp createdAt) {
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
