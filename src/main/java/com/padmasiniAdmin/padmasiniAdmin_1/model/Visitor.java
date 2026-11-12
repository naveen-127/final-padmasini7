package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps the document in studentUsers.visitor.
 *
 * NOTE: all date fields use Instant (not LocalDateTime). Mongo stores dates in UTC,
 * and Instant is serialized to JSON as "2026-11-15T04:30:00Z", so the browser can
 * convert it to the correct local time (IST). LocalDateTime has no timezone and
 * caused times to be shown 5:30 hours off.
 */
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
    private Instant registeredAt;

    @Field("submittedAt")
    private Instant submittedAt;

    // ---- Free test slot details ----
    @Field("slotId")
    private String slotId;

    @Field("slotDate")
    private String slotDate;          // stored as "2026-11-15"

    @Field("slotStart")
    private Instant slotStart;

    @Field("slotEnd")
    private Instant slotEnd;

    // ---- Enrollment details ----
    @Field("accessToken")
    private String accessToken;

    @Field("enrolledAt")
    private Instant enrolledAt;

    @Field("activities")
    private List<String> activities = new ArrayList<>();

    @Field("primarySource")
    private String primarySource;

    @Field("confirmationSentAt")
    private Instant confirmationSentAt;

    // ---- Test result details ----
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

    public Visitor() {}

    // ---------- Getters and Setters ----------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Instant getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getSlotDate() { return slotDate; }
    public void setSlotDate(String slotDate) { this.slotDate = slotDate; }

    public Instant getSlotStart() { return slotStart; }
    public void setSlotStart(Instant slotStart) { this.slotStart = slotStart; }

    public Instant getSlotEnd() { return slotEnd; }
    public void setSlotEnd(Instant slotEnd) { this.slotEnd = slotEnd; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }

    public List<String> getActivities() { return activities; }
    public void setActivities(List<String> activities) { this.activities = activities; }

    public String getPrimarySource() { return primarySource; }
    public void setPrimarySource(String primarySource) { this.primarySource = primarySource; }

    public Instant getConfirmationSentAt() { return confirmationSentAt; }
    public void setConfirmationSentAt(Instant confirmationSentAt) { this.confirmationSentAt = confirmationSentAt; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getWrongCount() { return wrongCount; }
    public void setWrongCount(int wrongCount) { this.wrongCount = wrongCount; }

    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String get_class() { return _class; }
    public void set_class(String _class) { this._class = _class; }
}