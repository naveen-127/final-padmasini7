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
            List<StudentModel> students = studentService.getAllStudents();
            System.out.println("Retrieved " + students.size() + " students");
            
            // Convert to response format
            List<Map<String, Object>> studentList = new ArrayList<>();
            
            for (StudentModel student : students) {
                Map<String, Object> studentData = new HashMap<>();
                
                studentData.put("_id", student.getId());
                studentData.put("id", student.getId());
                studentData.put("firstname", student.getFirstname());
                studentData.put("lastname", student.getLastname());
                studentData.put("fullName", student.getFirstname() + " " + student.getLastname());
                studentData.put("email", student.getEmail());
                studentData.put("phone", student.getMobile());
                studentData.put("mobile", student.getMobile());
                studentData.put("password", student.getPassword());
                studentData.put("dob", student.getDob());
                studentData.put("gender", student.getGender());
                studentData.put("role", "student");
                
                // Course info
                Map<String, Object> courseInfo = new HashMap<>();
                courseInfo.put("type", student.getCoursetype());
                courseInfo.put("name", student.getCourseName());
                studentData.put("selectedCourse", courseInfo);
                
                // Standards
                studentData.put("standards", student.getStandards());
                studentData.put("selectedStandard", student.getSelectedStandard());
                
                // Subjects
                studentData.put("subjects", student.getSubjects());
                
                // Other fields
                studentData.put("coursetype", student.getCoursetype());
                studentData.put("courseName", student.getCourseName());
                studentData.put("plan", student.getPlan());
                studentData.put("startDate", student.getStartDate());
                studentData.put("endDate", student.getEndDate());
                studentData.put("paymentId", student.getPaymentId());
                studentData.put("paymentMethod", student.getPaymentMethod());
                studentData.put("amountPaid", student.getAmountPaid());
                studentData.put("payerId", student.getPayerId());
                studentData.put("comfortableDailyHours", student.getComfortableDailyHours());
                studentData.put("severity", student.getSeverity());
                
                // Access info for frontend
                Map<String, Object> access = new HashMap<>();
                access.put("mode", student.getCoursetype() != null && student.getCoursetype().equalsIgnoreCase("NEET") ? "professional" : "academics");
                access.put("cardId", student.getCourseName() != null ? student.getCourseName().toLowerCase() : "");
                access.put("subjects", student.getSubjects() != null ? student.getSubjects() : new ArrayList<>());
                access.put("standards", student.getSelectedStandard() != null ? student.getSelectedStandard() : new ArrayList<>());
                studentData.put("access", access);
                
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
    
    @DeleteMapping("/deleteStudent/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable("id") String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Attempting to delete student with ID/Email: " + id);
            
            boolean deleted = false;
            
            // Try to delete by ObjectId first
            try {
                deleted = studentService.deleteStudentById(id);
                if (deleted) {
                    System.out.println("Student deleted by ID");
                }
            } catch (Exception e) {
                System.out.println("Not a valid ObjectId, trying email...");
            }
            
            // If not deleted by ID, try by email
            if (!deleted) {
                deleted = studentService.deleteStudentByEmail(id);
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
