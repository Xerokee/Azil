package com.activity.vuv_azil_navigation.models;

import java.util.UUID;

public class MyCartModel {

    String productName;
    String productType;
    String currentDate;
    String currentTime;
    String animalId;

    public MyCartModel() {
    }

    public MyCartModel(String productName, String productType, String currentDate, String currentTime) {
        this.productName = productName;
        this.productType = productType;
        this.currentDate = currentDate;
        this.currentTime = currentTime;
        this.animalId = UUID.randomUUID().toString();;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
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
}
