package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

public class UnitRequest {

    @Id
    private String id;

    private String unitName;
    private String standard;
    private String subjectName;
    
    // ✅ Add the missing fields
    private Integer order;
    private String explanation;
    private String description;
    private String customDescription;
    private List<String> tags = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private List<String> audioFileId = new ArrayList<>();
    private String aiVideoUrl;
    private String parentId;

    // ✅ Tests inside this UnitRequest
    private List<MotherMCQTest> test = new ArrayList<>();

    // ✅ Child units
    private List<Unit> units = new ArrayList<>();

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

    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    // ✅ Add the missing getters and setters
    public Integer getOrder() {
        return order;
    }
    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getExplanation() {
        return explanation;
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(Object object) {
        this.description = (String) object;
    }

    public String getCustomDescription() {
        return customDescription;
    }
    public void setCustomDescription(Object object) {
        this.customDescription = (String) object;
    }

    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    // ✅ Convenience passthroughs to match MCQTestService calls
    public void setTestName(String testName) {
        if (!test.isEmpty()) {
            test.get(0).setTestName(testName);
        }
    }

    public void setMarks(int marks) {
        if (!test.isEmpty()) {
            test.get(0).setMarks(marks);
        }
    }

    // ----- toString -----
    @Override
    public String toString() {
        return "UnitRequest{" +
                "id='" + id + '\'' +
                ", unitName='" + unitName + '\'' +
                ", standard='" + standard + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", order=" + order +
                ", explanation='" + explanation + '\'' +
                ", description='" + description + '\'' +
                ", customDescription='" + customDescription + '\'' +
                ", tags=" + tags +
                ", imageUrls=" + imageUrls +
                ", audioFileId=" + audioFileId +
                ", aiVideoUrl='" + aiVideoUrl + '\'' +
                ", parentId='" + parentId + '\'' +
                ", units=" + units +
                ", test=" + test +
                '}';
    }
}
