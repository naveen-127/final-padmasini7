package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;

public class MotherMCQTest {

    private String testName;
    private int marks;
    private String subjectName;

    private List<MCQTest> questionsList = new ArrayList<>();

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public List<MCQTest> getQuestionsList() { return questionsList; }
    public void setQuestionsList(List<MCQTest> questionsList) { this.questionsList = questionsList; }

    @Override
    public String toString() {
        return "MotherMCQTest{" +
                "testName='" + testName + '\'' +
                ", marks=" + marks +
                ", subjectName='" + subjectName + '\'' +
                ", questions=" + (questionsList != null ? questionsList.size() : 0) +
                '}';
    }
	public void setUnitName(String unitName) {
		// TODO Auto-generated method stub
		
	}
}
