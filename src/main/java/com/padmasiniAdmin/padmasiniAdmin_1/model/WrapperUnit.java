package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class WrapperUnit {

    @NotBlank(message = "Parent ID is required")
    private String parentId;

    @NotBlank(message = "Standard is required")
    private String standard;

    private String dbname;
    private String unitName;
    private String explanation;

    @NotBlank(message = "Root Unit ID is required")
    private String rootUnitId;

    private String subjectName;

    // ✅ Keep Audio file IDs (existing files not to be deleted)
    private List<String> keepAudioFileIds;

    // ✅ Audio files (newly uploaded IDs)
    private List<String> audioFileId;

    // ✅ Images (AI-generated or uploaded)
    private List<String> imageUrls;

    // ✅ AI Video (single generated video)
    private String aiVideoUrl;

    // ----- Getters & Setters -----
    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getStandard() {
        return standard;
    }
    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getDbname() {
        return dbname;
    }
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getUnitName() {
        return unitName;
    }
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getExplanation() {
        return explanation;
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getRootUnitId() {
        return rootUnitId;
    }
    public void setRootUnitId(String rootUnitId) {
        this.rootUnitId = rootUnitId;
    }

    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public List<String> getKeepAudioFileIds() {
        return keepAudioFileIds;
    }
    public void setKeepAudioFileIds(List<String> keepAudioFileIds) {
        this.keepAudioFileIds = keepAudioFileIds;
    }

    public List<String> getAudioFileId() {
        return audioFileId;
    }
    public void setAudioFileId(List<String> audioFileId) {
        this.audioFileId = audioFileId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getAiVideoUrl() {
        return aiVideoUrl;
    }
    public void setAiVideoUrl(String aiVideoUrl) {
        this.aiVideoUrl = aiVideoUrl;
    }

    @Override
    public String toString() {
        return "WrapperUnit [parentId=" + parentId 
                + ", standard=" + standard 
                + ", dbname=" + dbname 
                + ", unitName=" + unitName 
                + ", explanation=" + explanation 
                + ", rootUnitId=" + rootUnitId 
                + ", subjectName=" + subjectName 
                + ", keepAudioFileIds=" + keepAudioFileIds 
                + ", audioFileId=" + audioFileId 
                + ", imageUrls=" + imageUrls 
                + ", aiVideoUrl=" + aiVideoUrl + "]";
    }
}
