// File: /home/ec2-user/final-padmasini7/src/main/java/com/padmasiniAdmin/padmasiniAdmin_1/manageUser/UserController.java
package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
import org.springframework.web.bind.annotation.RequestMethod;

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
                
                if (doc.get("_id") != null && doc.get("_id") instanceof ObjectId) {
                    ObjectId objectId = (ObjectId) doc.get("_id");
                    studentData.put("_id", objectId.toString());
                    studentData.put("id", objectId.toString());
                }
                
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
                
                if (doc.containsKey("city")) studentData.put("city", doc.getString("city"));
                if (doc.containsKey("state")) studentData.put("state", doc.getString("state"));
                if (doc.containsKey("photo")) studentData.put("photo", doc.getString("photo"));
                
                studentData.put("coursetype", doc.getString("coursetype"));
                studentData.put("courseName", doc.getString("courseName"));
                studentData.put("standards", doc.get("standards"));
                studentData.put("selectedStandard", doc.get("selectedStandard"));
                studentData.put("subjects", doc.get("subjects"));
                studentData.put("selectedCourse", doc.get("selectedCourse"));
                
                if (doc.containsKey("plan")) studentData.put("plan", doc.getString("plan"));
                if (doc.containsKey("startDate")) studentData.put("startDate", doc.getString("startDate"));
                if (doc.containsKey("endDate")) studentData.put("endDate", doc.getString("endDate"));
                
                if (doc.containsKey("paymentId")) studentData.put("paymentId", doc.getString("paymentId"));
                if (doc.containsKey("paymentMethod")) studentData.put("paymentMethod", doc.getString("paymentMethod"));
                if (doc.containsKey("amountPaid")) studentData.put("amountPaid", doc.getString("amountPaid"));
                if (doc.containsKey("payerId")) studentData.put("payerId", doc.getString("payerId"));
                
                if (doc.containsKey("couponUsed")) studentData.put("couponUsed", doc.getString("couponUsed"));
                if (doc.containsKey("discountPercentage")) studentData.put("discountPercentage", doc.getString("discountPercentage"));
                if (doc.containsKey("discountAmount")) studentData.put("discountAmount", doc.getString("discountAmount"));
                if (doc.containsKey("action")) studentData.put("action", doc.getString("action"));
                
                if (doc.containsKey("comfortableDailyHours")) {
                    Object hoursObj = doc.get("comfortableDailyHours");
                    if (hoursObj instanceof Integer) {
                        studentData.put("comfortableDailyHours", (Integer) hoursObj);
                    } else if (hoursObj instanceof String) {
                        try {
                            studentData.put("comfortableDailyHours", Integer.parseInt((String) hoursObj));
                        } catch (NumberFormatException e) {
                            studentData.put("comfortableDailyHours", 3);
                        }
                    } else {
                        studentData.put("comfortableDailyHours", 3);
                    }
                } else {
                    studentData.put("comfortableDailyHours", 3);
                }
                
                if (doc.containsKey("severity")) studentData.put("severity", doc.getString("severity"));
                
                if (doc.containsKey("_class")) studentData.put("_class", doc.getString("_class"));
                
                if (doc.containsKey("paymentHistory")) {
                    studentData.put("paymentHistory", doc.get("paymentHistory"));
                } else {
                    studentData.put("paymentHistory", new ArrayList<>());
                }
                
                Map<String, Object> access = new HashMap<>();
                String coursetype = doc.getString("coursetype");
                access.put("mode", coursetype != null && (coursetype.equalsIgnoreCase("NEET") || coursetype.equalsIgnoreCase("JEE")) ? "professional" : "academics");
                access.put("cardId", doc.getString("courseName") != null ? doc.getString("courseName").toLowerCase() : "");
                access.put("subjects", doc.get("subjects") != null ? doc.get("subjects") : new ArrayList<>());
                access.put("standards", doc.get("selectedStandard") != null ? doc.get("selectedStandard") : new ArrayList<>());
                studentData.put("access", access);
                
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
            
            String email = (String) studentData.get("email");
            if (email != null && !email.isEmpty()) {
                Query query = new Query(Criteria.where("email").is(email));
                if (mongoTemplate.exists(query, "studentUserDetail")) {
                    response.put("status", "failed");
                    response.put("message", "Student with this email already exists");
                    return ResponseEntity.ok(response);
                }
            }
            
            studentData.put("createdAt", new Date().toString());
            studentData.put("updatedAt", new Date().toString());
            
            if (!studentData.containsKey("_class")) {
                studentData.put("_class", "com.padmasiniAdmin.padmasiniAdmin_1.manageUser.StudentModel");
            }
            
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
            
            Query query = new Query(Criteria.where("email").is(email));
            
            if (!mongoTemplate.exists(query, "studentUserDetail")) {
                response.put("status", "failed");
                response.put("message", "Student not found with email: " + email);
                return ResponseEntity.ok(response);
            }
            
            String newEmail = (String) studentData.get("email");
            if (newEmail != null && !newEmail.equals(email)) {
                Query emailCheckQuery = new Query(Criteria.where("email").is(newEmail));
                if (mongoTemplate.exists(emailCheckQuery, "studentUserDetail")) {
                    response.put("status", "failed");
                    response.put("message", "Another student with this email already exists");
                    return ResponseEntity.ok(response);
                }
            }
            
            studentData.remove("_id");
            studentData.remove("id");
            studentData.put("updatedAt", new Date().toString());
            
            org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update();
            
            for (Map.Entry<String, Object> entry : studentData.entrySet()) {
                update.set(entry.getKey(), entry.getValue());
            }
            
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
    
    @GetMapping("/getStudent/{email}")
    public ResponseEntity<?> getStudentByEmail(@PathVariable("email") String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Getting student with email: " + email);
            
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            Query query = new Query(Criteria.where("email").is(email));
            Document student = mongoTemplate.findOne(query, Document.class, "studentUserDetail");
            
            if (student != null) {
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
    
    @GetMapping("/simpleSessions/{studentId}")
    public ResponseEntity<?> simpleSessions(@PathVariable String studentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== SIMPLE SESSIONS FOR STUDENT: " + studentId);
            
            MongoTemplate sessionTemplate = new MongoTemplate(mongoClient, "subjects");
            
            Query query = new Query(Criteria.where("userId").is(studentId));
            List<Document> sessions = sessionTemplate.find(query, Document.class, "study_sessions");
            
            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (Document session : sessions) {
                Map<String, Object> sessionData = new HashMap<>();
                if (session.get("_id") instanceof ObjectId) {
                    sessionData.put("_id", ((ObjectId) session.get("_id")).toString());
                }
                sessionData.put("userId", session.getString("userId"));
                sessionData.put("loginTime", session.getDate("loginTime"));
                sessionData.put("logoutTime", session.getDate("logoutTime"));
                sessionData.put("durationInMinutes", session.getInteger("durationInMinutes"));
                sessionList.add(sessionData);
            }
            
            response.put("status", "pass");
            response.put("sessions", sessionList);
            response.put("count", sessionList.size());
            
            int totalMinutes = sessionList.stream().mapToInt(s -> (Integer) s.get("durationInMinutes")).sum();
            response.put("totalMinutes", totalMinutes);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    
 // Endpoint to get student progress dynamically - FIXED for nested PERCENTAGE_ fields
    @GetMapping("/getStudentProgressDynamic/{studentId}")
    public ResponseEntity<?> getStudentProgressDynamic(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== GETTING STUDENT PROGRESS FOR ID: " + studentId);
            
            MongoTemplate progressTemplate = new MongoTemplate(mongoClient, "subjects");
            Query query = new Query(Criteria.where("userId").is(studentId));
            Document progress = progressTemplate.findOne(query, Document.class, "Progress");
            
            if (progress == null) {
                System.out.println("No progress found for student: " + studentId);
                response.put("status", "pass");
                response.put("hasProgress", false);
                response.put("message", "No progress records found");
                return ResponseEntity.ok(response);
            }
            
            Map<String, Map<String, List<Map<String, Object>>>> standardData = new LinkedHashMap<>();
            standardData.put("11th", new LinkedHashMap<>());
            standardData.put("12th", new LinkedHashMap<>());
            
            String[] subjects = {"Physics", "Chemistry", "Botany", "Zoology"};
            for (String standard : standardData.keySet()) {
                for (String subject : subjects) {
                    standardData.get(standard).put(subject, new ArrayList<>());
                }
            }
            
            // Recursively extract PERCENTAGE_ fields from the entire document
            extractPercentageFields(progress, standardData);
            
            // Also check completedSubtopics for completed lessons
            Document completedSubtopics = (Document) progress.get("completedSubtopics");
            if (completedSubtopics != null) {
                extractCompletedLessons(completedSubtopics, standardData);
            }
            
            // Build the response structure for frontend
            Map<String, Object> neetCourse = new LinkedHashMap<>();
            neetCourse.put("name", "NEET");
            
            Map<String, Object> standardsData = new LinkedHashMap<>();
            int totalOverallLessons = 0;
            int totalOverallPercentage = 0;
            
            for (String standard : new String[]{"11th", "12th"}) {
                Map<String, Object> standardObj = new LinkedHashMap<>();
                Map<String, List<Map<String, Object>>> rawSubjectsData = standardData.get(standard);
                Map<String, Object> subjectsData = new LinkedHashMap<>();
                
                int standardTotalLessons = 0;
                int standardTotalPercentage = 0;
                
                for (String subject : subjects) {
                    List<Map<String, Object>> lessons = rawSubjectsData.get(subject);
                    
                    // Remove duplicates (by lesson name)
                    Map<String, Map<String, Object>> uniqueLessons = new LinkedHashMap<>();
                    for (Map<String, Object> lesson : lessons) {
                        String name = (String) lesson.get("name");
                        if (!uniqueLessons.containsKey(name) || 
                            (Integer) uniqueLessons.get(name).get("percentage") < (Integer) lesson.get("percentage")) {
                            uniqueLessons.put(name, lesson);
                        }
                    }
                    
                    List<Map<String, Object>> cleanedLessons = new ArrayList<>(uniqueLessons.values());
                    
                    // Sort lessons by name
                    cleanedLessons.sort((a, b) -> {
                        String nameA = (String) a.get("name");
                        String nameB = (String) b.get("name");
                        return nameA.compareTo(nameB);
                    });
                    
                    // Calculate subject progress
                    int subjectTotalPct = 0;
                    int completedCount = 0;
                    for (Map<String, Object> lesson : cleanedLessons) {
                        int pct = (Integer) lesson.get("percentage");
                        subjectTotalPct += pct;
                        if (pct >= 100) completedCount++;
                        standardTotalPercentage += pct;
                    }
                    
                    int subjectProgress = cleanedLessons.size() > 0 ? subjectTotalPct / cleanedLessons.size() : 0;
                    
                    Map<String, Object> subjectData = new LinkedHashMap<>();
                    subjectData.put("name", subject);
                    subjectData.put("progress", subjectProgress);
                    subjectData.put("completedLessons", completedCount);
                    subjectData.put("totalLessons", cleanedLessons.size());
                    subjectData.put("lessons", cleanedLessons);
                    
                    subjectsData.put(subject, subjectData);
                    standardTotalLessons += cleanedLessons.size();
                }
                
                int standardProgress = standardTotalLessons > 0 ? standardTotalPercentage / standardTotalLessons : 0;
                
                standardObj.put("progress", standardProgress);
                standardObj.put("totalLessons", standardTotalLessons);
                standardObj.put("subjects", subjectsData);
                
                standardsData.put(standard, standardObj);
                totalOverallLessons += standardTotalLessons;
                totalOverallPercentage += standardTotalPercentage;
            }
            
            neetCourse.put("standards", standardsData);
            neetCourse.put("overallProgress", totalOverallLessons > 0 ? totalOverallPercentage / totalOverallLessons : 0);
            neetCourse.put("totalLessons", totalOverallLessons);
            
            Map<String, Object> hierarchicalProgress = new LinkedHashMap<>();
            hierarchicalProgress.put("NEET", neetCourse);
            
            response.put("status", "pass");
            response.put("hasProgress", true);
            response.put("hierarchicalProgress", hierarchicalProgress);
            
            System.out.println("Returning progress: " + totalOverallLessons + " total lessons");
            
        } catch (Exception e) {
            System.err.println("Error fetching student progress: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // NEW METHOD: Recursively extract PERCENTAGE_ fields from nested documents
    private void extractPercentageFields(Document doc, Map<String, Map<String, List<Map<String, Object>>>> standardData) {
        for (String key : doc.keySet()) {
            Object value = doc.get(key);
            
            if (key.startsWith("PERCENTAGE_")) {
                // Found a percentage field
                String lessonKey = key.replace("PERCENTAGE_", "");
                
                String standard = extractStandard(lessonKey);
                if (standard == null) return;
                
                String subject = determineSubject(lessonKey);
                if (subject == null) return;
                
                String lessonName = extractLessonName(lessonKey);
                
                int percentage = 0;
                if (value instanceof Integer) percentage = (Integer) value;
                else if (value instanceof Long) percentage = ((Long) value).intValue();
                else if (value instanceof Double) percentage = ((Double) value).intValue();
                
                Map<String, Object> lesson = new HashMap<>();
                lesson.put("name", lessonName);
                lesson.put("percentage", percentage);
                lesson.put("standard", standard);
                
                standardData.get(standard).get(subject).add(lesson);
                
                System.out.println("Found PERCENTAGE: " + standard + " - " + subject + " - " + lessonName + " = " + percentage + "%");
                
            } else if (value instanceof Document) {
                // Recursively search nested documents
                extractPercentageFields((Document) value, standardData);
            }
        }
    }

    private String extractLessonName(String identifier) {
        if (identifier == null) return "Unknown";
        
        // Remove NEET_11th_Physics_ prefix
        String cleaned = identifier.replaceAll("^NEET_\\d+(th|st|nd|rd)_(Physics|Chemistry|Botany|Zoology)_", "");
        
        // Replace __dot__ with space and clean up
        cleaned = cleaned.replace("__dot__", " ");
        
        // Remove leading numbers (e.g., "1 " or "1.1 ")
        cleaned = cleaned.replaceAll("^\\d+(\\.\\d+)?\\s*", "");
        
        // Replace underscores with spaces
        cleaned = cleaned.replace("_", " ");
        
        // Clean up multiple spaces
        cleaned = cleaned.trim().replaceAll("\\s+", " ");
        
        return cleaned.isEmpty() ? identifier : cleaned;
    }

    private String extractStandard(String identifier) {
        if (identifier == null) return null;
        if (identifier.contains("_11th_") || identifier.contains("11th")) return "11th";
        if (identifier.contains("_12th_") || identifier.contains("12th")) return "12th";
        return null;
    }

    private String determineSubject(String identifier) {
        if (identifier == null) return null;
        if (identifier.contains("_Physics_") || identifier.toLowerCase().contains("physics")) return "Physics";
        if (identifier.contains("_Chemistry_") || identifier.toLowerCase().contains("chemistry")) return "Chemistry";
        if (identifier.contains("_Botany_") || identifier.toLowerCase().contains("botany")) return "Botany";
        if (identifier.contains("_Zoology_") || identifier.toLowerCase().contains("zoology")) return "Zoology";
        return null;
    }

    private void extractCompletedLessons(Document doc, Map<String, Map<String, List<Map<String, Object>>>> standardData) {
        for (String key : doc.keySet()) {
            Object value = doc.get(key);
            
            if (value instanceof Document) {
                extractCompletedLessons((Document) value, standardData);
            } else if (value instanceof Boolean && (Boolean) value) {
                String standard = extractStandard(key);
                if (standard == null) continue;
                
                String subject = determineSubject(key);
                if (subject == null) continue;
                
                String lessonName = extractLessonName(key);
                
                boolean exists = false;
                List<Map<String, Object>> existingLessons = standardData.get(standard).get(subject);
                for (Map<String, Object> lesson : existingLessons) {
                    if (lesson.get("name").equals(lessonName)) {
                        int currentPct = (Integer) lesson.get("percentage");
                        if (currentPct < 100) {
                            lesson.put("percentage", 100);
                        }
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    Map<String, Object> lesson = new HashMap<>();
                    lesson.put("name", lessonName);
                    lesson.put("percentage", 100);
                    lesson.put("standard", standard);
                    standardData.get(standard).get(subject).add(lesson);
                    System.out.println("Added completed lesson: " + standard + " - " + subject + " - " + lessonName);
                }
            }
        }
    }
    
    
    
    @GetMapping("/getStudentSessions/{studentId}")
    public ResponseEntity<?> getStudentSessions(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== GETTING STUDY SESSIONS FOR STUDENT ID: " + studentId);
            
            MongoTemplate sessionTemplate = new MongoTemplate(mongoClient, "subjects");
            
            Set<String> collectionNames = sessionTemplate.getCollectionNames();
            System.out.println("Collections in subjects database: " + collectionNames);
            
            if (!collectionNames.contains("study_sessions")) {
                response.put("status", "error");
                response.put("message", "study_sessions collection not found");
                response.put("sessions", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> sessions = sessionTemplate.find(queryByUserId, Document.class, "study_sessions");
            System.out.println("Found " + sessions.size() + " sessions for student ID: " + studentId);
            
            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (Document session : sessions) {
                Map<String, Object> sessionData = new HashMap<>();
                
                if (session.get("_id") instanceof ObjectId) {
                    sessionData.put("_id", ((ObjectId) session.get("_id")).toString());
                }
                
                sessionData.put("userId", session.getString("userId"));
                sessionData.put("loginTime", session.getDate("loginTime"));
                sessionData.put("logoutTime", session.getDate("logoutTime"));
                
                Object durationObj = session.get("durationInMinutes");
                int durationInMinutes = 0;
                if (durationObj instanceof Integer) {
                    durationInMinutes = (Integer) durationObj;
                } else if (durationObj instanceof Long) {
                    durationInMinutes = ((Long) durationObj).intValue();
                } else if (durationObj != null) {
                    try {
                        durationInMinutes = Integer.parseInt(durationObj.toString());
                    } catch (NumberFormatException e) {
                        durationInMinutes = 0;
                    }
                }
                sessionData.put("durationInMinutes", durationInMinutes);
                sessionData.put("_class", session.getString("_class"));
                
                sessionList.add(sessionData);
            }
            
            sessionList.sort((a, b) -> {
                Date dateA = (Date) a.get("loginTime");
                Date dateB = (Date) b.get("loginTime");
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;
                if (dateB == null) return -1;
                return dateB.compareTo(dateA);
            });
            
            Map<String, Object> stats = new HashMap<>();
            int totalSessions = sessionList.size();
            int totalMinutes = 0;
            Date lastLogin = null;
            Date lastLogout = null;
            
            for (Map<String, Object> session : sessionList) {
                Integer duration = (Integer) session.get("durationInMinutes");
                if (duration != null) {
                    totalMinutes += duration;
                }
                
                Date loginTime = (Date) session.get("loginTime");
                if (loginTime != null && (lastLogin == null || loginTime.after(lastLogin))) {
                    lastLogin = loginTime;
                }
                
                Date logoutTime = (Date) session.get("logoutTime");
                if (logoutTime != null && (lastLogout == null || logoutTime.after(lastLogout))) {
                    lastLogout = logoutTime;
                }
            }
            
            stats.put("totalSessions", totalSessions);
            stats.put("totalMinutes", totalMinutes);
            stats.put("averageDuration", totalSessions > 0 ? totalMinutes / totalSessions : 0);
            stats.put("lastLogin", lastLogin);
            stats.put("lastLogout", lastLogout);
            
            response.put("status", "pass");
            response.put("sessions", sessionList);
            response.put("count", sessionList.size());
            response.put("statistics", stats);
            
            System.out.println("Returning " + sessionList.size() + " sessions");
            
        } catch (Exception e) {
            System.err.println("Error fetching study sessions: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("sessions", new ArrayList<>());
        }
        
        return ResponseEntity.ok(response);
        
    }
    
 // Get detailed assessment analysis for a specific student
    @GetMapping("/getStudentAssessmentAnalysis/{studentId}")
    public ResponseEntity<?> getStudentAssessmentAnalysis(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== GETTING ASSESSMENT ANALYSIS FOR STUDENT ID: " + studentId);
            
            MongoTemplate assessmentTemplate = new MongoTemplate(mongoClient, "subjects");
            
            // Check if collection exists
            Set<String> collectionNames = assessmentTemplate.getCollectionNames();
            System.out.println("Collections in subjects database: " + collectionNames);
            
            if (!collectionNames.contains("AssessmentResults")) {
                System.out.println("AssessmentResults collection not found");
                response.put("status", "pass");
                response.put("hasData", false);
                response.put("message", "No assessment data found");
                response.put("assessments", new ArrayList<>());
                response.put("strongTopics", new ArrayList<>());
                response.put("weakTopics", new ArrayList<>());
                response.put("subjectPerformance", new HashMap<>());
                response.put("overallStats", Map.of(
                    "totalQuestions", 0,
                    "totalCorrect", 0,
                    "totalWrong", 0,
                    "totalSkipped", 0,
                    "overallPercentage", 0,
                    "totalAssessments", 0
                ));
                return ResponseEntity.ok(response);
            }
            
            // Query for specific student by userId
            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> results = assessmentTemplate.find(queryByUserId, Document.class, "AssessmentResults");
            
            System.out.println("Found " + results.size() + " assessment results for student ID: " + studentId);
            
            // If no results, return empty data
            if (results.isEmpty()) {
                response.put("status", "pass");
                response.put("hasData", false);
                response.put("message", "No assessment data found for this student");
                response.put("assessments", new ArrayList<>());
                response.put("strongTopics", new ArrayList<>());
                response.put("weakTopics", new ArrayList<>());
                response.put("subjectPerformance", new HashMap<>());
                response.put("overallStats", Map.of(
                    "totalQuestions", 0,
                    "totalCorrect", 0,
                    "totalWrong", 0,
                    "totalSkipped", 0,
                    "overallPercentage", 0,
                    "totalAssessments", 0
                ));
                return ResponseEntity.ok(response);
            }
            
            // Process data for analysis
            List<Map<String, Object>> assessmentsList = new ArrayList<>();
            Map<String, Object> topicPerformance = new HashMap<>();
            Map<String, Object> subjectPerformance = new HashMap<>();
            
            int totalQuestions = 0;
            int totalCorrect = 0;
            int totalWrong = 0;
            int totalSkipped = 0;
            
            for (Document result : results) {
                String topicName = result.getString("topicName");
                String subject = result.getString("subject");
                String testType = result.getString("testType");
                
                // Get attempts
                List<Document> attempts = (List<Document>) result.get("attempts");
                Document latestAttempt = null;
                if (attempts != null && !attempts.isEmpty()) {
                    latestAttempt = attempts.get(attempts.size() - 1);
                }
                
                if (latestAttempt != null) {
                    int scoreScored = latestAttempt.getInteger("scoreScored", 0);
                    int totalMarks = latestAttempt.getInteger("totalMarks", 0);
                    int correct = latestAttempt.getInteger("correctAnswers", 0);
                    int wrong = latestAttempt.getInteger("wrongAnswers", 0);
                    int unattended = latestAttempt.getInteger("unattended", 0);
                    Object percentageObj = latestAttempt.get("percentage");
                    String percentage = percentageObj != null ? percentageObj.toString() : "0";
                    Date timestamp = latestAttempt.getDate("timestamp");
                    
                    totalQuestions += totalMarks;
                    totalCorrect += correct;
                    totalWrong += wrong;
                    totalSkipped += unattended;
                    
                    // Get unit breakdown
                    Document unitBreakdown = (Document) latestAttempt.get("unitBreakdown");
                    Map<String, Object> unitsData = new HashMap<>();
                    
                    if (unitBreakdown != null) {
                        for (String unitKey : unitBreakdown.keySet()) {
                            Document unitData = (Document) unitBreakdown.get(unitKey);
                            String unitName = unitData.getString("originalName");
                            if (unitName == null) unitName = unitKey.replace("__dot__", ".");
                            
                            int unitCorrect = unitData.getInteger("correct", 0);
                            int unitWrong = unitData.getInteger("wrong", 0);
                            int unitUnattended = unitData.getInteger("unattended", 0);
                            int unitTotal = unitData.getInteger("total", 0);
                            
                            Map<String, Object> unitInfo = new HashMap<>();
                            unitInfo.put("name", unitName);
                            unitInfo.put("correct", unitCorrect);
                            unitInfo.put("wrong", unitWrong);
                            unitInfo.put("unattended", unitUnattended);
                            unitInfo.put("total", unitTotal);
                            unitInfo.put("percentage", unitTotal > 0 ? (unitCorrect * 100 / unitTotal) : 0);
                            
                            // Get subtopics
                            Document subtopics = (Document) unitData.get("subtopics");
                            if (subtopics != null) {
                                Map<String, Object> subtopicsData = new HashMap<>();
                                for (String subKey : subtopics.keySet()) {
                                    Document subData = (Document) subtopics.get(subKey);
                                    String subName = subData.getString("originalName");
                                    if (subName == null) subName = subKey.replace("__dot__", ".");
                                    
                                    Map<String, Object> subInfo = new HashMap<>();
                                    subInfo.put("name", subName);
                                    subInfo.put("correct", subData.getInteger("correct", 0));
                                    subInfo.put("wrong", subData.getInteger("wrong", 0));
                                    subInfo.put("unattended", subData.getInteger("unattended", 0));
                                    subInfo.put("total", subData.getInteger("total", 0));
                                    subtopicsData.put(subName, subInfo);
                                }
                                unitInfo.put("subtopics", subtopicsData);
                            }
                            unitsData.put(unitName, unitInfo);
                        }
                    }
                    
                    Map<String, Object> assessment = new HashMap<>();
                    assessment.put("id", result.getObjectId("_id").toString());
                    assessment.put("topicName", topicName);
                    assessment.put("subject", subject);
                    assessment.put("testType", testType);
                    assessment.put("scoreScored", scoreScored);
                    assessment.put("totalMarks", totalMarks);
                    assessment.put("correctCount", correct);
                    assessment.put("wrongCount", wrong);
                    assessment.put("unattendedCount", unattended);
                    assessment.put("percentage", percentage);
                    assessment.put("status", (scoreScored >= totalMarks / 2) ? "Passed" : "Failed");
                    assessment.put("timestamp", timestamp);
                    assessment.put("unitBreakdown", unitsData);
                    assessment.put("attemptCount", attempts != null ? attempts.size() : 1);
                    
                    assessmentsList.add(assessment);
                    
                    // Track topic performance
                    double topicPercentage = totalMarks > 0 ? (scoreScored * 100.0 / totalMarks) : 0;
                    if (!topicPerformance.containsKey(topicName)) {
                        Map<String, Object> topicStats = new HashMap<>();
                        topicStats.put("subject", subject);
                        topicStats.put("totalAttempts", 1);
                        topicStats.put("totalScore", scoreScored);
                        topicStats.put("totalMarks", totalMarks);
                        topicStats.put("avgPercentage", topicPercentage);
                        topicStats.put("bestPercentage", topicPercentage);
                        topicStats.put("worstPercentage", topicPercentage);
                        topicStats.put("status", (scoreScored >= totalMarks / 2) ? "Passed" : "Failed");
                        topicPerformance.put(topicName, topicStats);
                    } else {
                        Map<String, Object> topicStats = (Map<String, Object>) topicPerformance.get(topicName);
                        topicStats.put("totalAttempts", (Integer) topicStats.get("totalAttempts") + 1);
                        topicStats.put("totalScore", (Integer) topicStats.get("totalScore") + scoreScored);
                        topicStats.put("totalMarks", (Integer) topicStats.get("totalMarks") + totalMarks);
                        double newAvg = ((Integer) topicStats.get("totalScore") * 100.0 / (Integer) topicStats.get("totalMarks"));
                        topicStats.put("avgPercentage", newAvg);
                        
                        double best = (Double) topicStats.get("bestPercentage");
                        if (topicPercentage > best) topicStats.put("bestPercentage", topicPercentage);
                        
                        double worst = (Double) topicStats.get("worstPercentage");
                        if (topicPercentage < worst) topicStats.put("worstPercentage", topicPercentage);
                    }
                    
                    // Track subject performance
                    if (!subjectPerformance.containsKey(subject)) {
                        Map<String, Object> subjStats = new HashMap<>();
                        subjStats.put("totalCorrect", correct);
                        subjStats.put("totalWrong", wrong);
                        subjStats.put("totalSkipped", unattended);
                        subjStats.put("totalQuestions", totalMarks);
                        subjStats.put("assessmentCount", 1);
                        subjectPerformance.put(subject, subjStats);
                    } else {
                        Map<String, Object> subjStats = (Map<String, Object>) subjectPerformance.get(subject);
                        subjStats.put("totalCorrect", (Integer) subjStats.get("totalCorrect") + correct);
                        subjStats.put("totalWrong", (Integer) subjStats.get("totalWrong") + wrong);
                        subjStats.put("totalSkipped", (Integer) subjStats.get("totalSkipped") + unattended);
                        subjStats.put("totalQuestions", (Integer) subjStats.get("totalQuestions") + totalMarks);
                        subjStats.put("assessmentCount", (Integer) subjStats.get("assessmentCount") + 1);
                    }
                }
            }
            
            // Calculate strong and weak topics
            List<Map<String, Object>> strongTopics = new ArrayList<>();
            List<Map<String, Object>> weakTopics = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : topicPerformance.entrySet()) {
                String topic = entry.getKey();
                Map<String, Object> stats = (Map<String, Object>) entry.getValue();
                double avgPct = (Double) stats.get("avgPercentage");
                
                Map<String, Object> topicInfo = new HashMap<>();
                topicInfo.put("name", topic);
                topicInfo.put("subject", stats.get("subject"));
                topicInfo.put("avgPercentage", avgPct);
                topicInfo.put("totalAttempts", stats.get("totalAttempts"));
                topicInfo.put("bestPercentage", stats.get("bestPercentage"));
                topicInfo.put("worstPercentage", stats.get("worstPercentage"));
                
                if (avgPct >= 70) {
                    strongTopics.add(topicInfo);
                } else if (avgPct <= 40) {
                    weakTopics.add(topicInfo);
                }
            }
            
            // Sort by percentage
            strongTopics.sort((a, b) -> Double.compare((Double) b.get("avgPercentage"), (Double) a.get("avgPercentage")));
            weakTopics.sort((a, b) -> Double.compare((Double) a.get("avgPercentage"), (Double) b.get("avgPercentage")));
            
            // Calculate overall stats
            double overallPercentage = totalQuestions > 0 ? (totalCorrect * 100.0 / totalQuestions) : 0;
            
            response.put("status", "pass");
            response.put("hasData", assessmentsList.size() > 0);
            response.put("assessments", assessmentsList);
            response.put("strongTopics", strongTopics);
            response.put("weakTopics", weakTopics);
            response.put("subjectPerformance", subjectPerformance);
            response.put("overallStats", Map.of(
                "totalQuestions", totalQuestions,
                "totalCorrect", totalCorrect,
                "totalWrong", totalWrong,
                "totalSkipped", totalSkipped,
                "overallPercentage", Math.round(overallPercentage),
                "totalAssessments", assessmentsList.size()
            ));
            
            System.out.println("Returning analysis for " + assessmentsList.size() + " assessments");
            
        } catch (Exception e) {
            System.err.println("Error fetching assessment analysis: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("hasData", false);
            response.put("assessments", new ArrayList<>());
            response.put("strongTopics", new ArrayList<>());
            response.put("weakTopics", new ArrayList<>());
            response.put("subjectPerformance", new HashMap<>());
            response.put("overallStats", Map.of(
                "totalQuestions", 0,
                "totalCorrect", 0,
                "totalWrong", 0,
                "totalSkipped", 0,
                "overallPercentage", 0,
                "totalAssessments", 0
            ));
        }
        
        return ResponseEntity.ok(response);
    }
    
 // Endpoint to get student analytical test results
    @GetMapping("/getStudentAnalyticalResults/{studentId}")
    public ResponseEntity<?> getStudentAnalyticalResults(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== GETTING ANALYTICAL RESULTS FOR STUDENT ID: " + studentId);
            
            MongoTemplate analyticalTemplate = new MongoTemplate(mongoClient, "subjects");
            
            // Check if collection exists
            Set<String> collectionNames = analyticalTemplate.getCollectionNames();
            System.out.println("Collections in subjects database: " + collectionNames);
            
            if (!collectionNames.contains("analytical_test_results")) {
                System.out.println("analytical_test_results collection not found");
                response.put("status", "pass");
                response.put("hasResults", false);
                response.put("message", "No analytical test results found");
                response.put("results", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Query for specific student by userId
            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> results = analyticalTemplate.find(queryByUserId, Document.class, "analytical_test_results");
            
            System.out.println("Found " + results.size() + " analytical results for student ID: " + studentId);
            
            // Convert to response format
            List<Map<String, Object>> resultsList = new ArrayList<>();
            for (Document result : results) {
                Map<String, Object> resultData = new HashMap<>();
                
                if (result.get("_id") instanceof ObjectId) {
                    resultData.put("_id", ((ObjectId) result.get("_id")).toString());
                }
                
                resultData.put("userId", result.getString("userId"));
                resultData.put("userName", result.getString("userName"));
                resultData.put("userEmail", result.getString("userEmail"));
                resultData.put("topicName", result.getString("topicName"));
                resultData.put("subject", result.getString("subject"));
                resultData.put("score", result.getInteger("score"));
                resultData.put("totalQuestions", result.getInteger("totalQuestions"));
                resultData.put("correctCount", result.getInteger("correctCount"));
                resultData.put("wrongCount", result.getInteger("wrongCount"));
                resultData.put("skippedCount", result.getInteger("skippedCount"));
                resultData.put("lessonBreakdown", result.get("lessonBreakdown"));
                resultData.put("timestamp", result.getLong("timestamp"));
                resultData.put("_class", result.getString("_class"));
                
                resultsList.add(resultData);
            }
            
            response.put("status", "pass");
            response.put("hasResults", resultsList.size() > 0);
            response.put("results", resultsList);
            response.put("count", resultsList.size());
            
            System.out.println("Returning " + resultsList.size() + " analytical results");
            
        } catch (Exception e) {
            System.err.println("Error fetching analytical results: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("hasResults", false);
            response.put("results", new ArrayList<>());
        }
        
        return ResponseEntity.ok(response);
    }
    
 // Endpoint to get student assessment results (from AssessmentResults collection)
    @GetMapping("/getStudentAssessmentResults/{studentId}")
    public ResponseEntity<?> getStudentAssessmentResults(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== GETTING ASSESSMENT RESULTS FOR STUDENT ID: " + studentId);
            
            MongoTemplate assessmentTemplate = new MongoTemplate(mongoClient, "subjects");
            
            // Check if collection exists
            Set<String> collectionNames = assessmentTemplate.getCollectionNames();
            System.out.println("Collections in subjects database: " + collectionNames);
            
            if (!collectionNames.contains("AssessmentResults")) {
                System.out.println("AssessmentResults collection not found");
                response.put("status", "pass");
                response.put("hasResults", false);
                response.put("message", "No assessment results found");
                response.put("results", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Query for specific student by userId
            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> results = assessmentTemplate.find(queryByUserId, Document.class, "AssessmentResults");
            
            System.out.println("Found " + results.size() + " assessment results for student ID: " + studentId);
            
            // Convert to response format
            List<Map<String, Object>> resultsList = new ArrayList<>();
            for (Document result : results) {
                Map<String, Object> resultData = new HashMap<>();
                
                if (result.get("_id") instanceof ObjectId) {
                    resultData.put("_id", ((ObjectId) result.get("_id")).toString());
                }
                
                resultData.put("userId", result.getString("userId"));
                resultData.put("testType", result.getString("testType"));
                resultData.put("subject", result.getString("subject"));
                resultData.put("topicName", result.getString("topicName"));
                resultData.put("scoreScored", result.getInteger("scoreScored"));
                resultData.put("totalMarks", result.getInteger("totalMarks"));
                resultData.put("percentage", result.get("percentage"));
                resultData.put("status", result.getString("status"));
                resultData.put("timestamp", result.getDate("timestamp"));
                resultData.put("attempts", result.get("attempts"));
                resultData.put("_class", result.getString("_class"));
                
                resultsList.add(resultData);
            }
            
            response.put("status", "pass");
            response.put("hasResults", resultsList.size() > 0);
            response.put("results", resultsList);
            response.put("count", resultsList.size());
            
            System.out.println("Returning " + resultsList.size() + " assessment results");
            
        } catch (Exception e) {
            System.err.println("Error fetching assessment results: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("hasResults", false);
            response.put("results", new ArrayList<>());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/getStudySessionsByEmail/{email}")
    public ResponseEntity<?> getStudySessionsByEmail(@PathVariable("email") String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== GETTING STUDY SESSIONS FOR EMAIL: " + email);
            
            MongoTemplate studentTemplate = new MongoTemplate(mongoClient, "studentUsers");
            Query studentQuery = new Query(Criteria.where("email").is(email));
            Document student = studentTemplate.findOne(studentQuery, Document.class, "studentUserDetail");
            
            String studentId = null;
            if (student != null && student.get("_id") instanceof ObjectId) {
                studentId = ((ObjectId) student.get("_id")).toString();
                System.out.println("Found student with ID: " + studentId);
            } else {
                System.out.println("Student not found with email: " + email);
                response.put("status", "pass");
                response.put("sessions", new ArrayList<>());
                response.put("count", 0);
                response.put("statistics", new HashMap<>());
                return ResponseEntity.ok(response);
            }
            
            MongoTemplate sessionTemplate = new MongoTemplate(mongoClient, "subjects");
            
            Set<String> collectionNames = sessionTemplate.getCollectionNames();
            
            if (!collectionNames.contains("study_sessions")) {
                response.put("status", "error");
                response.put("message", "study_sessions collection not found");
                response.put("sessions", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> sessions = sessionTemplate.find(queryByUserId, Document.class, "study_sessions");
            System.out.println("Found " + sessions.size() + " sessions for student: " + studentId);
            
            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (Document session : sessions) {
                Map<String, Object> sessionData = new HashMap<>();
                
                if (session.get("_id") instanceof ObjectId) {
                    sessionData.put("_id", ((ObjectId) session.get("_id")).toString());
                }
                
                sessionData.put("userId", session.getString("userId"));
                sessionData.put("loginTime", session.getDate("loginTime"));
                sessionData.put("logoutTime", session.getDate("logoutTime"));
                
                Object durationObj = session.get("durationInMinutes");
                int durationInMinutes = 0;
                if (durationObj instanceof Integer) {
                    durationInMinutes = (Integer) durationObj;
                } else if (durationObj instanceof Long) {
                    durationInMinutes = ((Long) durationObj).intValue();
                } else if (durationObj != null) {
                    try {
                        durationInMinutes = Integer.parseInt(durationObj.toString());
                    } catch (NumberFormatException e) {
                        durationInMinutes = 0;
                    }
                }
                sessionData.put("durationInMinutes", durationInMinutes);
                
                sessionList.add(sessionData);
            }
            
            sessionList.sort((a, b) -> {
                Date dateA = (Date) a.get("loginTime");
                Date dateB = (Date) b.get("loginTime");
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;
                if (dateB == null) return -1;
                return dateB.compareTo(dateA);
            });
            
            Map<String, Object> stats = new HashMap<>();
            int totalSessions = sessionList.size();
            int totalMinutes = 0;
            Date lastLogin = null;
            Date lastLogout = null;
            
            for (Map<String, Object> session : sessionList) {
                Integer duration = (Integer) session.get("durationInMinutes");
                if (duration != null) {
                    totalMinutes += duration;
                }
                
                Date loginTime = (Date) session.get("loginTime");
                if (loginTime != null && (lastLogin == null || loginTime.after(lastLogin))) {
                    lastLogin = loginTime;
                }
                
                Date logoutTime = (Date) session.get("logoutTime");
                if (logoutTime != null && (lastLogout == null || logoutTime.after(lastLogout))) {
                    lastLogout = logoutTime;
                }
            }
            
            stats.put("totalSessions", totalSessions);
            stats.put("totalMinutes", totalMinutes);
            stats.put("averageDuration", totalSessions > 0 ? totalMinutes / totalSessions : 0);
            stats.put("lastLogin", lastLogin);
            stats.put("lastLogout", lastLogout);
            
            response.put("status", "pass");
            response.put("sessions", sessionList);
            response.put("count", sessionList.size());
            response.put("statistics", stats);
            
        } catch (Exception e) {
            System.err.println("Error fetching study sessions: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("sessions", new ArrayList<>());
        }
        
        return ResponseEntity.ok(response);
    }
}
