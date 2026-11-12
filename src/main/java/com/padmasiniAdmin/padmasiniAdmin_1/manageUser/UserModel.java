package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String userName;
    private String gmail;
    private String password;
    private String passwordEnc;     // reversible (AES) copy of the password - admin view only
    private String role;            // "teacher" | "admin" | "student"
    private String service;         // for role "admin": comma-separated sections, e.g. "students,blogs" or "full"
    private String boardType;       // cbse / state_board (tutor teachers)
    private String coursetype;
    private String courseName;
    private List<String> standards = new ArrayList<String>();
    private List<String> subjects = new ArrayList<String>();
    private String phoneNumber;

    // Filled at login only (NOT saved to MongoDB): e.g. ["teacher","admin"]
    @Transient
    private List<String> roles = new ArrayList<String>();

    private String firstname;
    private String lastname;
    private String email;
    private String mobile;
    private String dob;
    private String gender;
    private String plan;
    private String startDate;
    private String endDate;
    private String paymentId;
    private String paymentMethod;
    private String amountPaid;
    private String payerId;
    private Integer comfortableDailyHours;
    private String severity;
    private List<String> selectedStandard;
    private Map<String, Object> selectedCourse;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getGmail() { return gmail; }
    public void setGmail(String gmail) { this.gmail = gmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordEnc() { return passwordEnc; }
    public void setPasswordEnc(String passwordEnc) { this.passwordEnc = passwordEnc; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getBoardType() { return boardType; }
    public void setBoardType(String boardType) { this.boardType = boardType; }

    public String getCoursetype() { return coursetype; }
    public void setCoursetype(String coursetype) { this.coursetype = coursetype; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public List<String> getStandards() { return standards; }
    public void setStandards(List<String> standards) { this.standards = standards; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getAmountPaid() { return amountPaid; }
    public void setAmountPaid(String amountPaid) { this.amountPaid = amountPaid; }

    public String getPayerId() { return payerId; }
    public void setPayerId(String payerId) { this.payerId = payerId; }

    public Integer getComfortableDailyHours() { return comfortableDailyHours; }
    public void setComfortableDailyHours(Integer comfortableDailyHours) { this.comfortableDailyHours = comfortableDailyHours; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public List<String> getSelectedStandard() { return selectedStandard; }
    public void setSelectedStandard(List<String> selectedStandard) { this.selectedStandard = selectedStandard; }

    public Map<String, Object> getSelectedCourse() { return selectedCourse; }
    public void setSelectedCourse(Map<String, Object> selectedCourse) { this.selectedCourse = selectedCourse; }

    public static long getSerialversionuid() { return serialVersionUID; }

    // NOTE: password and passwordEnc intentionally NOT included
    @Override
    public String toString() {
        return "UserModel [userName=" + userName + ", gmail=" + gmail + ", role=" + role + ", roles=" + roles
                + ", service=" + service + ", coursetype=" + coursetype + ", courseName=" + courseName
                + ", standards=" + standards + ", subjects=" + subjects + "]";
    }
}