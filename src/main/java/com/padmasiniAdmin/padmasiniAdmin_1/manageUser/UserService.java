package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder;

@Service
public class UserService {
    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    String dbName = "users";
    String collectionName = "users";

    private MongoTemplate template() {
        return new MongoTemplate(mongoClient, dbName);
    }

    private String hashIfNeeded(String pw) {
        if (pw == null || pw.isEmpty()) return pw;
        return pw.startsWith("$2") ? pw : passwordEncoder.encodePassword(pw);
    }

    /** Fill subjects / standards from the course when they were left empty. */
    private void applyDefaults(UserModel u) {
        String courseName = u.getCourseName();
        if (courseName == null || courseName.isEmpty()) return;
        String role = u.getRole();
        if (u.getSubjects() == null || u.getSubjects().isEmpty()) {
            u.setSubjects(getDefaultSubjects(courseName, role));
        }
        if (u.getStandards() == null || u.getStandards().isEmpty()) {
            u.setStandards(getDefaultStandards(courseName, role));
        }
    }

    /** CREATE */
    public boolean saveNewUser(UserDTO dto) {
        UserModel user = dto.getUser();
        if (user == null || user.getGmail() == null || user.getGmail().trim().isEmpty()) return false;
        if (checkGmail(user.getGmail())) {
            System.out.println("User with email " + user.getGmail() + " already exists");
            return false;
        }

        user.setPassword(hashIfNeeded(user.getPassword()));
        if (user.getRole() == null || user.getRole().isEmpty()) user.setRole("teacher");
        applyDefaults(user);

        System.out.println("Saving user: " + user);
        template().save(user, collectionName);
        return true;
    }

    /**
     * UPDATE in place (keeps the same _id; blank password = keep old password).
     * @return "ok" | "notfound" | "duplicate" | "invalid"
     */
    public String updateUser(String oldGmail, UserModel in) {
        if (in == null || in.getGmail() == null || in.getGmail().trim().isEmpty()) return "invalid";

        MongoTemplate mt = template();
        Query q = new Query(Criteria.where("gmail").is(oldGmail));
        UserModel existing = mt.findOne(q, UserModel.class, collectionName);
        if (existing == null) return "notfound";

        if (!oldGmail.equals(in.getGmail()) && checkGmail(in.getGmail())) return "duplicate";

        String newPassword = (in.getPassword() == null || in.getPassword().isEmpty())
                ? existing.getPassword()
                : hashIfNeeded(in.getPassword());
        in.setPassword(newPassword);
        applyDefaults(in);

        Update up = new Update()
                .set("userName", in.getUserName())
                .set("gmail", in.getGmail())
                .set("phoneNumber", in.getPhoneNumber())
                .set("password", in.getPassword())
                .set("role", in.getRole())
                .set("service", in.getService())
                .set("coursetype", in.getCoursetype())
                .set("courseName", in.getCourseName())
                .set("boardType", in.getBoardType())
                .set("standards", in.getStandards())
                .set("subjects", in.getSubjects());

        mt.updateFirst(q, up, UserModel.class, collectionName);
        return "ok";
    }

    private boolean checkGmail(String gmail) {
        Query query = new Query(Criteria.where("gmail").is(gmail));
        return template().exists(query, UserModel.class, collectionName);
    }

    public List<UserModel> getUsers() {
        return template().findAll(UserModel.class, collectionName);
    }

    public void deleteUser(String gmail) {
        Query query = new Query(Criteria.where("gmail").is(gmail));
        template().remove(query, UserModel.class, collectionName);
        System.out.println("Deleted " + gmail);
    }

    // ───────────── default subjects / standards ─────────────
    private List<String> getDefaultSubjects(String courseName, String role) {
        if (courseName == null) return new ArrayList<>();
        String c = courseName.toLowerCase();

        if ("teacher".equalsIgnoreCase(role)) {
            switch (c) {
                case "neet":    return Arrays.asList("Physics", "Chemistry", "Botany", "Zoology");
                case "jee":     return Arrays.asList("Physics", "Chemistry", "Maths");
                case "class11":
                case "class12": return Arrays.asList("Physics", "Chemistry", "Biology", "Mathematics");
                default:        return new ArrayList<>();
            }
        } else {
            switch (c) {
                case "neet":         return Arrays.asList("Physics", "Chemistry", "Zoology", "Botany");
                case "jee":          return Arrays.asList("Physics", "Chemistry", "Maths");
                case "class1-5":     return Arrays.asList("English", "Maths", "Science");
                case "class6-12":    return Arrays.asList("Physics", "Chemistry", "Maths", "Biology");
                case "kindergarten": return Arrays.asList("ABCs", "Numbers", "Shapes");
                default:             return new ArrayList<>();
            }
        }
    }

    private List<String> getDefaultStandards(String courseName, String role) {
        if (courseName == null) return new ArrayList<>();
        String c = courseName.toLowerCase();

        if ("teacher".equalsIgnoreCase(role)) {
            switch (c) {
                case "neet":
                case "jee":     return Arrays.asList("11", "12");
                case "class11": return Arrays.asList("11");
                case "class12": return Arrays.asList("12");
                default:        return new ArrayList<>();
            }
        } else {
            switch (c) {
                case "neet":
                case "jee":          return Arrays.asList("11", "12");
                case "class1-5":     return Arrays.asList("1", "2", "3", "4", "5");
                case "class6-12":    return Arrays.asList("6", "7", "8", "9", "10", "11", "12");
                case "kindergarten": return Arrays.asList("KG");
                default:             return new ArrayList<>();
            }
        }
    }

    public List<String> getDefaultSubjectsPublic(String courseName, String role) {
        return getDefaultSubjects(courseName, role);
    }

    public List<String> getDefaultStandardsPublic(String courseName, String role) {
        return getDefaultStandards(courseName, role);
    }

    // ───────────── one-time migration ─────────────
    public void migrateExistingUsers() {
        try {
            MongoTemplate mt = template();
            List<UserModel> all = mt.findAll(UserModel.class, collectionName);
            int updated = 0, pwMigrated = 0;

            for (UserModel user : all) {
                boolean changed = false;
                String courseName = user.getCourseName();
                String role = user.getRole() != null ? user.getRole() : "student";

                String pw = user.getPassword();
                if (pw != null && !pw.isEmpty() && !pw.startsWith("$2")) {
                    user.setPassword(passwordEncoder.encodePassword(pw));
                    changed = true;
                    pwMigrated++;
                }
                if (courseName != null && (user.getSubjects() == null || user.getSubjects().isEmpty())) {
                    user.setSubjects(getDefaultSubjects(courseName, role));
                    changed = true;
                }
                if (courseName != null && (user.getStandards() == null || user.getStandards().isEmpty())) {
                    user.setStandards(getDefaultStandards(courseName, role));
                    changed = true;
                }
                if (user.getRole() == null || user.getRole().isEmpty()) {
                    user.setRole("student");
                    changed = true;
                }
                if (changed) {
                    mt.save(user, collectionName);
                    updated++;
                }
            }
            System.out.println("Migration done. Updated " + updated + " users, hashed " + pwMigrated + " passwords.");
        } catch (Exception e) {
            System.err.println("Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}