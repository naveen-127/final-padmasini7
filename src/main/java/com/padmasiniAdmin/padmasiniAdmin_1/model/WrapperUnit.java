package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

public class WrapperUnit {

    private String unit;

    @NotBlank(message = "Parent ID is required")
    private String parentId;

    @NotBlank(message = "Standard is required")
    private String standard;

    private List<String> keepAudioFileIds;
    private String dbname;
    private String unitName;
    private String explanation;

    @NotBlank(message = "Root Unit ID is required")
    private String rootUnitId;

    private String subjectName;
    private List<String> audioFileId;
    private List<String> imageUrls;

    // ✅ CHANGED: from String to List<String>
    private List<String> aiVideoUrl;

    private List<MCQTest> test;

    // --- Getters & Setters ---
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public List<String> getKeepAudioFileIds() { return keepAudioFileIds; }
    public void setKeepAudioFileIds(List<String> keepAudioFileIds) { this.keepAudioFileIds = keepAudioFileIds; }

    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }

    public List<MCQTest> getTest() { return test; }
    public void setTest(List<MCQTest> test) { this.test = test; }

    public String getRootUnitId() { return rootUnitId; }
    public void setRootUnitId(String rootUnitId) { this.rootUnitId = rootUnitId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getParentId() {
        if ((parentId == null || parentId.isEmpty()) && unit != null && !unit.isEmpty()) {
            return unit;
        }
        return parentId;
    }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public List<String> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(List<String> audioFileId) { this.audioFileId = audioFileId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(List<String> aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

    @Override
    public String toString() {
        return "WrapperUnit [parentId=" + parentId + ", unitName=" + unitName +
                ", aiVideoUrl=" + aiVideoUrl + ", audioFileId=" + audioFileId + "]";
    }
}
