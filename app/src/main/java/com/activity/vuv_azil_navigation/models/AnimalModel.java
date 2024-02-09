package com.activity.vuv_azil_navigation.models;

import com.google.firebase.database.PropertyName;

public class AnimalModel {
    private String animalId;
    private String animalName;
    private String animalType;
    private String currentDate;
    private String currentTime;
    private String img_url;
    private boolean isAdopted;
    private String adopterId;
    private String adopterName;
    private String documentId;

    public AnimalModel() {
    }

    public AnimalModel(String animalId, String animalName, String animalType, String img_url, boolean isAdopted) {
        this.animalId = animalId;
        this.animalName = animalName;
        this.animalType = animalType;
        this.img_url = img_url;
        this.isAdopted = isAdopted;
    }

    @PropertyName("animalId")
    public String getAnimalId() {
        return animalId;
    }

    @PropertyName("animalId")
    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public String getAnimalName() {
        return animalName;
    }

    public void setAnimalName(String animalName) {
        this.animalName = animalName;
    }

    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    @PropertyName("currentDate")
    public String getCurrentDate() {
        return currentDate;
    }

    @PropertyName("currentDate")
    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    @PropertyName("currentTime")
    public String getCurrentTime() {
        return currentTime;
    }

    @PropertyName("currentTime")
    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    @PropertyName("img_url")
    public String getImg_url() {
        return img_url;
    }

    @PropertyName("img_url")
    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    @PropertyName("adopted")
    public boolean isAdopted() {
        return isAdopted;
    }

    @PropertyName("adopted")
    public void setAdopted(boolean adopted) {
        isAdopted = adopted;
    }

    @PropertyName("adopterId")
    public String getAdopterId() {
        return adopterId;
    }

    @PropertyName("adopterId")
    public void setAdopterId(String adopterId) {
        this.adopterId = adopterId;
    }

    @PropertyName("adopterName")
    public String getAdopterName() {
        return adopterName;
    }

    @PropertyName("adopterName")
    public void setAdopterName(String adopterName) {
        this.adopterName = adopterName;
    }

    public String getDocumentId() { return documentId; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }
}

