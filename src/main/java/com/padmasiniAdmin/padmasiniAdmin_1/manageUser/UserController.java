package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoClient;

@RestController
@RequestMapping("/api")
public class UserController {
    Map<String, String> map = new HashMap<String, String>();
    
    @Autowired
    private UserService userService;
    
    @Autowired 
    private MongoClient mongoClient; // Inject MongoClient here
    
    @PostMapping("/newUser")
    public ResponseEntity<?> addNewUser(@RequestBody UserDTO user) {
        System.out.println("frontend connection to backend successful");
        System.out.println(user);
        if(userService.saveNewUser(user)) {
            System.out.println("code executed successfully");
            map.put("status", "pass");
        } else {
            map.put("status", "failed");
        }
        return ResponseEntity.ok(map);
    }

    @PostMapping("/getUserSubjects")
    public ResponseEntity<?> getUserSubjects(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String userId = request.get("userId");
        String courseName = request.get("courseName");
        
        System.out.println("Getting subjects for user: " + userId + ", course: " + courseName);
        
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "users"); // Use injected mongoClient
            
            // Find user by email (since you're sending gmail from frontend)
            Query query = new Query(Criteria.where("gmail").is(userId));
            
            UserModel user = mongoTemplate.findOne(query, UserModel.class, "users");
            
            if (user != null) {
                System.out.println("User found: " + user.getGmail());
                System.out.println("User course: " + user.getCourseName());
                System.out.println("User subjects: " + user.getSubjects());
                System.out.println("User standards: " + user.getStandards());
                
                // Check if the requested course matches the user's course
                if (courseName != null && courseName.equals(user.getCourseName())) {
                    response.put("subjects", user.getSubjects() != null ? user.getSubjects() : new ArrayList<>());
                    response.put("standards", user.getStandards() != null ? user.getStandards() : new ArrayList<>());
                    response.put("status", "success");
                } else {
                    // Even if course doesn't match, return user's subjects
                    response.put("subjects", user.getSubjects() != null ? user.getSubjects() : new ArrayList<>());
                    response.put("standards", user.getStandards() != null ? user.getStandards() : new ArrayList<>());
                    response.put("status", "success");
                    response.put("message", "Course mismatch, but returning user's data anyway");
                }
            } else {
                System.out.println("User not found with email: " + userId);
                response.put("subjects", new ArrayList<>());
                response.put("standards", new ArrayList<>());
                response.put("status", "failed");
                response.put("message", "User not found");
            }
        } catch (Exception e) {
            System.err.println("Error fetching user subjects: " + e.getMessage());
            e.printStackTrace();
            response.put("subjects", new ArrayList<>());
            response.put("standards", new ArrayList<>());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/migrateUsers")
    public ResponseEntity<?> migrateUsers() {
        try {
            userService.migrateExistingUsers();
            return ResponseEntity.ok("Migration completed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Migration failed: " + e.getMessage());
        }
    }

    @GetMapping("/getDefaultData/{courseName}")
    public ResponseEntity<?> getDefaultData(@PathVariable String courseName) {
        Map<String, Object> response = new HashMap<>();
        response.put("courseName", courseName);
        response.put("defaultSubjects", userService.getDefaultSubjectsPublic(courseName));
        response.put("defaultStandards", userService.getDefaultStandardsPublic(courseName));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUsers")
    public List<UserModel> getUsers(){
        List<UserModel> users = userService.getUsers();
        System.out.println(users);
        return users;
    }
    
    @PutMapping("/updateUser/{gmail}")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO user, @PathVariable("gmail") String gmail) {
        System.out.println("Updating user. Old email: " + gmail + ", New email: " + user.getUser().getGmail());
        
        // If email is being changed, check if new email already exists
        if (!gmail.equals(user.getUser().getGmail())) {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "users");
            Query query = new Query(Criteria.where("gmail").is(user.getUser().getGmail()));
            if (mongoTemplate.exists(query, UserModel.class, "users")) {
                map.put("status", "failed");
                map.put("message", "Email already exists");
                return ResponseEntity.ok(map);
            }
        }
        
        // Delete old user if email is different
        if (!gmail.equals(user.getUser().getGmail())) {
            userService.deleteUser(gmail);
        } else {
            // If same email, just update
            userService.deleteUser(gmail);
        }
        
        // Save new user
        boolean saved = userService.saveNewUser(user);
        if (saved) {
            map.put("status", "pass");
        } else {
            map.put("status", "failed");
        }
        return ResponseEntity.ok(map);
    }
    
    @DeleteMapping("deleteUser/{gmail}")
    public ResponseEntity<?> deleteUser(@PathVariable("gmail") String gmail) {
        System.out.println(gmail);
        userService.deleteUser(gmail);
        map.put("status", "pass");
        return ResponseEntity.ok(map);
    }
}
