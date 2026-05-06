package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.Binary;

@Document(collection = "Enquiry")
public class PeopleEnquiry {
    
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
    
    @Field("submittedAt")
    private String submittedAt;
    
    @Field("isRegistered")
    private Boolean isRegistered;
    
    @Field("fileData")
    private Binary fileData;
    
    @Field("contentType")
    private String contentType;
    
    // Support fields
    @Field("status")
    private String status = "new"; // new, contacted, assigned, resolved, spam
    
    @Field("assignedTo")
    private String assignedTo;
    
    @Field("assignmentDate")
    private String assignmentDate;
    
    @Field("completedDate")
    private String completedDate;
    
    @Field("deadline")
    private String deadline;
    
    @Field("taskDescription")
    private String taskDescription;
    
    @Field("notes")
    private String notes;
    
    @Field("priority")
    private String priority = "medium"; // low, medium, high, urgent
    
    @Field("assignedEmployeeName")
    private String assignedEmployeeName;
    
    @Field("assignedEmployeeDesignation")
    private String assignedEmployeeDesignation;
    
    // Constructors
    public PeopleEnquiry() {}
    
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
    
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    
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
    
    public String getAssignmentDate() { return assignmentDate; }
    public void setAssignmentDate(String assignmentDate) { this.assignmentDate = assignmentDate; }
    
    // Helper method to set as Date
    public void setAssignmentDate(java.util.Date assignmentDate) {
        if (assignmentDate != null) {
            this.assignmentDate = assignmentDate.toInstant().toString();
        } else {
            this.assignmentDate = null;
        }
    }
    
    public String getCompletedDate() { return completedDate; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }
    
    // Helper method to set as Date
    public void setCompletedDate(java.util.Date completedDate) {
        if (completedDate != null) {
            this.completedDate = completedDate.toInstant().toString();
        } else {
            this.completedDate = null;
        }
    }
    
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    
    // Helper method to set as Date
    public void setDeadline(java.util.Date deadline) {
        if (deadline != null) {
            this.deadline = deadline.toInstant().toString();
        } else {
            this.deadline = null;
        }
    }
    
    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public String getAssignedEmployeeName() { return assignedEmployeeName; }
    public void setAssignedEmployeeName(String assignedEmployeeName) { this.assignedEmployeeName = assignedEmployeeName; }
    
    public String getAssignedEmployeeDesignation() { return assignedEmployeeDesignation; }
    public void setAssignedEmployeeDesignation(String assignedEmployeeDesignation) { this.assignedEmployeeDesignation = assignedEmployeeDesignation; }
    
    @Override
    public String toString() {
        return "PeopleEnquiry{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", isRegistered=" + isRegistered +
                '}';
    }
}
