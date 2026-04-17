package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Document(collection = "studentUserDetail")
public class StudentModel {
    
    @Id
    private String id;
    
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String mobile;
    private String coursetype;
    private String courseName;
    private List<String> standards;
    private List<String> subjects;
    private Map<String, Object> selectedCourse;
    private List<String> selectedStandard;
    private String photo;
    private String dob;
    private String gender;
    private String city;
    private String state;
    private String plan;
    private String startDate;
    private String endDate;
    private String paymentId;
    private String paymentMethod;
    private String amountPaid;
    private String payerId;
    private String couponUsed;
    private String discountPercentage;
    private String discountAmount;
    private String action;
    private Integer comfortableDailyHours;
    private String severity;
    private List<Map<String, Object>> paymentHistory;
    private String _class;
    
    // Constructors
    public StudentModel() {
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFirstname() {
        return firstname;
    }
    
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    
    public String getLastname() {
        return lastname;
    }
    
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getCoursetype() {
        return coursetype;
    }
    
    public void setCoursetype(String coursetype) {
        this.coursetype = coursetype;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public List<String> getStandards() {
        return standards;
    }
    
    public void setStandards(List<String> standards) {
        this.standards = standards;
    }
    
    public List<String> getSubjects() {
        return subjects;
    }
    
    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }
    
    public Map<String, Object> getSelectedCourse() {
        return selectedCourse;
    }
    
    public void setSelectedCourse(Map<String, Object> selectedCourse) {
        this.selectedCourse = selectedCourse;
    }
    
    public List<String> getSelectedStandard() {
        return selectedStandard;
    }
    
    public void setSelectedStandard(List<String> selectedStandard) {
        this.selectedStandard = selectedStandard;
    }
    
    public String getPhoto() {
        return photo;
    }
    
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    
    public String getDob() {
        return dob;
    }
    
    public void setDob(String dob) {
        this.dob = dob;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPlan() {
        return plan;
    }
    
    public void setPlan(String plan) {
        this.plan = plan;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getAmountPaid() {
        return amountPaid;
    }
    
    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }
    
    public String getPayerId() {
        return payerId;
    }
    
    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }
    
    public String getCouponUsed() {
        return couponUsed;
    }
    
    public void setCouponUsed(String couponUsed) {
        this.couponUsed = couponUsed;
    }
    
    public String getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(String discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public String getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Integer getComfortableDailyHours() {
        return comfortableDailyHours;
    }
    
    public void setComfortableDailyHours(Integer comfortableDailyHours) {
        this.comfortableDailyHours = comfortableDailyHours;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public List<Map<String, Object>> getPaymentHistory() {
        return paymentHistory;
    }
    
    public void setPaymentHistory(List<Map<String, Object>> paymentHistory) {
        this.paymentHistory = paymentHistory;
    }
    
    public String get_class() {
        return _class;
    }
    
    public void set_class(String _class) {
        this._class = _class;
    }
    
    @Override
    public String toString() {
        return "StudentModel [id=" + id + ", firstname=" + firstname + ", email=" + email + ", course=" + courseName + "]";
    }
}
