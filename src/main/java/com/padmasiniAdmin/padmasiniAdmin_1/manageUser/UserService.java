package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordCrypto;
import com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder;

@Service
public class UserService {
    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordCrypto passwordCrypto;

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

    // gmail + role together identify an account, so a teacher and an admin may share the same email
    private Query byGmailAndRole(String gmail, String role) {
        return new Query(Criteria.where("gmail").is(gmail).and("role").is(role));
    }

    /** CREATE */
    public boolean saveNewUser(UserDTO dto) {
        UserModel user = dto.getUser();
        if (user == null || user.getGmail() == null || user.getGmail().trim().isEmpty()) return false;
        if (user.getRole() == null || user.getRole().isEmpty()) user.setRole("teacher");

        if (checkGmail(user.getGmail(), user.getRole())) {
            System.out.println("A " + user.getRole() + " with email " + user.getGmail() + " already exists");
            return false;
        }

        String raw = user.getPassword();
        if (raw != null && !raw.isEmpty() && !raw.startsWith("$2")) {
            user.setPasswordEnc(passwordCrypto.encrypt(raw));
        }
        user.setPassword(hashIfNeeded(raw));
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
        if (in.getRole() == null || in.getRole().isEmpty()) in.setRole("teacher");

        MongoTemplate mt = template();
        Query q = byGmailAndRole(oldGmail, in.getRole());
        UserModel existing = mt.findOne(q, UserModel.class, collectionName);
        if (existing == null) return "notfound";

        if (!oldGmail.equals(in.getGmail()) && checkGmail(in.getGmail(), in.getRole())) {
            System.out.println("Duplicate: a " + in.getRole() + " with email " + in.getGmail() + " already exists");
            return "duplicate";
        }

        String newPassword;
        String newEnc;
        String raw = in.getPassword();
        if (raw == null || raw.isEmpty()) {
            newPassword = existing.getPassword();
            newEnc = existing.getPasswordEnc();
        } else {
            newPassword = hashIfNeeded(raw);
            newEnc = raw.startsWith("$2") ? existing.getPasswordEnc() : passwordCrypto.encrypt(raw);
        }
        in.setPassword(newPassword);
        applyDefaults(in);

        Update up = new Update()
                .set("userName", in.getUserName())
                .set("gmail", in.getGmail())
                .set("phoneNumber", in.getPhoneNumber())
                .set("password", newPassword)
                .set("passwordEnc", newEnc)
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

    private boolean checkGmail(String gmail, String role) {
        return template().exists(byGmailAndRole(gmail, role), UserModel.class, collectionName);
    }

    public List<UserModel> getUsers() {
        return template().findAll(UserModel.class, collectionName);
    }

    public void deleteUser(String gmail, String role) {
        Query query = (role == null || role.isEmpty())
                ? new Query(Criteria.where("gmail").is(gmail))
                : byGmailAndRole(gmail, role);
        template().remove(query, UserModel.class, collectionName);
        System.out.println("Deleted " + gmail + " (" + role + ")");
    }

    /** Admin-only: the viewable password (null if this account has no stored copy yet). */
    public String getPlainPassword(String gmail, String role) {
        UserModel u = template().findOne(byGmailAndRole(gmail, role), UserModel.class, collectionName);
        return u == null ? null : passwordCrypto.decrypt(u.getPasswordEnc());
    }

    // ───────────── LOGIN HELPER ─────────────
    /**
     * Finds EVERY account (teacher + admin) whose username OR email equals `identifier`
     * AND whose password matches this password. Only the account(s) whose password matches
     * are used, so the same username with different passwords logs into the right account.
     *  - admin only    -> role "admin"
     *  - teacher only  -> role "teacher"
     *  - both match    -> merged (role "admin", teacher course data kept, roles = both)
     * Passwords are NOT removed here: call sanitized() before sending to the browser.
     * Returns null when nothing matches.
     */
    public UserModel findAndMergeAccounts(String identifier, String rawPassword) {
        if (identifier == null || rawPassword == null) return null;

        Query q = new Query(new Criteria().orOperator(
                Criteria.where("gmail").is(identifier),
                Criteria.where("userName").is(identifier)));
        List<UserModel> candidates = template().find(q, UserModel.class, collectionName);

        UserModel admin = null;
        UserModel teacher = null;
        UserModel other = null;

        for (UserModel u : candidates) {
            if (!passwordMatches(rawPassword, u.getPassword())) continue;

            // old account without a viewable copy -> store it now, so the admin can see it later
            backfillPasswordEnc(u, rawPassword);

            String r = u.getRole() == null ? "" : u.getRole();
            if (r.equalsIgnoreCase("admin") && admin == null) admin = u;
            else if (r.equalsIgnoreCase("teacher") && teacher == null) teacher = u;
            else if (other == null) other = u;
        }

        if (admin == null && teacher == null) {
            if (other != null) other.setRoles(new ArrayList<>(Arrays.asList(
                    other.getRole() == null ? "student" : other.getRole())));
            return other;
        }

        Set<String> roles = new LinkedHashSet<>();
        UserModel base;
        if (admin != null) {
            base = admin;
            roles.add("admin");
            if (teacher != null) {
                roles.add("teacher");
                base.setCoursetype(teacher.getCoursetype());
                base.setCourseName(teacher.getCourseName());
                base.setBoardType(teacher.getBoardType());
                base.setStandards(teacher.getStandards());
                base.setSubjects(teacher.getSubjects());
            }
        } else {
            base = teacher;
            roles.add("teacher");
        }
        base.setRoles(new ArrayList<>(roles));   // not saved - merged copy only for the response
        return base;
    }

    /** Remove password data before the user object goes to the browser. */
    public UserModel sanitized(UserModel u) {
        if (u != null) {
            u.setPassword(null);
            u.setPasswordEnc(null);
        }
        return u;
    }

    private void backfillPasswordEnc(UserModel u, String rawPassword) {
        try {
            if (u.getId() == null) return;
            if (u.getPasswordEnc() != null && !u.getPasswordEnc().isEmpty()) return;
            String enc = passwordCrypto.encrypt(rawPassword);
            if (enc == null) return;
            template().updateFirst(
                    new Query(Criteria.where("_id").is(u.getId())),
                    new Update().set("passwordEnc", enc),
                    UserModel.class, collectionName);
            u.setPasswordEnc(enc);
        } catch (Exception e) {
            System.err.println("Could not store viewable password: " + e.getMessage());
        }
    }

    private boolean passwordMatches(String raw, String stored) {
        if (stored == null || stored.isEmpty()) return false;
        if (stored.startsWith("$2")) {
            try { return passwordEncoder.matches(raw, stored); } catch (Exception e) { return false; }
        }
        return stored.equals(raw); // legacy plain-text record
    }

    // ───────────── LINKED ACCOUNTS (used right after login by the frontend) ─────────────
    /**
     * Accounts that share this email AND the same password (compared through passwordEnc).
     * Returns which roles exist plus the admin's service and the teacher's course data.
     * Accounts with a different password are NOT linked.
     */
    public Map<String, Object> getLinkedProfile(String gmail, String role) {
        Map<String, Object> res = new HashMap<>();
        List<UserModel> same = template().find(
                new Query(Criteria.where("gmail").is(gmail)), UserModel.class, collectionName);

        UserModel self = null;
        for (UserModel u : same) {
            if (u.getRole() != null && u.getRole().equalsIgnoreCase(role)) { self = u; break; }
        }
        if (self == null) {
            res.put("status", "failed");
            return res;
        }

        String selfPw = passwordCrypto.decrypt(self.getPasswordEnc());
        UserModel admin = null;
        UserModel teacher = null;

        for (UserModel u : same) {
            boolean linked = (u == self)
                    || (selfPw != null && selfPw.equals(passwordCrypto.decrypt(u.getPasswordEnc())));
            if (!linked || u.getRole() == null) continue;
            if (u.getRole().equalsIgnoreCase("admin") && admin == null) admin = u;
            else if (u.getRole().equalsIgnoreCase("teacher") && teacher == null) teacher = u;
        }

        List<String> roles = new ArrayList<>();
        if (admin != null) {
            roles.add("admin");
            Map<String, Object> a = new HashMap<>();
            a.put("service", admin.getService() == null ? "" : admin.getService());
            res.put("admin", a);
        }
        if (teacher != null) {
            roles.add("teacher");
            Map<String, Object> t = new HashMap<>();
            t.put("coursetype", teacher.getCoursetype());
            t.put("courseName", teacher.getCourseName());
            t.put("boardType", teacher.getBoardType());
            t.put("standards", teacher.getStandards());
            t.put("subjects", teacher.getSubjects());
            res.put("teacher", t);
        }

        res.put("status", "pass");
        res.put("roles", roles);
        return res;
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
                    user.setPasswordEnc(passwordCrypto.encrypt(pw));
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