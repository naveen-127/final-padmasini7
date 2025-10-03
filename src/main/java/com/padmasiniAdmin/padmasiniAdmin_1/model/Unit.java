package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

public class Unit {

    private String id;
    private String unitName;
    private String parentId;   // links back to parent
    private String explanation;

    private List<String> audioFileId;
    private List<String> imageUrls;  // ✅ image array
    private String aiVideoUrl;       // ✅ single AI video field

    // ❌ remove "units" and "test"
    // private List<Unit> units = new ArrayList<>();
    // private List<MotherMCQTest> test = new ArrayList<>();

    public Unit() {
        this.id = new ObjectId().toHexString();
    }

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

    @Override
    public String toString() {
        return "Unit [id=" + id +
                ", name=" + unitName +
                ", parentId=" + parentId +
                ", audioFileId=" + audioFileId +
                ", imageUrls=" + imageUrls +
                ", aiVideoUrl=" + aiVideoUrl + "]";
    }
}
