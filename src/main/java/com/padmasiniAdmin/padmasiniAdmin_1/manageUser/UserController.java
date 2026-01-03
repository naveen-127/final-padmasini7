package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

import com.mongodb.client.MongoClient;
import com.mongodb.client.result.DeleteResult;

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
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            
            // Query using Document class instead of UserModel
            Query query = new Query();
            List<Document> students = mongoTemplate.find(query, Document.class, "studentUserDetail");
            
            System.out.println("Retrieved " + students.size() + " students");
            
            // Convert to List of Maps
            List<Map<String, Object>> studentList = new ArrayList<>();
            
            for (Document student : students) {
                Map<String, Object> studentData = new HashMap<>();
                
                // Get ObjectId properly
                ObjectId objectId = student.getObjectId("_id");
                if (objectId != null) {
                    studentData.put("id", objectId.toString());
                }
                
                // Map all fields directly
                studentData.put("firstname", student.getString("firstname"));
                studentData.put("lastname", student.getString("lastname"));
                studentData.put("email", student.getString("email"));
                studentData.put("mobile", student.getString("mobile"));
                studentData.put("password", student.getString("password"));
                studentData.put("dob", student.getString("dob"));
                studentData.put("gender", student.getString("gender"));
                studentData.put("role", "student");
                
                // Full name
                String fullName = (student.getString("firstname") != null ? student.getString("firstname") : "") + " " + 
                                 (student.getString("lastname") != null ? student.getString("lastname") : "");
                studentData.put("fullName", fullName.trim());
                
                // Course info
                if (student.get("selectedCourse") != null) {
                    studentData.put("selectedCourse", student.get("selectedCourse"));
                }
                
                // Handle arrays/lists
                studentData.put("selectedStandard", student.get("selectedStandard") != null ? 
                              student.get("selectedStandard") : new ArrayList<>());
                studentData.put("subjects", student.get("subjects") != null ? 
                              student.get("subjects") : new ArrayList<>());
                studentData.put("standards", student.get("standards") != null ? 
                              student.get("standards") : new ArrayList<>());
                
                studentList.add(studentData);
            }
            
            return ResponseEntity.ok(studentList);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error fetching students: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/deleteStudent/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable("id") String id) {
        System.out.println("=== DELETE STUDENT CALLED ===");
        System.out.println("Student ID to delete: " + id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // List of possible database/collection combinations for students
            String[][] possibleLocations = {
                {"studentUsers", "studentUserDetail"},
                {"studentUsers", "studentUsers"},
                {"users", "studentUserDetail"},
                {"users", "studentUsers"}
            };
            
            boolean deleted = false;
            String deletedFrom = "";
            
            // Try each possible location
            for (String[] location : possibleLocations) {
                String dbName = location[0];
                String collectionName = location[1];
                
                try {
                    MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
                    
                    if (mongoTemplate.collectionExists(collectionName)) {
                        // Try to delete by ObjectId
                        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
                        
                        // If ObjectId fails, try by email or other fields
                        if (mongoTemplate.exists(query, Document.class, collectionName)) {
                            DeleteResult result = mongoTemplate.remove(query, collectionName);
                            if (result.getDeletedCount() > 0) {
                                deleted = true;
                                deletedFrom = dbName + "." + collectionName;
                                System.out.println("Deleted student from: " + deletedFrom);
                                break;
                            }
                        }
                        
                        // Also try deleting by email field (if ID is actually email)
                        Query emailQuery = new Query(Criteria.where("email").is(id));
                        if (mongoTemplate.exists(emailQuery, Document.class, collectionName)) {
                            DeleteResult result = mongoTemplate.remove(emailQuery, collectionName);
                            if (result.getDeletedCount() > 0) {
                                deleted = true;
                                deletedFrom = dbName + "." + collectionName;
                                System.out.println("Deleted student by email from: " + deletedFrom);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error trying " + dbName + "." + collectionName + ": " + e.getMessage());
                }
            }
            
            if (deleted) {
                response.put("status", "pass");
                response.put("message", "Student deleted successfully from " + deletedFrom);
            } else {
                response.put("status", "failed");
                response.put("message", "Student not found in any known collection");
                
                // Try one more approach: check all collections
                System.out.println("Trying exhaustive search...");
                for (String dbName : mongoClient.listDatabaseNames()) {
                    try {
                        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
                        Set<String> collections = mongoTemplate.getCollectionNames();
                        
                        for (String collection : collections) {
                            Query query = new Query(new Criteria().orOperator(
                                Criteria.where("_id").is(id),
                                Criteria.where("email").is(id),
                                Criteria.where("gmail").is(id)
                            ));
                            
                            if (mongoTemplate.exists(query, Document.class, collection)) {
                                mongoTemplate.remove(query, collection);
                                response.put("status", "pass");
                                response.put("message", "Student deleted from " + dbName + "." + collection);
                                System.out.println("Found and deleted from " + dbName + "." + collection);
                                return ResponseEntity.ok(response);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error checking " + dbName + ": " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error deleting student: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
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
