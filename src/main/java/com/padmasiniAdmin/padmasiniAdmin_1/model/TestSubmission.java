package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "student_submissions")
public class TestSubmission {
    @Id
    private String id;
    private String testPaperId;
    private String studentName;
    private String status;
    private String studentAnswerContent;
    private String answerFilePath;
    private LocalDateTime submittedAt = LocalDateTime.now();
    
    // Evaluation fields
    private String marks;
    private String remarks;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTestPaperId() { return testPaperId; }
    public void setTestPaperId(String testPaperId) { this.testPaperId = testPaperId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getStudentAnswerContent() { return studentAnswerContent; }
    public void setStudentAnswerContent(String studentAnswerContent) { this.studentAnswerContent = studentAnswerContent; }
    
    public String getAnswerFilePath() { return answerFilePath; }
    public void setAnswerFilePath(String answerFilePath) { this.answerFilePath = answerFilePath; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getMarks() { return marks; }
    public void setMarks(String marks) { this.marks = marks; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
