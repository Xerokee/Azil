package com.activity.vuv_azil_navigation.models;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class ViewAllModel implements Serializable {
    String name;
    String description;
    String rating;
    String img_url;
    String type;
    private boolean isAdopted;
    private String adopterId;
    private String adopterName;
    private String documentId;

    public ViewAllModel(){
    }

    public ViewAllModel(String name, String description, String rating, String img_url, String type) {
        this.name = name;
        this.description = description;
        this.rating = rating;
        this.img_url = img_url;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @PropertyName("img_url")
    public String getImg_url() {
        return img_url;
    }

    @PropertyName("img_url")
    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("adopted")
    public boolean isAdopted() {
        return isAdopted;
    }

    @PropertyName("adopted")
    public void setAdopted(boolean adopted) { this.isAdopted = adopted; }

    @PropertyName("adopterId")
    public String getAdopterId() {
        return adopterId;
    }

    @PropertyName("adopterId")
    public void setAdopterId(String adopterId) {
        this.adopterId = adopterId;
    }

    @PropertyName("adopterName")
    public String getAdopterName() { return adopterName; }

    @PropertyName("adopterName")
    public void setAdopterName(String adopterName) { this.adopterName = adopterName; }

    public String getDocumentId() { return documentId; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }
}
