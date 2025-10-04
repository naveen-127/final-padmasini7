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
    private String aiVideoUrl;

    private boolean assignTest;
    private List<Unit> units = new ArrayList<>();

    // ✅ Add tests field here
    private List<MotherMCQTest> test = new ArrayList<>();

    public Unit() {}
    public Unit(boolean assignTest) { this.assignTest = assignTest; }

    // ----- Getters & Setters -----
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

    public String getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

    public boolean isAssignTest() { return assignTest; }
    public void setAssignTest(boolean assignTest) { this.assignTest = assignTest; }

    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) { this.units = units; }

    // ✅ Getter/setter for test field
    public List<MotherMCQTest> getTest() { return test; }
    public void setTest(List<MotherMCQTest> test) { this.test = test; }
}
