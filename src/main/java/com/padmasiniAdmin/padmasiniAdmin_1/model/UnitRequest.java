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
    private Integer order;
    private String explanation;
    private String description;
    private String customDescription;
    private List<String> tags = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private List<String> audioFileId = new ArrayList<>();
    private String aiVideoUrl;
    private String parentId;
    private Boolean isLesson;
    private List<MotherMCQTest> test = new ArrayList<>();
    private List<Unit> units = new ArrayList<>();
    private List<List<String>> tableData = new ArrayList<>();
    private Integer rows = 0;
    private Integer cols = 0;
    private Boolean showMatches = false;

    public UnitRequest() {
        this.isLesson = true;
    }

    // ----- Null‑safe Getters (all List fields) -----
    public List<Unit> getUnits() {
        return units == null ? new ArrayList<>() : units;
    }

    public List<String> getImageUrls() {
        return imageUrls == null ? new ArrayList<>() : imageUrls;
    }

    public List<String> getAudioFileId() {
        return audioFileId == null ? new ArrayList<>() : audioFileId;
    }

    public List<String> getTags() {
        return tags == null ? new ArrayList<>() : tags;
    }

    public List<MotherMCQTest> getTest() {
        return test == null ? new ArrayList<>() : test;
    }

    public List<List<String>> getTableData() {
        return tableData == null ? new ArrayList<>() : tableData;
    }

    // ----- Other getters (non‑List) remain unchanged -----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCustomDescription() { return customDescription; }
    public void setCustomDescription(String customDescription) { this.customDescription = customDescription; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows != null ? rows : 0; }

    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols != null ? cols : 0; }

    public Boolean getShowMatches() { return showMatches; }
    public void setShowMatches(Boolean showMatches) { this.showMatches = showMatches != null ? showMatches : false; }

    public String getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public Boolean getIsLesson() { return isLesson; }
    public void setIsLesson(Boolean isLesson) { this.isLesson = isLesson; }

    // ----- Setters for List fields (already null‑safe) -----
    public void setUnits(List<Unit> units) { this.units = units != null ? units : new ArrayList<>(); }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>(); }
    public void setAudioFileId(List<String> audioFileId) { this.audioFileId = audioFileId != null ? audioFileId : new ArrayList<>(); }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }
    public void setTest(List<MotherMCQTest> test) { this.test = test != null ? test : new ArrayList<>(); }
    public void setTableData(List<List<String>> tableData) { this.tableData = tableData != null ? tableData : new ArrayList<>(); }

    // ----- Convenience methods -----
    public void setTestName(String testName) {
        if (getTest() != null && !getTest().isEmpty()) {
            getTest().get(0).setTestName(testName);
        }
    }

    public void setMarks(int marks) {
        if (getTest() != null && !getTest().isEmpty()) {
            getTest().get(0).setMarks(marks);
        }
    }

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
                ", isLesson=" + isLesson +
                ", units=" + units +
                ", test=" + test +
                '}';
    }
}