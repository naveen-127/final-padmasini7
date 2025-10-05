package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;

public class Unit {

    private String unitName;
    private String explanation;
    private List<String> imageUrls;
    private List<String> audioFileId;
    private String aiVideoUrl;

    // if you had a test field before, add it back:
    private Test test;

    public Unit() {}

    public Unit(String unitName, String explanation, List<String> imageUrls, List<String> audioFileId, String aiVideoUrl) {
        this.unitName = unitName;
        this.explanation = explanation;
        this.imageUrls = imageUrls;
        this.audioFileId = audioFileId;
        this.aiVideoUrl = aiVideoUrl;
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

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }
}
