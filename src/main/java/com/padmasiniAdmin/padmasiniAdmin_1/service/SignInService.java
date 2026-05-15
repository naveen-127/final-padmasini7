package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;
import com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder;

@Service
public class SignInService {
    
    @Autowired
    private MongoClient mongoClient;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Find user by username (without password check)
    public UserModel findUserByUserName(String userName) {
        MongoTemplate mongo = new MongoTemplate(mongoClient, "users");
        Query query = new Query(Criteria.where("userName").is(userName));
        return mongo.findOne(query, UserModel.class, "users");
    }
    
    // Find user by email (without password check)
    public UserModel findUserByEmail(String email) {
        MongoTemplate mongo = new MongoTemplate(mongoClient, "users");
        Query query = new Query(Criteria.where("gmail").is(email));
        return mongo.findOne(query, UserModel.class, "users");
    }
    
    // Updated method that supports both plain text and hashed passwords
    public UserModel checkUserName(String userName, String password) {
        UserModel user = findUserByUserName(userName);
        if (user != null) {
            String dbPassword = user.getPassword();
            System.out.println("=== LOGIN ATTEMPT ===");
            System.out.println("User: " + userName);
            System.out.println("DB Password starts with $2? " + (dbPassword != null && dbPassword.startsWith("$2")));
            
            // Check if password is BCrypt hash or plain text
            if (dbPassword != null && dbPassword.startsWith("$2")) {
                // BCrypt hash - use matches
                if (passwordEncoder.matches(password, dbPassword)) {
                    System.out.println("✅ Password matched (BCrypt)");
                    return user;
                } else {
                    System.out.println("❌ Password mismatch (BCrypt)");
                }
            } else {
                // Plain text - direct compare (for existing users)
                System.out.println("Plain text password detected in DB");
                if (password.equals(dbPassword)) {
                    System.out.println("✅ Password matched (plain text) - Migrating to hash...");
                    // Migrate to hash on successful login
                    String hashedPassword = passwordEncoder.encodePassword(password);
                    user.setPassword(hashedPassword);
                    MongoTemplate mongo = new MongoTemplate(mongoClient, "users");
                    mongo.save(user, "users");
                    System.out.println("✅ Password migrated to BCrypt hash for: " + userName);
                    return user;
                } else {
                    System.out.println("❌ Password mismatch (plain text)");
                }
            }
        }
        System.out.println("No user found for username: " + userName);
        return null;
    }
    
    // Updated method that supports both plain text and hashed passwords
    public UserModel checkUserGmail(String gmail, String password) {
        UserModel user = findUserByEmail(gmail);
        if (user != null) {
            String dbPassword = user.getPassword();
            System.out.println("=== LOGIN ATTEMPT (Email) ===");
            System.out.println("Email: " + gmail);
            System.out.println("DB Password starts with $2? " + (dbPassword != null && dbPassword.startsWith("$2")));
            
            // Check if password is BCrypt hash or plain text
            if (dbPassword != null && dbPassword.startsWith("$2")) {
                // BCrypt hash - use matches
                if (passwordEncoder.matches(password, dbPassword)) {
                    System.out.println("✅ Password matched (BCrypt)");
                    return user;
                } else {
                    System.out.println("❌ Password mismatch (BCrypt)");
                }
            } else {
                // Plain text - direct compare (for existing users)
                System.out.println("Plain text password detected in DB");
                if (password.equals(dbPassword)) {
                    System.out.println("✅ Password matched (plain text) - Migrating to hash...");
                    // Migrate to hash on successful login
                    String hashedPassword = passwordEncoder.encodePassword(password);
                    user.setPassword(hashedPassword);
                    MongoTemplate mongo = new MongoTemplate(mongoClient, "users");
                    mongo.save(user, "users");
                    System.out.println("✅ Password migrated to BCrypt hash for: " + gmail);
                    return user;
                } else {
                    System.out.println("❌ Password mismatch (plain text)");
                }
            }
        }
        System.out.println("No user found for email: " + gmail);
        return null;
    }
    
    // Private helper methods (keeping your original structure)
    private UserModel checkDbForUserGmail(String gmail) {
        MongoTemplate mongo = new MongoTemplate(mongoClient, "users");
        Query query = new Query(Criteria.where("gmail").is(gmail));
        return mongo.findOne(query, UserModel.class, "users");
    }
    
    private UserModel checkDbForUser(String userName) {
        MongoTemplate mongo = new MongoTemplate(mongoClient, "users");
        Query query = new Query(Criteria.where("userName").is(userName));
        return mongo.findOne(query, UserModel.class, "users");
    }
}