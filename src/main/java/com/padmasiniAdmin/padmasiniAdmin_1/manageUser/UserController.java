// File: /home/ec2-user/final-padmasini7/src/main/java/com/padmasiniAdmin/padmasiniAdmin_1/manageUser/UserController.java
package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.mongodb.client.result.DeleteResult;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {
	    "https://trilokinnovations.com",
	    "https://dafj1druksig9.cloudfront.net",
	    "http://localhost:3000"
	}, 
	allowCredentials = "true",
	maxAge = 3600)
public class UserController {
    Map<String, String> map = new HashMap<String, String>();
    
    @Autowired
    private UserService userService;
    
    @Autowired 
    private MongoClient mongoClient;
    
    // NO StudentService import or autowired field here!
    
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
        
        if (!gmail.equals(user.getUser().getGmail())) {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "users");
            Query query = new Query(Criteria.where("gmail").is(user.getUser().getGmail()));
            if (mongoTemplate.exists(query, UserModel.class, "users")) {
                map.put("status", "failed");
                map.put("message", "Email already exists");
                return ResponseEntity.ok(map);
            }
        }
        
        if (!gmail.equals(user.getUser().getGmail())) {
            userService.deleteUser(gmail);
        } else {
            userService.deleteUser(gmail);
        }
        
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
            System.out.println("=== GET ALL STUDENTS CALLED ===");
            
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            
            if (!mongoTemplate.collectionExists("studentUserDetail")) {
                System.out.println("Collection studentUserDetail does not exist");
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            Query query = new Query();
            List<Document> documents = mongoTemplate.find(query, Document.class, "studentUserDetail");
            
            System.out.println("Found " + documents.size() + " student documents");
            
            List<Map<String, Object>> studentList = new ArrayList<>();
            
            for (Document doc : documents) {
                Map<String, Object> studentData = new HashMap<>();
                
                // ID and Basic Info
                if (doc.get("_id") != null && doc.get("_id") instanceof ObjectId) {
                    ObjectId objectId = (ObjectId) doc.get("_id");
                    studentData.put("_id", objectId.toString());
                    studentData.put("id", objectId.toString());
                }
                
                // Personal Information
                studentData.put("firstname", doc.getString("firstname"));
                studentData.put("lastname", doc.getString("lastname"));
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
                
                // Additional personal fields
                if (doc.containsKey("city")) studentData.put("city", doc.getString("city"));
                if (doc.containsKey("state")) studentData.put("state", doc.getString("state"));
                if (doc.containsKey("photo")) studentData.put("photo", doc.getString("photo"));
                
                // Course Information
                studentData.put("coursetype", doc.getString("coursetype"));
                studentData.put("courseName", doc.getString("courseName"));
                studentData.put("standards", doc.get("standards"));
                studentData.put("selectedStandard", doc.get("selectedStandard"));
                studentData.put("subjects", doc.get("subjects"));
                studentData.put("selectedCourse", doc.get("selectedCourse"));
                
                // Subscription Details
                if (doc.containsKey("plan")) studentData.put("plan", doc.getString("plan"));
                if (doc.containsKey("startDate")) studentData.put("startDate", doc.getString("startDate"));
                if (doc.containsKey("endDate")) studentData.put("endDate", doc.getString("endDate"));
                
                // Payment Information
                if (doc.containsKey("paymentId")) studentData.put("paymentId", doc.getString("paymentId"));
                if (doc.containsKey("paymentMethod")) studentData.put("paymentMethod", doc.getString("paymentMethod"));
                if (doc.containsKey("amountPaid")) studentData.put("amountPaid", doc.getString("amountPaid"));
                if (doc.containsKey("payerId")) studentData.put("payerId", doc.getString("payerId"));
                
                // Coupon and Discount Information
                if (doc.containsKey("couponUsed")) studentData.put("couponUsed", doc.getString("couponUsed"));
                if (doc.containsKey("discountPercentage")) studentData.put("discountPercentage", doc.getString("discountPercentage"));
                if (doc.containsKey("discountAmount")) studentData.put("discountAmount", doc.getString("discountAmount"));
                if (doc.containsKey("action")) studentData.put("action", doc.getString("action"));
                
                // Study Preferences
                if (doc.containsKey("comfortableDailyHours")) {
                    Object hoursObj = doc.get("comfortableDailyHours");
                    if (hoursObj instanceof Integer) {
                        studentData.put("comfortableDailyHours", (Integer) hoursObj);
                    } else if (hoursObj instanceof String) {
                        try {
                            studentData.put("comfortableDailyHours", Integer.parseInt((String) hoursObj));
                        } catch (NumberFormatException e) {
                            studentData.put("comfortableDailyHours", 3); // default
                        }
                    } else {
                        studentData.put("comfortableDailyHours", 3); // default
                    }
                } else {
                    studentData.put("comfortableDailyHours", 3); // default
                }
                
                if (doc.containsKey("severity")) studentData.put("severity", doc.getString("severity"));
                
                // System Information
                if (doc.containsKey("_class")) studentData.put("_class", doc.getString("_class"));
                
                // Payment History
                if (doc.containsKey("paymentHistory")) {
                    studentData.put("paymentHistory", doc.get("paymentHistory"));
                } else {
                    studentData.put("paymentHistory", new ArrayList<>());
                }
                
                // Calculate Access Information
                Map<String, Object> access = new HashMap<>();
                String coursetype = doc.getString("coursetype");
                access.put("mode", coursetype != null && (coursetype.equalsIgnoreCase("NEET") || coursetype.equalsIgnoreCase("JEE")) ? "professional" : "academics");
                access.put("cardId", doc.getString("courseName") != null ? doc.getString("courseName").toLowerCase() : "");
                access.put("subjects", doc.get("subjects") != null ? doc.get("subjects") : new ArrayList<>());
                access.put("standards", doc.get("selectedStandard") != null ? doc.get("selectedStandard") : new ArrayList<>());
                studentData.put("access", access);
                
                // Calculate subscription status and days remaining
                String startDateStr = doc.getString("startDate");
                String endDateStr = doc.getString("endDate");
                
                if (startDateStr != null && endDateStr != null && !startDateStr.isEmpty() && !endDateStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date startDate = sdf.parse(startDateStr);
                        Date endDate = sdf.parse(endDateStr);
                        Date now = new Date();
                        
                        if (now.before(startDate)) {
                            studentData.put("status", "upcoming");
                            long diff = startDate.getTime() - now.getTime();
                            long daysUntilStart = diff / (1000 * 60 * 60 * 24);
                            studentData.put("daysRemaining", daysUntilStart);
                        } else if (now.after(endDate)) {
                            studentData.put("status", "expired");
                            studentData.put("daysRemaining", 0);
                        } else {
                            long diff = endDate.getTime() - now.getTime();
                            long daysRemaining = diff / (1000 * 60 * 60 * 24);
                            studentData.put("daysRemaining", daysRemaining);
                            
                            if (daysRemaining <= 7) {
                                studentData.put("status", "expiring");
                            } else {
                                studentData.put("status", "active");
                            }
                        }
                    } catch (Exception e) {
                        studentData.put("status", "unknown");
                        studentData.put("daysRemaining", null);
                    }
                } else {
                    studentData.put("status", "inactive");
                    studentData.put("daysRemaining", null);
                }
                
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
    
    @PostMapping("/addStudent")
    public ResponseEntity<?> addStudent(@RequestBody Map<String, Object> studentData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== ADDING NEW STUDENT ===");
            System.out.println("Student data received: " + studentData);
            
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            
            // Check if student already exists by email
            String email = (String) studentData.get("email");
            if (email != null && !email.isEmpty()) {
                Query query = new Query(Criteria.where("email").is(email));
                if (mongoTemplate.exists(query, "studentUserDetail")) {
                    response.put("status", "failed");
                    response.put("message", "Student with this email already exists");
                    return ResponseEntity.ok(response);
                }
            }
            
            // Add timestamps
            studentData.put("createdAt", new Date().toString());
            studentData.put("updatedAt", new Date().toString());
            
            // Ensure _class field for Spring Data
            if (!studentData.containsKey("_class")) {
                studentData.put("_class", "com.padmasiniAdmin.padmasiniAdmin_1.manageUser.StudentModel");
            }
            
            // Insert the document
            Document doc = new Document(studentData);
            mongoTemplate.insert(doc, "studentUserDetail");
            
            response.put("status", "pass");
            response.put("message", "Student added successfully");
            response.put("id", doc.get("_id").toString());
            
            System.out.println("Student added successfully with ID: " + doc.get("_id"));
            
        } catch (Exception e) {
            System.err.println("Error adding student: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error adding student: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateStudent/{email}")
    public ResponseEntity<?> updateStudent(@PathVariable("email") String email, @RequestBody Map<String, Object> studentData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== UPDATING STUDENT ===");
            System.out.println("Updating student with email: " + email);
            System.out.println("Update data: " + studentData);
            
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            
            // Find the student by email
            Query query = new Query(Criteria.where("email").is(email));
            
            // Check if student exists
            if (!mongoTemplate.exists(query, "studentUserDetail")) {
                response.put("status", "failed");
                response.put("message", "Student not found with email: " + email);
                return ResponseEntity.ok(response);
            }
            
            // Check if trying to update email to a new one that already exists
            String newEmail = (String) studentData.get("email");
            if (newEmail != null && !newEmail.equals(email)) {
                Query emailCheckQuery = new Query(Criteria.where("email").is(newEmail));
                if (mongoTemplate.exists(emailCheckQuery, "studentUserDetail")) {
                    response.put("status", "failed");
                    response.put("message", "Another student with this email already exists");
                    return ResponseEntity.ok(response);
                }
            }
            
            // Remove _id if present to avoid conflicts
            studentData.remove("_id");
            studentData.remove("id");
            
            // Add update timestamp
            studentData.put("updatedAt", new Date().toString());
            
            // Create update document using org.springframework.data.mongodb.core.query.Update
            org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update();
            
            // Set all fields from studentData
            for (Map.Entry<String, Object> entry : studentData.entrySet()) {
                update.set(entry.getKey(), entry.getValue());
            }
            
            // Update the student
            mongoTemplate.updateFirst(query, update, "studentUserDetail");
            
            response.put("status", "pass");
            response.put("message", "Student updated successfully");
            
            System.out.println("Student updated successfully");
            
        } catch (Exception e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error updating student: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    // Optional: Add endpoint to get single student by email
    @GetMapping("/getStudent/{email}")
    public ResponseEntity<?> getStudentByEmail(@PathVariable("email") String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Getting student with email: " + email);
            
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            Query query = new Query(Criteria.where("email").is(email));
            Document student = mongoTemplate.findOne(query, Document.class, "studentUserDetail");
            
            if (student != null) {
                // Convert ObjectId to string
                if (student.get("_id") instanceof ObjectId) {
                    student.put("_id", ((ObjectId) student.get("_id")).toString());
                    student.put("id", ((ObjectId) student.get("_id")).toString());
                }
                
                response.put("status", "pass");
                response.put("data", student);
            } else {
                response.put("status", "failed");
                response.put("message", "Student not found");
            }
            
        } catch (Exception e) {
            System.err.println("Error getting student: " + e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/deleteStudent/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable("id") String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Attempting to delete student with ID/Email: " + id);
            
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            boolean deleted = false;
            
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
}
