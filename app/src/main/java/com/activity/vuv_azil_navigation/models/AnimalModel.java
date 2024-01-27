package com.activity.vuv_azil_navigation.models;

public class AnimalModel {
    private String animalId;
    private String animalName;
    private String animalType;
    private String imgUrl;
    private boolean isAdopted;

    public AnimalModel() {
        // Default constructor required for calls to DataSnapshot.getValue(AnimalModel.class)
    }

    public AnimalModel(String animalId, String animalName, String animalType, String imgUrl, boolean isAdopted) {
        this.animalId = animalId;
        this.animalName = animalName;
        this.animalType = animalType;
        this.imgUrl = imgUrl;
        this.isAdopted = isAdopted;
    }

    public String getAnimalId() {
        return animalId;
    }

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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isAdopted() {
        return isAdopted;
    }

    public void setAdopted(boolean adopted) {
        isAdopted = adopted;
    }
}

