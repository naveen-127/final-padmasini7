package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;

@Service
public class UserService {
    @Autowired
    private MongoClient mongoClient;
    
    String dbName = "users";
    String collectionName = "users";
    
    public boolean saveNewUser(UserDTO user) {
        // Check if user already exists
        if(!checkGmail(user.getUser().getGmail())) {
            UserModel userModel = user.getUser();
            
            // Get role from user model
            String role = userModel.getRole();
            String courseName = userModel.getCourseName();
            
            // AUTO-POPULATE subjects and standards based on course if empty
            if (courseName != null) {
                // Check and populate subjects
                if (userModel.getSubjects() == null || userModel.getSubjects().isEmpty()) {
                    List<String> defaultSubjects = getDefaultSubjects(courseName, role);
                    userModel.setSubjects(defaultSubjects);
                    System.out.println("Auto-populated subjects for " + courseName + " (role: " + role + "): " + defaultSubjects);
                }
                
                // Check and populate standards
                if (userModel.getStandards() == null || userModel.getStandards().isEmpty()) {
                    List<String> defaultStandards = getDefaultStandards(courseName, role);
                    userModel.setStandards(defaultStandards);
                    System.out.println("Auto-populated standards for " + courseName + " (role: " + role + "): " + defaultStandards);
                }
            }
            
            // Log the user being saved
            System.out.println("Saving user:");
            System.out.println("  Name: " + userModel.getUserName());
            System.out.println("  Email: " + userModel.getGmail());
            System.out.println("  Role: " + userModel.getRole());
            System.out.println("  Course Type: " + userModel.getCoursetype());
            System.out.println("  Course Name: " + userModel.getCourseName());
            System.out.println("  Subjects: " + userModel.getSubjects());
            System.out.println("  Standards: " + userModel.getStandards());
            System.out.println("  Phone: " + userModel.getPhoneNumber());
            
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
            mongoTemplate.save(userModel, collectionName);
            System.out.println("Data saved successfully to database");
            return true;
        } else {
            System.out.println("User with email " + user.getUser().getGmail() + " already exists");
            return false;
        }
    }
    
    private boolean checkGmail(String gmail) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
        Query query = new Query(Criteria.where("gmail").is(gmail));
        return mongoTemplate.exists(query, UserModel.class, collectionName);
    }
    
    public List<UserModel> getUsers() {
        MongoTemplate mt = new MongoTemplate(mongoClient, dbName);
        return mt.findAll(UserModel.class, collectionName);
    }

    public void deleteUser(String gmail) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
        System.out.println("Deleting user: " + gmail);
        Query query = new Query(Criteria.where("gmail").is(gmail));
        mongoTemplate.remove(query, UserModel.class, collectionName);
        System.out.println("Deleted " + gmail);
    }
    
    // Updated method to get default subjects based on course AND role
    private List<String> getDefaultSubjects(String courseName, String role) {
        if (courseName == null) return new ArrayList<>();
        
        String courseLower = courseName.toLowerCase();
        
        // Teacher-specific subjects
        if ("teacher".equalsIgnoreCase(role)) {
            switch (courseLower) {
                case "neet":
                    return Arrays.asList("Physics", "Chemistry", "Botany", "Zoology");
                case "jee":
                    return Arrays.asList("Physics", "Chemistry", "Maths");
                case "class11":
                    return Arrays.asList("Physics", "Chemistry", "Biology", "Mathematics");
                case "class12":
                    return Arrays.asList("Physics", "Chemistry", "Biology", "Mathematics");
                default:
                    return new ArrayList<>();
            }
        } 
        // Student-specific subjects
        else {
            switch (courseLower) {
                case "neet":
                    return Arrays.asList("Physics", "Chemistry", "Zoology", "Botany");
                case "jee":
                    return Arrays.asList("Physics", "Chemistry", "Maths");
                case "class1-5":
                    return Arrays.asList("English", "Maths", "Science");
                case "class6-12":
                    return Arrays.asList("Physics", "Chemistry", "Maths", "Biology");
                case "kindergarten":
                    return Arrays.asList("ABCs", "Numbers", "Shapes");
                default:
                    return new ArrayList<>();
            }
        }
    }
    
    // Updated method to get default standards based on course AND role
    private List<String> getDefaultStandards(String courseName, String role) {
        if (courseName == null) return new ArrayList<>();
        
        String courseLower = courseName.toLowerCase();
        
        // Teacher-specific standards
        if ("teacher".equalsIgnoreCase(role)) {
            switch (courseLower) {
                case "neet":
                case "jee":
                    return Arrays.asList("11", "12");
                case "class11":
                    return Arrays.asList("11");
                case "class12":
                    return Arrays.asList("12");
                default:
                    return new ArrayList<>();
            }
        } 
        // Student-specific standards
        else {
            switch (courseLower) {
                case "neet":
                case "jee":
                    return Arrays.asList("11", "12");
                case "class1-5":
                    return Arrays.asList("1", "2", "3", "4", "5");
                case "class6-12":
                    return Arrays.asList("6", "7", "8", "9", "10", "11", "12");
                case "kindergarten":
                    return Arrays.asList("KG");
                default:
                    return new ArrayList<>();
            }
        }
    }
    
    // Public method to get default subjects (updated with role parameter)
    public List<String> getDefaultSubjectsPublic(String courseName, String role) {
        return getDefaultSubjects(courseName, role);
    }
    
    // Public method to get default standards (updated with role parameter)
    public List<String> getDefaultStandardsPublic(String courseName, String role) {
        return getDefaultStandards(courseName, role);
    }
    
    // Updated migration method
    public void migrateExistingUsers() {
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
            List<UserModel> allUsers = mongoTemplate.findAll(UserModel.class, collectionName);
            
            int updatedCount = 0;
            for (UserModel user : allUsers) {
                boolean needsUpdate = false;
                String courseName = user.getCourseName();
                String role = user.getRole() != null ? user.getRole() : "student"; // Default to student if role is null
                
                // Update subjects if empty
                if (courseName != null && (user.getSubjects() == null || user.getSubjects().isEmpty())) {
                    List<String> defaultSubjects = getDefaultSubjects(courseName, role);
                    user.setSubjects(defaultSubjects);
                    needsUpdate = true;
                    System.out.println("Setting default subjects for " + user.getGmail() + " (role: " + role + "): " + defaultSubjects);
                }
                
                // Update standards if empty
                if (courseName != null && (user.getStandards() == null || user.getStandards().isEmpty())) {
                    List<String> defaultStandards = getDefaultStandards(courseName, role);
                    user.setStandards(defaultStandards);
                    needsUpdate = true;
                    System.out.println("Setting default standards for " + user.getGmail() + " (role: " + role + "): " + defaultStandards);
                }
                
                // Update role for existing users if not set
                if (user.getRole() == null || user.getRole().isEmpty()) {
                    user.setRole("student");
                    needsUpdate = true;
                    System.out.println("Setting default role for " + user.getGmail() + ": student");
                }
                
                if (needsUpdate) {
                    mongoTemplate.save(user, collectionName);
                    updatedCount++;
                }
            }
            
            System.out.println("Migration completed. Updated " + updatedCount + " users.");
        } catch (Exception e) {
            System.err.println("Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
