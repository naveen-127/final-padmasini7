package com.padmasiniAdmin.padmasiniAdmin_1.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;
import com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordCrypto;
import com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder;

@Service
public class SignInService {

    private static final String DB = "users";
    private static final String COLLECTION = "users";

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordCrypto passwordCrypto;

    private MongoTemplate template() {
        return new MongoTemplate(mongoClient, DB);
    }

    // Find user by username (without password check)
    public UserModel findUserByUserName(String userName) {
        Query query = new Query(Criteria.where("userName").is(userName));
        return template().findOne(query, UserModel.class, COLLECTION);
    }

    // Find user by email (without password check)
    public UserModel findUserByEmail(String email) {
        Query query = new Query(Criteria.where("gmail").is(email));
        return template().findOne(query, UserModel.class, COLLECTION);
    }

    // ─────────────────────────────────────────────────────────────
    //  LOGIN
    //  Looks at EVERY account whose username OR email equals `identifier`
    //  and keeps only the ones whose password matches the typed password.
    //   - only the teacher matches -> teacher login
    //   - only the admin matches   -> admin login
    //   - both match               -> ONE merged login (role "admin", roles = [admin, teacher],
    //                                 teacher course data kept) = access of both
    //  So the same username with different passwords logs into the right account.
    //  Returns null when nothing matches.
    // ─────────────────────────────────────────────────────────────
    public UserModel authenticate(String identifier, String password) {
        if (identifier == null || password == null) return null;
        String id = identifier.trim();
        if (id.isEmpty() || password.isEmpty()) return null;

        Query q = new Query(new Criteria().orOperator(
                Criteria.where("userName").is(id),
                Criteria.where("gmail").is(id)));
        List<UserModel> candidates = template().find(q, UserModel.class, COLLECTION);

        UserModel admin = null;
        UserModel teacher = null;
        UserModel other = null;

        for (UserModel u : candidates) {
            if (!verifyAndUpgrade(u, password)) continue;

            String r = u.getRole() == null ? "" : u.getRole();
            if (r.equalsIgnoreCase("admin")) {
                if (admin == null) admin = u;
            } else if (r.equalsIgnoreCase("teacher")) {
                if (teacher == null) teacher = u;
            } else if (other == null) {
                other = u;
            }
        }

        if (admin == null && teacher == null) {
            if (other == null) {
                System.out.println("❌ Login failed for: " + id);
                return null;
            }
            other.setRoles(new ArrayList<>(Arrays.asList(
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
        base.setRoles(new ArrayList<>(roles)); // merged copy for the response/session only - never saved
        System.out.println("✅ Login OK for " + id + " roles=" + roles);
        return base;
    }

    // Kept so any other code that calls the old methods still works
    public UserModel checkUserName(String userName, String password) {
        return authenticate(userName, password);
    }

    public UserModel checkUserGmail(String gmail, String password) {
        return authenticate(gmail, password);
    }

    /**
     * Checks the typed password against ONE account (BCrypt hash or old plain text).
     * On success it also:
     *   - hashes an old plain-text password,
     *   - stores the reversible copy (passwordEnc) when missing, so a full admin can view it later.
     */
    private boolean verifyAndUpgrade(UserModel u, String raw) {
        String stored = u.getPassword();
        if (stored == null || stored.isEmpty()) return false;

        boolean matched;
        boolean plain = !stored.startsWith("$2");
        if (plain) {
            matched = stored.equals(raw);
        } else {
            try {
                matched = passwordEncoder.matches(raw, stored);
            } catch (Exception e) {
                matched = false;
            }
        }
        if (!matched) return false;

        try {
            boolean needEnc = u.getPasswordEnc() == null || u.getPasswordEnc().isEmpty();
            if ((plain || needEnc) && u.getId() != null) {
                Update up = new Update();
                if (needEnc) {
                    String enc = passwordCrypto.encrypt(raw);
                    if (enc != null) {
                        up.set("passwordEnc", enc);
                        u.setPasswordEnc(enc);
                    }
                }
                if (plain) {
                    String hashed = passwordEncoder.encodePassword(raw);
                    up.set("password", hashed);
                    u.setPassword(hashed);
                    System.out.println("✅ Password migrated to BCrypt for: " + u.getUserName());
                }
                if (!up.getUpdateObject().isEmpty()) {
                    template().updateFirst(
                            new Query(Criteria.where("_id").is(u.getId())),
                            up, UserModel.class, COLLECTION);
                }
            }
        } catch (Exception e) {
            // login must still work even if this housekeeping fails
            System.err.println("Could not upgrade password data: " + e.getMessage());
        }
        return true;
    }
}