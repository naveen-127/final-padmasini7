package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoClient;

@RestController
@RequestMapping("/api")
public class UserController {
    Map<String, String> map = new HashMap<String, String>();
    
    @Autowired
    private UserService userService;
    
    @Autowired 
    private MongoClient mongoClient;
    
    @PostMapping("/newUser")
    public ResponseEntity<?> addNewUser(@RequestBody UserDTO user) {
        System.out.println("Frontend connection to backend successful");
        System.out.println("Received user data: " + user);
        
        if(userService.saveNewUser(user)) {
            System.out.println("User saved successfully");
            map.put("status", "pass");
        } else {
            map.put("status", "failed");
            map.put("message", "User with this email already exists");
        }
        return ResponseEntity.ok(map);
    }
    
    @GetMapping("/getDefaultData/{courseName}")
    public ResponseEntity<?> getDefaultData(@PathVariable String courseName, 
                                           @RequestParam(required = false) String role) {
        Map<String, Object> response = new HashMap<>();
        response.put("courseName", courseName);
        response.put("role", role != null ? role : "student");
        
        // Get default data based on role
        List<String> defaultSubjects = userService.getDefaultSubjectsPublic(courseName, role != null ? role : "student");
        List<String> defaultStandards = userService.getDefaultStandardsPublic(courseName, role != null ? role : "student");
        
        response.put("defaultSubjects", defaultSubjects);
        response.put("defaultStandards", defaultStandards);
        
        System.out.println("Default data for course: " + courseName + ", role: " + role);
        System.out.println("Subjects: " + defaultSubjects);
        System.out.println("Standards: " + defaultStandards);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUsers")
    public List<UserModel> getUsers(){
        List<UserModel> users = userService.getUsers();
        System.out.println("Retrieved " + users.size() + " users from database");
        return users;
    }
    
    @PutMapping("/updateUser/{gmail}")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO user, @PathVariable("gmail") String gmail) {
        System.out.println("Updating user:");
        System.out.println("  Old email: " + gmail);
        System.out.println("  New email: " + user.getUser().getGmail());
        System.out.println("  Role: " + user.getUser().getRole());
        
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
            // If same email, just update by deleting and recreating
            userService.deleteUser(gmail);
        }
        
        // Save new user with updated data
        boolean saved = userService.saveNewUser(user);
        if (saved) {
            map.put("status", "pass");
            map.put("message", "User updated successfully");
        } else {
            map.put("status", "failed");
            map.put("message", "Failed to update user");
        }
        return ResponseEntity.ok(map);
    }
    
    @DeleteMapping("/deleteUser/{gmail}")
    public ResponseEntity<?> deleteUser(@PathVariable("gmail") String gmail) {
        System.out.println("Deleting user with email: " + gmail);
        userService.deleteUser(gmail);
        map.put("status", "pass");
        map.put("message", "User deleted successfully");
        return ResponseEntity.ok(map);
    }
    
    // Additional endpoint to get users by role
    @GetMapping("/getUsersByRole/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "users");
            Query query = new Query(Criteria.where("role").is(role));
            List<UserModel> users = mongoTemplate.find(query, UserModel.class, "users");
            
            System.out.println("Retrieved " + users.size() + " users with role: " + role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/getAllStudents")
    public ResponseEntity<?> getAllStudents() {
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "users");
            
            // Query the CORRECT collection for students
            Query query = new Query();
            
            // Try different collections - students might be in "studentUserDetail" collection
            List<Document> students = new ArrayList<>();
            
            // Try studentUserDetail collection first
            if (mongoTemplate.collectionExists("studentUserDetail")) {
                System.out.println("Found studentUserDetail collection");
                students = mongoTemplate.findAll(Document.class, "studentUserDetail");
            } 
            // Fallback to users collection with firstname filter
            else if (mongoTemplate.collectionExists("users")) {
                System.out.println("Using users collection with firstname filter");
                query.addCriteria(Criteria.where("firstname").exists(true));
                students = mongoTemplate.find(query, Document.class, "users");
            }
            
            System.out.println("Retrieved " + students.size() + " students from database");
            
            // Convert to a simpler structure for frontend
            List<Map<String, Object>> studentList = new ArrayList<>();
            
            for (Document student : students) {
                Map<String, Object> studentData = new HashMap<>();
                
                // Extract data from Document
                studentData.put("id", student.getObjectId("_id").toString());
                studentData.put("firstname", student.getString("firstname"));
                studentData.put("lastname", student.getString("lastname"));
                studentData.put("fullName", student.getString("firstname") + " " + 
                              (student.getString("lastname") != null ? student.getString("lastname") : ""));
                studentData.put("email", student.getString("email"));
                studentData.put("mobile", student.getString("mobile"));
                studentData.put("password", student.getString("password"));
                studentData.put("dob", student.getString("dob"));
                studentData.put("gender", student.getString("gender"));
                studentData.put("role", "student");
                
                // Course info - handle as Document
                if (student.get("selectedCourse") != null) {
                    studentData.put("selectedCourse", student.get("selectedCourse"));
                }
                
                // Standards and subjects
                studentData.put("standards", student.get("standards") != null ? 
                              student.get("standards") : new ArrayList<>());
                studentData.put("subjects", student.get("subjects") != null ? 
                              student.get("subjects") : new ArrayList<>());
                studentData.put("selectedStandard", student.get("selectedStandard") != null ? 
                              student.get("selectedStandard") : new ArrayList<>());
                
                studentList.add(studentData);
            }
            
            return ResponseEntity.ok(studentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }
}
