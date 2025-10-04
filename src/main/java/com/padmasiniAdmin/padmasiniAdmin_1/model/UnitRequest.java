package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

public class UnitRequest {

    @Id
    private String id;

    private String unitName;
    private String standard;

    // ✅ Tests inside this UnitRequest
    private List<MotherMCQTest> test = new ArrayList<>();

    // ✅ Child units
    private List<Unit> units = new ArrayList<>();

    // ✅ New fields for audio, images, explanation, AI video
    private String explanation;
    private List<String> audioFileId = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private String aiVideoUrl;

    // ----- Constructors -----
    public UnitRequest() {
    }

    // ----- Getters & Setters -----
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUnitName() {
        return unitName;
    }
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getStandard() {
        return standard;
    }
    public void setStandard(String standard) {
        this.standard = standard;
    }

    public List<Unit> getUnits() {
        return units;
    }
    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public List<MotherMCQTest> getTest() {
        return test;
    }
    public void setTest(List<MotherMCQTest> test) {
        this.test = test;
    }

    // ✅ New getters/setters
    public String getExplanation() {
        return explanation;
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
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

    // ----- Convenience passthroughs to match MCQTestService calls -----
    public void setTestName(String testName) {
        if (!test.isEmpty()) {
            test.get(0).setTestName(testName); // update first test by default
        }
    }

    public void setMarks(int marks) {
        if (!test.isEmpty()) {
            test.get(0).setMarks(marks);
        }
    }

    public void setSubjectName(String subjectName) {
        if (!test.isEmpty()) {
            test.get(0).setSubjectName(subjectName);
        }
    }

    public List<MCQTest> getQuestionsList() {
        if (!test.isEmpty()) {
            return test.get(0).getQuestionsList();
        }
        return new ArrayList<>();
    }

    // ----- toString -----
    @Override
    public String toString() {
        return "UnitRequest{" +
                "id='" + id + '\'' +
                ", unitName='" + unitName + '\'' +
                ", standard='" + standard + '\'' +
                ", units=" + units +
                ", test=" + test +
                ", explanation='" + explanation + '\'' +
                ", audioFileId=" + audioFileId +
                ", imageUrls=" + imageUrls +
                ", aiVideoUrl='" + aiVideoUrl + '\'' +
                '}';
    }
}
