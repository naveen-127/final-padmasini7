package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;

public class WrapperUnitRequest {

    private String id;
    private String unitName;
    private String explanation;
    private String parentId;
    private List<WrapperUnitRequest> units;
    private List<String> audioFileId;
    private List<String> imageUrls;   // ✅ added
    private String aiVideoUrl;        // ✅ added
    private boolean assignTest;

    public WrapperUnitRequest() {}

    // ✅ Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public List<WrapperUnitRequest> getUnits() { return units; }
    public void setUnits(List<WrapperUnitRequest> units) { this.units = units; }

    public List<String> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(List<String> audioFileId) { this.audioFileId = audioFileId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

    public boolean isAssignTest() { return assignTest; }
    public void setAssignTest(boolean assignTest) { this.assignTest = assignTest; }
}
