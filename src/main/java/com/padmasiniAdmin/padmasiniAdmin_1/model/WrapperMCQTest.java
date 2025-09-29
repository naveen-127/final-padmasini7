package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class WrapperMCQTest {

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

    // ✅ Audio files
    private List<String> audioFileId;

    // ✅ Images
    private List<String> imageUrls;

    // ✅ AI Video
    private String aiVideoUrl;

    private List<MCQTest> test;

    // ✅ New fields to fix MCQTestService compilation
    private int correctIndex;
    private int rows;
    private int cols;
    private List<List<String>> tableData;

    private String testName;
    private int marks;
    private String quesId;
    private List<MCQTest> questionsList;

    // ----- Getters & Setters -----
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public List<String> getKeepAudioFileIds() { return keepAudioFileIds; }
    public void setKeepAudioFileIds(List<String> keepAudioFileIds) { this.keepAudioFileIds = keepAudioFileIds; }

    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getRootUnitId() { return rootUnitId; }
    public void setRootUnitId(String rootUnitId) { this.rootUnitId = rootUnitId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public List<String> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(List<String> audioFileId) { this.audioFileId = audioFileId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

    public List<MCQTest> getTest() { return test; }
    public void setTest(List<MCQTest> test) { this.test = test; }

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public List<List<String>> getTableData() { return tableData; }
    public void setTableData(List<List<String>> tableData) { this.tableData = tableData; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public String getQuesId() { return quesId; }
    public void setQuesId(String quesId) { this.quesId = quesId; }

    public List<MCQTest> getQuestionsList() { return questionsList; }
    public void setQuestionsList(List<MCQTest> questionsList) { this.questionsList = questionsList; }

    @Override
    public String toString() {
        return "WrapperMCQTest [parentId=" + parentId + ", standard=" + standard + ", dbname=" + dbname +
                ", unitName=" + unitName + ", explanation=" + explanation + ", rootUnitId=" + rootUnitId +
                ", subjectName=" + subjectName + ", audioFileId=" + audioFileId + ", imageUrls=" + imageUrls +
                ", aiVideoUrl=" + aiVideoUrl + ", test=" + test + ", correctIndex=" + correctIndex +
                ", rows=" + rows + ", cols=" + cols + ", tableData=" + tableData + ", testName=" + testName +
                ", marks=" + marks + ", quesId=" + quesId + ", questionsList=" + questionsList + "]";
    }
}
