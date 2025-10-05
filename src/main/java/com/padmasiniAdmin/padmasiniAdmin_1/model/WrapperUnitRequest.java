package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;

public class WrapperUnitRequest {

    private String dbname;
    private String rootId;
    private String parentId;
    private String subjectName;
    private String unitName;
    private String explanation;
    private boolean assignTest;
    private List<String> imageUrls;
    private List<String> audioFileId;
    private String aiVideoUrl;
    private List<Unit> units;

    public WrapperUnitRequest() {}

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public boolean isAssignTest() {
        return assignTest;
    }

    public void setAssignTest(boolean assignTest) {
        this.assignTest = assignTest;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getAudioFileId() {
        return audioFileId;
    }

    public void setAudioFileId(List<String> audioFileId) {
        this.audioFileId = audioFileId;
    }

    public String getAiVideoUrl() {
        return aiVideoUrl;
    }

    public void setAiVideoUrl(String aiVideoUrl) {
        this.aiVideoUrl = aiVideoUrl;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }
}
