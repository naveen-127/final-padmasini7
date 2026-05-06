package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String type; // e.g. "RESCHEDULE_REQUEST"
    private String classAssignmentId;
    private String batchName;
    private String teacherId;
    private String teacherName;
    private String oldDate;
    private String newDate;
    private String status; // "PENDING", "ACCEPTED", "DECLINED"
    private String message;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getClassAssignmentId() { return classAssignmentId; }
    public void setClassAssignmentId(String classAssignmentId) { this.classAssignmentId = classAssignmentId; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getOldDate() { return oldDate; }
    public void setOldDate(String oldDate) { this.oldDate = oldDate; }

    public String getNewDate() { return newDate; }
    public void setNewDate(String newDate) { this.newDate = newDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
