package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UserModel implements Serializable {
	private static final long serialVersionUID = 1L;//suma
private String userName;
private String gmail;
private String password;
private String role;
private String coursetype;
private String courseName;
private List<String>standards=new ArrayList<String>();
private List<String> subjects=new ArrayList<String>();
private String phoneNumber;
private String id;
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

public void setId(String id) {
	this.id = id;
}

public void setFirstname(String firstname) {
	this.firstname = firstname;
}

public void setLastname(String lastname) {
	this.lastname = lastname;
}

public void setEmail(String email) {
	this.email = email;
}

public void setMobile(String mobile) {
	this.mobile = mobile;
}

public void setDob(String dob) {
	this.dob = dob;
}

public void setGender(String gender) {
	this.gender = gender;
}

public void setSelectedStandard(List<String> selectedStandard) {
	this.selectedStandard = selectedStandard;
}

public void setSelectedCourse(Map<String, Object> selectedCourse) {
	this.selectedCourse = selectedCourse;
}

public String getPhoneNumber() {
	return phoneNumber;
}

public void setPhoneNumber(String phoneNumber) {
	this.phoneNumber = phoneNumber;
}

public String getPassword() {
	return password;
}

public void setPassword(String password) {
	this.password = password;
}

public String getUserName() {
	return userName;
}
public void setUserName(String userName) {
	this.userName = userName;
}
public String getGmail() {
	return gmail;
}
public void setGmail(String gmail) {
	this.gmail = gmail;
}
public String getRole() {
	return role;
}
public void setRole(String role) {
	this.role = role;
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

@Override
public String toString() {
	return "UserModel [userName=" + userName + ", gmail=" + gmail + ", password=" + password + ", role=" + role
			+ ", coursetype=" + coursetype + ", courseName=" + courseName + ", standards=" + standards + ", subjects="
			+ subjects + "]";
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

public static long getSerialversionuid() {
	return serialVersionUID;
}

public Object getId() {
	// TODO Auto-generated method stub
	return null;
}

public Object getFirstname() {
	// TODO Auto-generated method stub
	return null;
}

public Object getLastname() {
	// TODO Auto-generated method stub
	return null;
}

public Object getEmail() {
	// TODO Auto-generated method stub
	return null;
}

public Object getMobile() {
	// TODO Auto-generated method stub
	return null;
}

public Object getDob() {
	// TODO Auto-generated method stub
	return null;
}

public Object getGender() {
	// TODO Auto-generated method stub
	return null;
}

public Object getSelectedCourse() {
	// TODO Auto-generated method stub
	return null;
}

public Object getSelectedStandard() {
	// TODO Auto-generated method stub
	return null;
}


}
