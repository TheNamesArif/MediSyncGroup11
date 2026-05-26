package com.example.medisync.model;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String age;
    private String gender;
    private String role;

    public User() {}

    public User(String uid, String fullName, String email, String age, String gender, String role) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.role = role;
    }

    public String getUid() { return uid; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getAge() { return age; }
    public String getGender() { return gender; }
    public String getRole() { return role; }

    // Setters are required for Firebase to populate private fields
    public void setUid(String uid) { this.uid = uid; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(String age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setRole(String role) { this.role = role; }
}
