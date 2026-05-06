package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "test_paper_submissions")
public class TestPaperSubmission {
    @Id
    private String id;
    private String testId;
    private String studentId;
    private String answerText;
    private String filePath;
    private String submittedAt;
    
    // Evaluation fields
    private String status = "Pending";
    private String marks;
    private String remarks;
    private String evaluatedFilePath;
    private java.util.List<java.util.Map<String, Object>> evaluationDetails;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMarks() { return marks; }
    public void setMarks(String marks) { this.marks = marks; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getEvaluatedFilePath() { return evaluatedFilePath; }
    public void setEvaluatedFilePath(String evaluatedFilePath) { this.evaluatedFilePath = evaluatedFilePath; }

    public java.util.List<java.util.Map<String, Object>> getEvaluationDetails() { return evaluationDetails; }
    public void setEvaluationDetails(java.util.List<java.util.Map<String, Object>> evaluationDetails) { this.evaluationDetails = evaluationDetails; }
}
