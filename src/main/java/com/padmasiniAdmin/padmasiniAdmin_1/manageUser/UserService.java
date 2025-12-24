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
        // Only check if email already exists
        if(!checkGmail(user.getUser().getGmail())) {
            UserModel userModel = user.getUser();
            
            // AUTO-POPULATE subjects and standards based on course if empty
            String courseName = userModel.getCourseName();
            if (courseName != null) {
                // Check and populate subjects
                if (userModel.getSubjects() == null || userModel.getSubjects().isEmpty()) {
                    List<String> defaultSubjects = getDefaultSubjects(courseName);
                    userModel.setSubjects(defaultSubjects);
                    System.out.println("Auto-populated subjects for " + courseName + ": " + defaultSubjects);
                }
                
                // Check and populate standards
                if (userModel.getStandards() == null || userModel.getStandards().isEmpty()) {
                    List<String> defaultStandards = getDefaultStandards(courseName);
                    userModel.setStandards(defaultStandards);
                    System.out.println("Auto-populated standards for " + courseName + ": " + defaultStandards);
                }
            }
            
            System.out.println("Saving user with email: " + userModel.getGmail());
            System.out.println("Course: " + userModel.getCourseName());
            System.out.println("Subjects: " + userModel.getSubjects());
            System.out.println("Standards: " + userModel.getStandards());
            
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
    
    // Helper method to get default subjects based on course
    private List<String> getDefaultSubjects(String courseName) {
        if (courseName == null) return new ArrayList<>();
        
        switch (courseName.toLowerCase()) {
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
                // Return empty list for unknown courses
                return new ArrayList<>();
        }
    }
    
    // Helper method to get default standards based on course
    private List<String> getDefaultStandards(String courseName) {
        if (courseName == null) return new ArrayList<>();
        
        switch (courseName.toLowerCase()) {
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
                // Return empty list for unknown courses
                return new ArrayList<>();
        }
    }
    
    // Public method to update existing users (for migration)
    public void migrateExistingUsers() {
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
            List<UserModel> allUsers = mongoTemplate.findAll(UserModel.class, collectionName);
            
            int updatedCount = 0;
            for (UserModel user : allUsers) {
                boolean needsUpdate = false;
                String courseName = user.getCourseName();
                
                // Update subjects if empty
                if (courseName != null && (user.getSubjects() == null || user.getSubjects().isEmpty())) {
                    List<String> defaultSubjects = getDefaultSubjects(courseName);
                    user.setSubjects(defaultSubjects);
                    needsUpdate = true;
                    System.out.println("Setting default subjects for " + user.getGmail() + ": " + defaultSubjects);
                }
                
                // Update standards if empty
                if (courseName != null && (user.getStandards() == null || user.getStandards().isEmpty())) {
                    List<String> defaultStandards = getDefaultStandards(courseName);
                    user.setStandards(defaultStandards);
                    needsUpdate = true;
                    System.out.println("Setting default standards for " + user.getGmail() + ": " + defaultStandards);
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
    
    // Public method to get default subjects (can be used by controller if needed)
    public List<String> getDefaultSubjectsPublic(String courseName) {
        return getDefaultSubjects(courseName);
    }
    
    // Public method to get default standards (can be used by controller if needed)
    public List<String> getDefaultStandardsPublic(String courseName) {
        return getDefaultStandards(courseName);
    }
}
