package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "unit")
public class Unit {

    @Id
    private String id;

    private String unitName;
    private String parentId;
    private String rootId;
    private String subjectName;
    private String dbname;
    private boolean assignTest;

    private List<Unit> units;
    private List<String> imageUrls;
    private String aiVideoUrl;
    private String audioFileId;
    private String explanation;

    private List<Test> tests; // ✅ Added back since MCQTestService expects it

    // Getters and Setters
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

    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRootId() {
        return rootId;
    }
    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDbname() {
        return dbname;
    }
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public boolean isAssignTest() {
        return assignTest;
    }
    public void setAssignTest(boolean assignTest) {
        this.assignTest = assignTest;
    }

    public List<Unit> getUnits() {
        return units;
    }
    public void setUnits(List<Unit> units) {
        this.units = units;
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

    public String getAudioFileId() {
        return audioFileId;
    }
    public void setAudioFileId(String audioFileId) {
        this.audioFileId = audioFileId;
    }

    public String getExplanation() {
        return explanation;
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<Test> getTests() {
        return tests;
    }
    public void setTests(List<Test> tests) {
        this.tests = tests;
    }
}
