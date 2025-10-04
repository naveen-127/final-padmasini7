package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;

public class Unit {
    private String id;
    private String unitName;
    private String parentId;
    private String explanation;
    private List<String> audioFileId = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private List<Unit> units = new ArrayList<>();
    private List<String> aivideoUrls = new ArrayList<>();

    // ✅ No-arg constructor
    public Unit() {}

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(List<String> audioFileId) { this.audioFileId = audioFileId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) { this.units = units; }

    public List<String> getAivideoUrls() { return aivideoUrls; }
    public void setAivideoUrls(List<String> aivideoUrls) { this.aivideoUrls = aivideoUrls; }
}
