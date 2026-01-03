package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
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
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.StudentService; 

import com.mongodb.client.MongoClient;
import com.mongodb.client.result.DeleteResult;
import com.padmasiniAdmin.padmasiniAdmin_1.service.StudentService; // Correct import

@RestController
@RequestMapping("/api")
public class UserController {
    Map<String, String> map = new HashMap<String, String>();
    
    @Autowired
    private UserService userService;
    
    @Autowired 
    private MongoClient mongoClient;
    
    @Autowired  // This is needed if you want to use StudentService
    private StudentService studentService; // Make sure this is properly autowired
    
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
            // Direct approach without StudentService
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            
            // Get all documents from studentUserDetail collection
            Query query = new Query();
            List<Document> documents = mongoTemplate.find(query, Document.class, "studentUserDetail");
            
            System.out.println("Found " + documents.size() + " student documents");
            
            // Convert documents to response format
            List<Map<String, Object>> studentList = new ArrayList<>();
            
            for (Document doc : documents) {
                Map<String, Object> studentData = new HashMap<>();
                
                // Extract _id
                if (doc.get("_id") != null && doc.get("_id") instanceof ObjectId) {
                    ObjectId objectId = (ObjectId) doc.get("_id");
                    studentData.put("_id", objectId.toString());
                    studentData.put("id", objectId.toString());
                }
                
                // Extract all fields
                studentData.put("firstname", doc.getString("firstname"));
                studentData.put("lastname", doc.getString("lastname"));
                
                // Create fullName
                String firstname = doc.getString("firstname") != null ? doc.getString("firstname") : "";
                String lastname = doc.getString("lastname") != null ? doc.getString("lastname") : "";
                studentData.put("fullName", firstname + " " + lastname);
                
                studentData.put("email", doc.getString("email"));
                studentData.put("phone", doc.getString("mobile"));
                studentData.put("mobile", doc.getString("mobile"));
                studentData.put("password", doc.getString("password"));
                studentData.put("dob", doc.getString("dob"));
                studentData.put("gender", doc.getString("gender"));
                studentData.put("role", "student");
                studentData.put("coursetype", doc.getString("coursetype"));
                studentData.put("courseName", doc.getString("courseName"));
                
                // Add other fields if they exist
                if (doc.containsKey("plan")) studentData.put("plan", doc.getString("plan"));
                if (doc.containsKey("startDate")) studentData.put("startDate", doc.getString("startDate"));
                if (doc.containsKey("endDate")) studentData.put("endDate", doc.getString("endDate"));
                if (doc.containsKey("paymentId")) studentData.put("paymentId", doc.getString("paymentId"));
                if (doc.containsKey("paymentMethod")) studentData.put("paymentMethod", doc.getString("paymentMethod"));
                if (doc.containsKey("amountPaid")) studentData.put("amountPaid", doc.getString("amountPaid"));
                if (doc.containsKey("payerId")) studentData.put("payerId", doc.getString("payerId"));
                if (doc.containsKey("comfortableDailyHours")) studentData.put("comfortableDailyHours", doc.getInteger("comfortableDailyHours"));
                if (doc.containsKey("severity")) studentData.put("severity", doc.getString("severity"));
                
                // Lists
                studentData.put("standards", doc.get("standards"));
                studentData.put("selectedStandard", doc.get("selectedStandard"));
                studentData.put("subjects", doc.get("subjects"));
                studentData.put("selectedCourse", doc.get("selectedCourse"));
                
                // Access info for frontend
                Map<String, Object> access = new HashMap<>();
                String coursetype = doc.getString("coursetype");
                access.put("mode", coursetype != null && (coursetype.equalsIgnoreCase("NEET") || coursetype.equalsIgnoreCase("JEE")) ? "professional" : "academics");
                access.put("cardId", doc.getString("courseName") != null ? doc.getString("courseName").toLowerCase() : "");
                access.put("subjects", doc.get("subjects") != null ? doc.get("subjects") : new ArrayList<>());
                access.put("standards", doc.get("selectedStandard") != null ? doc.get("selectedStandard") : new ArrayList<>());
                studentData.put("access", access);
                
                studentList.add(studentData);
            }
            
            return ResponseEntity.ok(studentList);
            
        } catch (Exception e) {
            System.err.println("Error in getAllStudents: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/deleteStudent/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable("id") String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Attempting to delete student with ID/Email: " + id);
            
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            boolean deleted = false;
            
            // Try to delete by ObjectId first
            try {
                Query queryById = new Query(Criteria.where("_id").is(new ObjectId(id)));
                DeleteResult result = mongoTemplate.remove(queryById, "studentUserDetail");
                deleted = result.getDeletedCount() > 0;
                if (deleted) {
                    System.out.println("Student deleted by ID");
                }
            } catch (Exception e) {
                System.out.println("Not a valid ObjectId, trying email...");
            }
            
            // If not deleted by ID, try by email
            if (!deleted) {
                Query queryByEmail = new Query(Criteria.where("email").is(id));
                DeleteResult result = mongoTemplate.remove(queryByEmail, "studentUserDetail");
                deleted = result.getDeletedCount() > 0;
                if (deleted) {
                    System.out.println("Student deleted by email");
                }
            }
            
            if (deleted) {
                response.put("status", "pass");
                response.put("message", "Student deleted successfully");
            } else {
                response.put("status", "failed");
                response.put("message", "Student not found");
            }
            
        } catch (Exception e) {
            System.out.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error deleting student: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // Helper method to check if a document is a student
    private boolean isStudentDocument(Document doc) {
        return doc.containsKey("firstname") || 
               doc.containsKey("email") || 
               (doc.containsKey("role") && "student".equalsIgnoreCase(doc.getString("role")));
    }
}
