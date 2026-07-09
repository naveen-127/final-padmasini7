package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

public class Unit {

    private String id;
    private String unitName;
    private String parentId;
    private String explanation;
    private String description;
    private String customDescription;
    private Integer order;
    private List<String> audioFileId = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private String aiVideoUrl;
    private List<String> tags = new ArrayList<>();
    private List<Unit> units = new ArrayList<>();
    private List<MotherMCQTest> test = new ArrayList<>();
    private List<List<String>> tableData = new ArrayList<>();
    private Integer rows = 0;
    private Integer cols = 0;
    private Boolean showMatches = false;

    public Unit() {
        this.id = new ObjectId().toHexString();
    }

    public Unit(boolean withTest) {
        this();
        this.test = withTest ? new ArrayList<>() : null;
    }

    // ----- Null‑safe Getters (all List fields) -----
    public List<String> getAudioFileId() {
        return audioFileId == null ? new ArrayList<>() : audioFileId;
    }

    public List<String> getImageUrls() {
        return imageUrls == null ? new ArrayList<>() : imageUrls;
    }

    public List<String> getTags() {
        return tags == null ? new ArrayList<>() : tags;
    }

    public List<Unit> getUnits() {
        return units == null ? new ArrayList<>() : units;
    }

    public List<MotherMCQTest> getTest() {
        return test == null ? new ArrayList<>() : test;
    }

    public List<List<String>> getTableData() {
        return tableData == null ? new ArrayList<>() : tableData;
    }

    // ----- Other getters -----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCustomDescription() { return customDescription; }
    public void setCustomDescription(String customDescription) { this.customDescription = customDescription; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public String getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows != null ? rows : 0; }

    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols != null ? cols : 0; }

    public Boolean getShowMatches() { return showMatches; }
    public void setShowMatches(Boolean showMatches) { this.showMatches = showMatches != null ? showMatches : false; }

    // ----- Setters with null safety -----
    public void setAudioFileId(List<String> audioFileId) { this.audioFileId = audioFileId != null ? audioFileId : new ArrayList<>(); }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>(); }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }
    public void setUnits(List<Unit> units) { this.units = units != null ? units : new ArrayList<>(); }
    public void setTest(List<MotherMCQTest> test) { this.test = test != null ? test : new ArrayList<>(); }
    public void setTableData(List<List<String>> tableData) { this.tableData = tableData != null ? tableData : new ArrayList<>(); }

    // ----- Other methods -----
    public void setStandard(String standard) { /* stub */ }
    public String getStandard() { return null; }

    @Override
    public String toString() {
        return "Unit [id=" + id +
                ", unitName=" + unitName +
                ", parentId=" + parentId +
                ", explanation=" + explanation +
                ", audioCount=" + (audioFileId != null ? audioFileId.size() : 0) +
                ", imageCount=" + (imageUrls != null ? imageUrls.size() : 0) +
                ", tagCount=" + (tags != null ? tags.size() : 0) +
                ", subUnitCount=" + (units != null ? units.size() : 0) +
                ", aiVideoUrl=" + aiVideoUrl + "]";
    }
}