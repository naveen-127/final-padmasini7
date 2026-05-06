package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;

public class MotherMCQTest {

    private String testName;
    private int marks;
    private String subjectName;
    private String unitName;
    private List<MCQTest> questionsList = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public List<MCQTest> getQuestionsList() { return questionsList; }
    public void setQuestionsList(List<MCQTest> questionsList) { this.questionsList = questionsList; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @Override
    public String toString() {
        return "MotherMCQTest{" +
                "testName='" + testName + '\'' +
                ", marks=" + marks +
                ", subjectName='" + subjectName + '\'' +
                ", unitName='" + unitName + '\'' +
                ", tags=" + tags + 
                ", questions=" + (questionsList != null ? questionsList.size() : 0) +
                '}';
    }
}
