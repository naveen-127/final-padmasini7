=package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;

public class WrapperUnitRequest {
    private String parentId;
    private String rootUnitId;
    private String dbname;
    private String subjectName; // collection
    private String standard;
    private String unitName;
    private String explanation;
    private List<String> audioFileId;
    private List<String> imageUrls;
    private String aiVideoUrl;
    private List<String> keepAudioFileIds;

    // getters & setters
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getRootUnitId() { return rootUnitId; }
    public void setRootUnitId(String rootUnitId) { this.rootUnitId = rootUnitId; }
    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public List<String> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(List<String> audioFileId) { this.audioFileId = audioFileId; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public String getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }
    public List<String> getKeepAudioFileIds() { return keepAudioFileIds; }
    public void setKeepAudioFileIds(List<String> keepAudioFileIds) { this.keepAudioFileIds = keepAudioFileIds; }

    @Override
    public String toString() {
        return "WrapperUnitRequest [parentId=" + parentId + ", rootUnitId=" + rootUnitId + ", dbname=" + dbname
                + ", subjectName=" + subjectName + ", standard=" + standard + ", unitName=" + unitName
                + ", explanation=" + explanation + ", audioFileId=" + audioFileId + ", imageUrls=" + imageUrls
                + ", aiVideoUrl=" + aiVideoUrl + "]";
    }
}



