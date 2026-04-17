package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "classAssignments")
public class ClassAssignment {
    @Id
    private String id;
    private String teacherId;
    private List<String> selectedStudents;
    private String subject;
    private String batchType;
    private String mode;
    private String startTime; 
    private String endTime;   
    private List<String> selectedDates; // Changed from days
    private String meetLink; // NEW field

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public List<String> getSelectedStudents() { return selectedStudents; }
    public void setSelectedStudents(List<String> selectedStudents) { this.selectedStudents = selectedStudents; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBatchType() { return batchType; }
    public void setBatchType(String batchType) { this.batchType = batchType; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public List<String> getSelectedDates() { return selectedDates; }
    public void setSelectedDates(List<String> selectedDates) { this.selectedDates = selectedDates; }
    public String getMeetLink() { return meetLink; }
    public void setMeetLink(String meetLink) { this.meetLink = meetLink; }
}