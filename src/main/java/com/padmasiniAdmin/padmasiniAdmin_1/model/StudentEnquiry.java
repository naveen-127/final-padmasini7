package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Date;
import org.bson.types.Binary;

@Document(collection = "Support")
public class StudentEnquiry {
    
    @Id	
    private String id;
    
    @Field("name")
    private String name;
    
    @Field("email")
    private String email;
    
    @Field("phone")
    private String phone;
    
    @Field("category")
    private String category;
    
    @Field("enquiryMessage")
    private String enquiryMessage;
    
    @Field("fileName")
    private String fileName;
    
    // Change from Date to String for submittedAt
    @Field("submittedAt")
    private String submittedAt;
    
    @Field("userId")
    private String userId;
    
    @Field("isRegistered")
    private Boolean isRegistered;
    
    @Field("fileData")
    private Binary fileData;
    
    @Field("contentType")
    private String contentType;

    
    // Support fields - also flat
    @Field("status")
    private String status = "pending";
    
    @Field("assignedTo")
    private String assignedTo;
    
    // Change other Date fields to String as well
    @Field("assignmentDate")
    private String assignmentDate;
    
    @Field("completedDate")
    private String completedDate;
    
    @Field("deadline")
    private String deadline;
    
    @Field("taskDescription")
    private String taskDescription;
    
    @Field("employeeNotes")
    private String employeeNotes;
    
    @Field("priority")
    private String priority = "medium";
    
    // Constructors
    public StudentEnquiry() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getEnquiryMessage() { return enquiryMessage; }
    public void setEnquiryMessage(String enquiryMessage) { this.enquiryMessage = enquiryMessage; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    // Changed from Date to String
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    
    // Helper method to get as Date if needed
    public Date getSubmittedAtAsDate() {
        if (submittedAt == null) return null;
        try {
            return Date.from(java.time.Instant.parse(submittedAt));
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Boolean getIsRegistered() { return isRegistered; }
    public void setIsRegistered(Boolean isRegistered) { this.isRegistered = isRegistered; }
    
    public Binary getFileData() { return fileData; }
    public void setFileData(Binary fileData) { this.fileData = fileData; }

    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    // Changed from Date to String
    public String getAssignmentDate() { return assignmentDate; }
    public void setAssignmentDate(String assignmentDate) { this.assignmentDate = assignmentDate; }
    
    // Helper method to set as Date
    public void setAssignmentDate(Date assignmentDate) {
        if (assignmentDate != null) {
            this.assignmentDate = assignmentDate.toInstant().toString();
        } else {
            this.assignmentDate = null;
        }
    }
    
    // Changed from Date to String
    public String getCompletedDate() { return completedDate; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }
    
    // Helper method to set as Date
    public void setCompletedDate(Date completedDate) {
        if (completedDate != null) {
            this.completedDate = completedDate.toInstant().toString();
        } else {
            this.completedDate = null;
        }
    }
    
    // Changed from Date to String
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    
    // Helper method to set as Date
    public void setDeadline(Date deadline) {
        if (deadline != null) {
            this.deadline = deadline.toInstant().toString();
        } else {
            this.deadline = null;
        }
    }
    
    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    
    public String getEmployeeNotes() { return employeeNotes; }
    public void setEmployeeNotes(String employeeNotes) { this.employeeNotes = employeeNotes; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    @Override
    public String toString() {
        return "StudentEnquiry{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
