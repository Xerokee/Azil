package com.activity.vuv_azil_navigation.models;

import java.util.UUID;

public class MyCartModel {

    String animalName;
    String animalType;
    String currentDate;
    String currentTime;
    String animalId;
    String img_url;

    public MyCartModel() {
    }

    public MyCartModel(String animalName, String animalType, String currentDate, String currentTime, String img_url) {
        this.animalName = animalName;
        this.animalType = animalType;
        this.currentDate = currentDate;
        this.currentTime = currentTime;
        this.animalId = UUID.randomUUID().toString();;
        this.img_url = img_url;
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

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }
    public String getImgUrl() {return img_url; }

    public void setImgUrl(String img_url) {this.img_url = img_url; }
}
