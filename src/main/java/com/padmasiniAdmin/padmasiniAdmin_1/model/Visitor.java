package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "visitor")
public class Visitor {
    
    @Id
    private String id;
    
    @Field("name")
    private String name;
    
    @Field("email")
    private String email;
    
    @Field("phone")
    private String phone;
    
    @Field("registeredAt")
    private LocalDateTime registeredAt;
    
    @Field("submittedAt")
    private LocalDateTime submittedAt;
    
    @Field("correctCount")
    private int correctCount;
    
    @Field("wrongCount")
    private int wrongCount;
    
    @Field("skippedCount")
    private int skippedCount;
    
    @Field("score")
    private int score;
    
    @Field("maxScore")
    private int maxScore;
    
    @Field("percentage")
    private double percentage;
    
    @Field("status")
    private String status;
    
    @Field("_class")
    private String _class;

    // Default constructor
    public Visitor() {}

    // Constructor with all fields
    public Visitor(String id, String name, String email, String phone, 
                   LocalDateTime registeredAt, LocalDateTime submittedAt, 
                   int correctCount, int wrongCount, int skippedCount, 
                   int score, int maxScore, double percentage, 
                   String status, String _class) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registeredAt = registeredAt;
        this.submittedAt = submittedAt;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.skippedCount = skippedCount;
        this.score = score;
        this.maxScore = maxScore;
        this.percentage = percentage;
        this.status = status;
        this._class = _class;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }
}