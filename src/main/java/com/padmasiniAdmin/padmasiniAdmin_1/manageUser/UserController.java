package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
@SuppressWarnings("unchecked")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MongoClient mongoClient;

    // =====================================================================
    //  STAFF (teacher / admin) CRUD
    //  NOTE: no shared "map" field any more - every request builds its own
    //  response map, so status/messages can never leak between requests.
    // =====================================================================

    @PostMapping("/newUser")
    public ResponseEntity<?> addNewUser(@RequestBody UserDTO user) {
        Map<String, String> res = new HashMap<>();

        if (user == null || user.getUser() == null) {
            res.put("status", "failed");
            res.put("message", "Invalid request");
            return ResponseEntity.ok(res);
        }

        if (userService.saveNewUser(user)) {
            res.put("status", "pass");
            res.put("message", "User created successfully");
        } else {
            res.put("status", "failed");
            res.put("message", "User with this email already exists");
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/getDefaultData/{courseName}")
    public ResponseEntity<?> getDefaultData(@PathVariable String courseName,
                                            @RequestParam(required = false) String role) {
        String r = role != null ? role : "student";
        Map<String, Object> response = new HashMap<>();
        response.put("courseName", courseName);
        response.put("role", r);
        response.put("defaultSubjects", userService.getDefaultSubjectsPublic(courseName, r));
        response.put("defaultStandards", userService.getDefaultStandardsPublic(courseName, r));
        return ResponseEntity.ok(response);
    }

    // Never send password hashes to the browser
    @GetMapping("/getUsers")
    public List<UserModel> getUsers() {
        List<UserModel> users = userService.getUsers();
        users.forEach(u -> u.setPassword(null));
        System.out.println("Retrieved " + users.size() + " users from database");
        return users;
    }

    // {gmail:.+} stops Spring cutting ".com" off the email in the URL
    @PutMapping("/updateUser/{gmail:.+}")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO user, @PathVariable("gmail") String gmail) {
        Map<String, String> res = new HashMap<>();

        if (user == null || user.getUser() == null) {
            res.put("status", "failed");
            res.put("message", "Invalid request");
            return ResponseEntity.ok(res);
        }

        String result = userService.updateUser(gmail, user.getUser());
        switch (result) {
            case "ok":
                res.put("status", "pass");
                res.put("message", "User updated successfully");
                break;
            case "duplicate":
                res.put("status", "failed");
                res.put("message", "Email already exists");
                break;
            case "notfound":
                res.put("status", "failed");
                res.put("message", "User not found");
                break;
            default:
                res.put("status", "failed");
                res.put("message", "Invalid data");
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/deleteUser/{gmail:.+}")
    public ResponseEntity<?> deleteUser(@PathVariable("gmail") String gmail) {
        Map<String, String> res = new HashMap<>();
        userService.deleteUser(gmail);
        res.put("status", "pass");
        res.put("message", "User deleted successfully");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/getUsersByRole/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "users");
            Query query = new Query(Criteria.where("role").is(role));
            List<UserModel> users = mongoTemplate.find(query, UserModel.class, "users");
            users.forEach(u -> u.setPassword(null));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // =====================================================================
    //  STUDENTS
    // =====================================================================

    @GetMapping("/getAllStudents")
    public ResponseEntity<?> getAllStudents() {
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");

            if (!mongoTemplate.collectionExists("studentUserDetail")) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<Document> documents = mongoTemplate.find(new Query(), Document.class, "studentUserDetail");
            List<Map<String, Object>> studentList = new ArrayList<>();

            for (Document doc : documents) {
                Map<String, Object> studentData = new HashMap<>();

                if (doc.get("_id") instanceof ObjectId) {
                    String idStr = ((ObjectId) doc.get("_id")).toString();
                    studentData.put("_id", idStr);
                    studentData.put("id", idStr);
                }

                String firstname = doc.getString("firstname") != null ? doc.getString("firstname") : "";
                String lastname = doc.getString("lastname") != null ? doc.getString("lastname") : "";
                studentData.put("firstname", doc.getString("firstname"));
                studentData.put("lastname", doc.getString("lastname"));
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

                if (doc.containsKey("severity")) studentData.put("severity", doc.getString("severity"));
                if (doc.containsKey("_class")) studentData.put("_class", doc.getString("_class"));

                studentData.put("paymentHistory",
                        doc.containsKey("paymentHistory") ? doc.get("paymentHistory") : new ArrayList<>());

                Map<String, Object> access = new HashMap<>();
                String coursetype = doc.getString("coursetype");
                access.put("mode", coursetype != null
                        && (coursetype.equalsIgnoreCase("NEET") || coursetype.equalsIgnoreCase("JEE"))
                        ? "professional" : "academics");
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
                        long day = 1000L * 60 * 60 * 24;

                        if (now.before(startDate)) {
                            studentData.put("status", "upcoming");
                            studentData.put("daysRemaining", (startDate.getTime() - now.getTime()) / day);
                        } else if (now.after(endDate)) {
                            studentData.put("status", "expired");
                            studentData.put("daysRemaining", 0);
                        } else {
                            long daysRemaining = (endDate.getTime() - now.getTime()) / day;
                            studentData.put("daysRemaining", daysRemaining);
                            studentData.put("status", daysRemaining <= 7 ? "expiring" : "active");
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
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    @PostMapping("/addStudent")
    public ResponseEntity<?> addStudent(@RequestBody Map<String, Object> studentData) {
        Map<String, Object> response = new HashMap<>();

        try {
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

            hashStudentPassword(studentData);

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

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error adding student: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateStudent/{email:.+}")
    public ResponseEntity<?> updateStudent(@PathVariable("email") String email,
                                           @RequestBody Map<String, Object> studentData) {
        Map<String, Object> response = new HashMap<>();

        try {
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

            hashStudentPassword(studentData);

            studentData.remove("_id");
            studentData.remove("id");
            studentData.put("updatedAt", new Date().toString());

            org.springframework.data.mongodb.core.query.Update update =
                    new org.springframework.data.mongodb.core.query.Update();
            for (Map.Entry<String, Object> entry : studentData.entrySet()) {
                update.set(entry.getKey(), entry.getValue());
            }

            mongoTemplate.updateFirst(query, update, "studentUserDetail");

            response.put("status", "pass");
            response.put("message", "Student updated successfully");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error updating student: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getStudent/{email:.+}")
    public ResponseEntity<?> getStudentByEmail(@PathVariable("email") String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            Query query = new Query(Criteria.where("email").is(email));
            Document student = mongoTemplate.findOne(query, Document.class, "studentUserDetail");

            if (student != null) {
                if (student.get("_id") instanceof ObjectId) {
                    String idStr = ((ObjectId) student.get("_id")).toString();
                    student.put("_id", idStr);
                    student.put("id", idStr);
                }
                response.put("status", "pass");
                response.put("data", student);
            } else {
                response.put("status", "failed");
                response.put("message", "Student not found");
            }

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteStudent/{id:.+}")
    public ResponseEntity<?> deleteStudent(@PathVariable("id") String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "studentUsers");
            boolean deleted = false;

            try {
                Query queryById = new Query(Criteria.where("_id").is(new ObjectId(id)));
                DeleteResult result = mongoTemplate.remove(queryById, "studentUserDetail");
                deleted = result.getDeletedCount() > 0;
            } catch (Exception e) {
                // not a valid ObjectId -> try email below
            }

            if (!deleted) {
                Query queryByEmail = new Query(Criteria.where("email").is(id));
                DeleteResult result = mongoTemplate.remove(queryByEmail, "studentUserDetail");
                deleted = result.getDeletedCount() > 0;
            }

            if (deleted) {
                response.put("status", "pass");
                response.put("message", "Student deleted successfully");
            } else {
                response.put("status", "failed");
                response.put("message", "Student not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error deleting student: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // =====================================================================
    //  SESSIONS
    // =====================================================================

    @GetMapping("/simpleSessions/{studentId}")
    public ResponseEntity<?> simpleSessions(@PathVariable String studentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            MongoTemplate sessionTemplate = new MongoTemplate(mongoClient, "subjects");
            Query query = new Query(Criteria.where("userId").is(studentId));
            List<Document> sessions = sessionTemplate.find(query, Document.class, "study_sessions");

            List<Map<String, Object>> sessionList = new ArrayList<>();
            int totalMinutes = 0;
            for (Document session : sessions) {
                Map<String, Object> sessionData = new HashMap<>();
                if (session.get("_id") instanceof ObjectId) {
                    sessionData.put("_id", ((ObjectId) session.get("_id")).toString());
                }
                int duration = toInt(session.get("durationInMinutes"));
                sessionData.put("userId", session.getString("userId"));
                sessionData.put("loginTime", session.getDate("loginTime"));
                sessionData.put("logoutTime", session.getDate("logoutTime"));
                sessionData.put("durationInMinutes", duration);
                totalMinutes += duration;
                sessionList.add(sessionData);
            }

            response.put("status", "pass");
            response.put("sessions", sessionList);
            response.put("count", sessionList.size());
            response.put("totalMinutes", totalMinutes);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getStudentSessions/{studentId}")
    public ResponseEntity<?> getStudentSessions(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();

        try {
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

            List<Map<String, Object>> sessionList = buildSessionList(sessions, true);

            response.put("status", "pass");
            response.put("sessions", sessionList);
            response.put("count", sessionList.size());
            response.put("statistics", buildSessionStats(sessionList));

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("sessions", new ArrayList<>());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getStudySessionsByEmail/{email:.+}")
    public ResponseEntity<?> getStudySessionsByEmail(@PathVariable("email") String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            MongoTemplate studentTemplate = new MongoTemplate(mongoClient, "studentUsers");
            Query studentQuery = new Query(Criteria.where("email").is(email));
            Document student = studentTemplate.findOne(studentQuery, Document.class, "studentUserDetail");

            if (student == null || !(student.get("_id") instanceof ObjectId)) {
                response.put("status", "pass");
                response.put("sessions", new ArrayList<>());
                response.put("count", 0);
                response.put("statistics", new HashMap<>());
                return ResponseEntity.ok(response);
            }
            String studentId = ((ObjectId) student.get("_id")).toString();

            MongoTemplate sessionTemplate = new MongoTemplate(mongoClient, "subjects");
            if (!sessionTemplate.getCollectionNames().contains("study_sessions")) {
                response.put("status", "error");
                response.put("message", "study_sessions collection not found");
                response.put("sessions", new ArrayList<>());
                return ResponseEntity.ok(response);
            }

            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> sessions = sessionTemplate.find(queryByUserId, Document.class, "study_sessions");

            List<Map<String, Object>> sessionList = buildSessionList(sessions, false);

            response.put("status", "pass");
            response.put("sessions", sessionList);
            response.put("count", sessionList.size());
            response.put("statistics", buildSessionStats(sessionList));

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("sessions", new ArrayList<>());
        }

        return ResponseEntity.ok(response);
    }

    // =====================================================================
    //  PROGRESS
    // =====================================================================

    @GetMapping("/getStudentProgressDynamic/{studentId}")
    public ResponseEntity<?> getStudentProgressDynamic(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            MongoTemplate progressTemplate = new MongoTemplate(mongoClient, "subjects");
            Query query = new Query(Criteria.where("userId").is(studentId));
            Document progress = progressTemplate.findOne(query, Document.class, "Progress");

            if (progress == null) {
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

            extractPercentageFields(progress, standardData);

            Document completedSubtopics = (Document) progress.get("completedSubtopics");
            if (completedSubtopics != null) {
                extractCompletedLessons(completedSubtopics, standardData);
            }

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

                    // keep the highest percentage per lesson name
                    Map<String, Map<String, Object>> uniqueLessons = new LinkedHashMap<>();
                    for (Map<String, Object> lesson : lessons) {
                        String name = (String) lesson.get("name");
                        if (!uniqueLessons.containsKey(name)
                                || (Integer) uniqueLessons.get(name).get("percentage") < (Integer) lesson.get("percentage")) {
                            uniqueLessons.put(name, lesson);
                        }
                    }

                    List<Map<String, Object>> cleanedLessons = new ArrayList<>(uniqueLessons.values());
                    cleanedLessons.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));

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

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    private void extractPercentageFields(Document doc, Map<String, Map<String, List<Map<String, Object>>>> standardData) {
        for (String key : doc.keySet()) {
            Object value = doc.get(key);

            if (key.startsWith("PERCENTAGE_")) {
                String lessonKey = key.replace("PERCENTAGE_", "");

                String standard = extractStandard(lessonKey);
                if (standard == null) continue;

                String subject = determineSubject(lessonKey);
                if (subject == null) continue;

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

            } else if (value instanceof Document) {
                extractPercentageFields((Document) value, standardData);
            }
        }
    }

    private String extractLessonName(String identifier) {
        if (identifier == null) return "Unknown";

        String cleaned = identifier.replaceAll("^NEET_\\d+(th|st|nd|rd)_(Physics|Chemistry|Botany|Zoology)_", "");
        cleaned = cleaned.replace("__dot__", " ");
        cleaned = cleaned.replaceAll("^\\d+(\\.\\d+)?\\s*", "");
        cleaned = cleaned.replace("_", " ");
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
                        if ((Integer) lesson.get("percentage") < 100) {
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
                }
            }
        }
    }

    // =====================================================================
    //  ASSESSMENTS
    // =====================================================================

    @GetMapping("/getStudentAssessmentAnalysis/{studentId}")
    public ResponseEntity<?> getStudentAssessmentAnalysis(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            MongoTemplate assessmentTemplate = new MongoTemplate(mongoClient, "subjects");
            Set<String> collectionNames = assessmentTemplate.getCollectionNames();

            if (!collectionNames.contains("AssessmentResults")) {
                return ResponseEntity.ok(emptyAssessmentResponse("No assessment data found"));
            }

            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> results = assessmentTemplate.find(queryByUserId, Document.class, "AssessmentResults");

            if (results.isEmpty()) {
                return ResponseEntity.ok(emptyAssessmentResponse("No assessment data found for this student"));
            }

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

                if (testType == null) {
                    String className = result.getString("_class");
                    if (className != null) {
                        if (className.contains("Mock")) testType = "Mock";
                        else if (className.contains("PYQ")) testType = "PYQ";
                        else if (className.contains("Formula")) testType = "Formula";
                        else testType = "Assessment";
                    } else {
                        testType = "Assessment";
                    }
                }

                String displayTestType = testType;
                if ("Mock".equals(testType)) {
                    if ("Full Cumulative Mock Test".equals(subject)
                            || "Full Cumulative Mock Test Mock Test".equals(topicName)) {
                        displayTestType = "FullMock";
                    } else {
                        displayTestType = "SubjectMock";
                    }
                }

                List<Document> attempts = (List<Document>) result.get("attempts");
                Document latestAttempt = null;
                if (attempts != null && !attempts.isEmpty()) {
                    latestAttempt = attempts.get(attempts.size() - 1);
                }
                if (latestAttempt == null) continue;

                int scoreScored = latestAttempt.getInteger("scoreScored", 0);
                int totalMarks = latestAttempt.getInteger("totalMarks", 0);
                int correct = latestAttempt.getInteger("correctAnswers", 0);
                int wrong = latestAttempt.getInteger("wrongAnswers", 0);
                int unattended = latestAttempt.getInteger("unattended", 0);
                Object percentageObj = latestAttempt.get("percentage");
                String percentage = percentageObj != null ? percentageObj.toString() : "0";
                Date timestamp = latestAttempt.getDate("timestamp");

                // use the status stored in the DB; compute only as a fallback
                String dbStatus = latestAttempt.getString("status");
                if (dbStatus == null) {
                    dbStatus = (scoreScored >= totalMarks / 2) ? "pass" : "fail";
                }

                totalQuestions += totalMarks;
                totalCorrect += correct;
                totalWrong += wrong;
                totalSkipped += unattended;

                Map<String, Object> unitsData = parseUnits((Document) latestAttempt.get("unitBreakdown"), true);

                List<Map<String, Object>> allAttempts = new ArrayList<>();
                for (Document attempt : attempts) {
                    Map<String, Object> attemptData = new HashMap<>();
                    attemptData.put("scoreScored", attempt.getInteger("scoreScored", 0));
                    attemptData.put("totalMarks", attempt.getInteger("totalMarks", 0));
                    attemptData.put("correctCount", attempt.getInteger("correctAnswers", 0));
                    attemptData.put("wrongCount", attempt.getInteger("wrongAnswers", 0));
                    attemptData.put("unattendedCount", attempt.getInteger("unattended", 0));
                    attemptData.put("timestamp", attempt.getDate("timestamp"));
                    attemptData.put("status", attempt.getString("status"));

                    Document attemptUnitBreakdown = (Document) attempt.get("unitBreakdown");
                    if (attemptUnitBreakdown != null) {
                        attemptData.put("unitBreakdown", parseUnits(attemptUnitBreakdown, false));
                    }
                    allAttempts.add(attemptData);
                }

                Map<String, Object> assessment = new HashMap<>();
                assessment.put("id", result.getObjectId("_id").toString());
                assessment.put("topicName", topicName);
                assessment.put("subject", subject);
                assessment.put("testType", displayTestType);
                assessment.put("originalTestType", testType);
                assessment.put("scoreScored", scoreScored);
                assessment.put("totalMarks", totalMarks);
                assessment.put("correctCount", correct);
                assessment.put("wrongCount", wrong);
                assessment.put("unattendedCount", unattended);
                assessment.put("percentage", percentage);
                assessment.put("status", dbStatus);
                assessment.put("timestamp", timestamp);
                assessment.put("unitBreakdown", unitsData);
                assessment.put("attempts", allAttempts);
                assessment.put("attemptCount", attempts.size());
                assessmentsList.add(assessment);

                // topic performance
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
                    topicStats.put("status", dbStatus);
                    topicPerformance.put(topicName, topicStats);
                } else {
                    Map<String, Object> topicStats = (Map<String, Object>) topicPerformance.get(topicName);
                    topicStats.put("totalAttempts", (Integer) topicStats.get("totalAttempts") + 1);
                    topicStats.put("totalScore", (Integer) topicStats.get("totalScore") + scoreScored);
                    topicStats.put("totalMarks", (Integer) topicStats.get("totalMarks") + totalMarks);
                    int sumMarks = (Integer) topicStats.get("totalMarks");
                    double newAvg = sumMarks > 0 ? ((Integer) topicStats.get("totalScore") * 100.0 / sumMarks) : 0;
                    topicStats.put("avgPercentage", newAvg);

                    if (topicPercentage > (Double) topicStats.get("bestPercentage"))
                        topicStats.put("bestPercentage", topicPercentage);
                    if (topicPercentage < (Double) topicStats.get("worstPercentage"))
                        topicStats.put("worstPercentage", topicPercentage);
                }

                // subject performance
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

            // strong / weak topics
            List<Map<String, Object>> strongTopics = new ArrayList<>();
            List<Map<String, Object>> weakTopics = new ArrayList<>();

            for (Map.Entry<String, Object> entry : topicPerformance.entrySet()) {
                Map<String, Object> stats = (Map<String, Object>) entry.getValue();
                double avgPct = (Double) stats.get("avgPercentage");

                Map<String, Object> topicInfo = new HashMap<>();
                topicInfo.put("name", entry.getKey());
                topicInfo.put("subject", stats.get("subject"));
                topicInfo.put("avgPercentage", avgPct);
                topicInfo.put("totalAttempts", stats.get("totalAttempts"));
                topicInfo.put("bestPercentage", stats.get("bestPercentage"));
                topicInfo.put("worstPercentage", stats.get("worstPercentage"));

                if (avgPct >= 70) strongTopics.add(topicInfo);
                else if (avgPct <= 40) weakTopics.add(topicInfo);
            }

            strongTopics.sort((a, b) -> Double.compare((Double) b.get("avgPercentage"), (Double) a.get("avgPercentage")));
            weakTopics.sort((a, b) -> Double.compare((Double) a.get("avgPercentage"), (Double) b.get("avgPercentage")));

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

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = emptyAssessmentResponse(null);
            err.put("status", "error");
            err.put("message", e.getMessage());
            err.put("hasData", false);
            return ResponseEntity.ok(err);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getStudentAnalyticalResults/{studentId}")
    public ResponseEntity<?> getStudentAnalyticalResults(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            MongoTemplate analyticalTemplate = new MongoTemplate(mongoClient, "subjects");

            if (!analyticalTemplate.getCollectionNames().contains("analytical_test_results")) {
                response.put("status", "pass");
                response.put("hasResults", false);
                response.put("message", "No analytical test results found");
                response.put("results", new ArrayList<>());
                return ResponseEntity.ok(response);
            }

            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> results = analyticalTemplate.find(queryByUserId, Document.class, "analytical_test_results");

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

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("hasResults", false);
            response.put("results", new ArrayList<>());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getStudentAssessmentResults/{studentId}")
    public ResponseEntity<?> getStudentAssessmentResults(@PathVariable("studentId") String studentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            MongoTemplate assessmentTemplate = new MongoTemplate(mongoClient, "subjects");

            if (!assessmentTemplate.getCollectionNames().contains("AssessmentResults")) {
                response.put("status", "pass");
                response.put("hasResults", false);
                response.put("message", "No assessment results found");
                response.put("results", new ArrayList<>());
                return ResponseEntity.ok(response);
            }

            Query queryByUserId = new Query(Criteria.where("userId").is(studentId));
            List<Document> results = assessmentTemplate.find(queryByUserId, Document.class, "AssessmentResults");

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

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("hasResults", false);
            response.put("results", new ArrayList<>());
        }

        return ResponseEntity.ok(response);
    }

    // =====================================================================
    //  DEBUG ENDPOINT
    //  The original version echoed plain passwords back. It now only
    //  reports whether hashing works. Remove it before production.
    // =====================================================================
    @PostMapping("/testEncryption")
    public ResponseEntity<?> testEncryption(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String plainPassword = request.get("password");
            com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder encoder =
                    new com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder();

            String hashed = encoder.encodePassword(plainPassword);
            response.put("status", "success");
            response.put("matches", encoder.matches(plainPassword, hashed));
            response.put("startsWithDollar", hashed.startsWith("$2"));

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    private ResponseEntity<?> errorResponse(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", e.getMessage());
        return ResponseEntity.status(500).body(error);
    }

    private void hashStudentPassword(Map<String, Object> studentData) {
        String plainPassword = (String) studentData.get("password");
        if (plainPassword != null && !plainPassword.isEmpty() && !plainPassword.startsWith("$2")) {
            com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder encoder =
                    new com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder();
            studentData.put("password", encoder.encodePassword(plainPassword));
        }
    }

    private int toInt(Object o) {
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Double) return ((Double) o).intValue();
        if (o != null) {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private List<Map<String, Object>> buildSessionList(List<Document> sessions, boolean includeClass) {
        List<Map<String, Object>> sessionList = new ArrayList<>();
        for (Document session : sessions) {
            Map<String, Object> sessionData = new HashMap<>();

            if (session.get("_id") instanceof ObjectId) {
                sessionData.put("_id", ((ObjectId) session.get("_id")).toString());
            }
            sessionData.put("userId", session.getString("userId"));
            sessionData.put("loginTime", session.getDate("loginTime"));
            sessionData.put("logoutTime", session.getDate("logoutTime"));
            sessionData.put("durationInMinutes", toInt(session.get("durationInMinutes")));
            if (includeClass) sessionData.put("_class", session.getString("_class"));

            sessionList.add(sessionData);
        }

        // newest login first, null login times last
        sessionList.sort((a, b) -> {
            Date dateA = (Date) a.get("loginTime");
            Date dateB = (Date) b.get("loginTime");
            if (dateA == null && dateB == null) return 0;
            if (dateA == null) return 1;
            if (dateB == null) return -1;
            return dateB.compareTo(dateA);
        });
        return sessionList;
    }

    private Map<String, Object> buildSessionStats(List<Map<String, Object>> sessionList) {
        int totalSessions = sessionList.size();
        int totalMinutes = 0;
        Date lastLogin = null;
        Date lastLogout = null;

        for (Map<String, Object> session : sessionList) {
            Integer duration = (Integer) session.get("durationInMinutes");
            if (duration != null) totalMinutes += duration;

            Date loginTime = (Date) session.get("loginTime");
            if (loginTime != null && (lastLogin == null || loginTime.after(lastLogin))) lastLogin = loginTime;

            Date logoutTime = (Date) session.get("logoutTime");
            if (logoutTime != null && (lastLogout == null || logoutTime.after(lastLogout))) lastLogout = logoutTime;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", totalSessions);
        stats.put("totalMinutes", totalMinutes);
        stats.put("averageDuration", totalSessions > 0 ? totalMinutes / totalSessions : 0);
        stats.put("lastLogin", lastLogin);
        stats.put("lastLogout", lastLogout);
        return stats;
    }

    /** Parses unitBreakdown (units + subtopics). withPercentage adds a per-unit percentage. */
    private Map<String, Object> parseUnits(Document unitBreakdown, boolean withPercentage) {
        Map<String, Object> unitsData = new HashMap<>();
        if (unitBreakdown == null) return unitsData;

        for (String unitKey : unitBreakdown.keySet()) {
            Document unitData = (Document) unitBreakdown.get(unitKey);
            String unitName = unitData.getString("originalName");
            if (unitName == null) unitName = unitKey.replace("__dot__", ".");

            int unitCorrect = unitData.getInteger("correct", 0);
            int unitTotal = unitData.getInteger("total", 0);

            Map<String, Object> unitInfo = new HashMap<>();
            unitInfo.put("name", unitName);
            unitInfo.put("correct", unitCorrect);
            unitInfo.put("wrong", unitData.getInteger("wrong", 0));
            unitInfo.put("unattended", unitData.getInteger("unattended", 0));
            unitInfo.put("total", unitTotal);
            if (withPercentage) {
                unitInfo.put("percentage", unitTotal > 0 ? (unitCorrect * 100 / unitTotal) : 0);
            }

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
        return unitsData;
    }

    private Map<String, Object> emptyAssessmentResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "pass");
        response.put("hasData", false);
        response.put("message", message);
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
        return response;
    }
}