package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;

public class UnitRequest {
    private String dbname;
    private String rootId;
    private String unitName;
    private ArrayList<Object> audioFileId;
    private ArrayList<Object> imageUrls;

    // Getters & Setters
    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }

    public String getRootId() { return rootId; }
    public void setRootId(String rootId) { this.rootId = rootId; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public ArrayList<Object> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(ArrayList<Object> audioFileId) { this.audioFileId = audioFileId; }

    public ArrayList<Object> getImageUrls() { return imageUrls; }
    public void setImageUrls(ArrayList<Object> imageUrls) { this.imageUrls = imageUrls; }
}
