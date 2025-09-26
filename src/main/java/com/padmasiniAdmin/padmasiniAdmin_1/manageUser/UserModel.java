package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


}
