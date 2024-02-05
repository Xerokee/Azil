package com.activity.vuv_azil_navigation.models;

public class UserModel {
    String name;
    String email;
    String password;
    String profileImg;
    boolean isAdmin;
    private String documentId;

    public UserModel() {
    }

    public UserModel(String name, String email, String password, boolean isAdmin) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    public String getDocumentId() { return documentId; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }
}
