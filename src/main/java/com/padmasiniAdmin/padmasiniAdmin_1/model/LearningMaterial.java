package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "learning_materials")
public class LearningMaterial {
    @Id
    private String id;
    private String title;
    private String description;
    private String subject;
    private String standard;
    private List<String> assignedStudents;
    private List<String> assignedBatches;
    private String materialType; // "Notes" or "Video"
    private String filePath; // For PDF/DOCX/PPT or MP4
    private String videoLink; // For Google Drive / YouTube / Meet Recording
    private LocalDateTime uploadDate = LocalDateTime.now();
    private String teacherId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    public List<String> getAssignedStudents() { return assignedStudents; }
    public void setAssignedStudents(List<String> assignedStudents) { this.assignedStudents = assignedStudents; }
    public List<String> getAssignedBatches() { return assignedBatches; }
    public void setAssignedBatches(List<String> assignedBatches) { this.assignedBatches = assignedBatches; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getVideoLink() { return videoLink; }
    public void setVideoLink(String videoLink) { this.videoLink = videoLink; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
}
