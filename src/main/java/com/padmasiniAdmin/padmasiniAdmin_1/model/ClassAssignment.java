package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "classAssignments")
public class ClassAssignment {
    @Id
    private String id;
    private String batchName;
    private String subject;
    private String standard;
    private String teacherId;
    private String assistantTeacherId;
    private List<String> selectedDates;
    private List<String> days;
    private String startTime; 
    private String endTime;   
    private String mode;
    private String meetLink;
    private String status;
    private List<String> selectedStudents;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getAssistantTeacherId() { return assistantTeacherId; }
    public void setAssistantTeacherId(String assistantTeacherId) { this.assistantTeacherId = assistantTeacherId; }

    public List<String> getSelectedDates() { return selectedDates; }
    public void setSelectedDates(List<String> selectedDates) { this.selectedDates = selectedDates; }

    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getMeetLink() { return meetLink; }
    public void setMeetLink(String meetLink) { this.meetLink = meetLink; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getSelectedStudents() { return selectedStudents; }
    public void setSelectedStudents(List<String> selectedStudents) { this.selectedStudents = selectedStudents; }
}