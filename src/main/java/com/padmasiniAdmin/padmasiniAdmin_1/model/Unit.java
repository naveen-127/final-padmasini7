package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

public class Unit {

    private String id;
    private String unitName;
    private String parentId;   // links back to parent
    private String explanation;

    private List<String> audioFileId = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private List<String> aiVideoUrl = new ArrayList<>();   // ✅ NEW — AI Video URLs

    private List<Unit> units = new ArrayList<>();          // subtopics
    private List<MotherMCQTest> test = new ArrayList<>();  // optional tests

    public Unit() {
        this.id = new ObjectId().toHexString();
    }

    public Unit(boolean withTest) {
        this();
        this.test = withTest ? new ArrayList<>() : new ArrayList<>();
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(List<String> audioFileId) {
        this.audioFileId = (audioFileId == null) ? new ArrayList<>() : audioFileId;
    }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls == null) ? new ArrayList<>() : imageUrls;
    }

    public List<String> getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(List<String> aiVideoUrl) {
        this.aiVideoUrl = (aiVideoUrl == null) ? new ArrayList<>() : aiVideoUrl;
    }

    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) { this.units = units; }

    public List<MotherMCQTest> getTest() { return test; }
    public void setTest(List<MotherMCQTest> test) { this.test = test; }

    @Override
    public String toString() {
        return "Unit [id=" + id +
                ", name=" + unitName +
                ", parentId=" + parentId +
                ", aiVideoUrlCount=" + (aiVideoUrl != null ? aiVideoUrl.size() : 0) +
                ", subUnits=" + (units != null ? units.size() : 0) + "]";
    }
}
