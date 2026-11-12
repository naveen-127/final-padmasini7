package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UserDetails;
import com.padmasiniAdmin.padmasiniAdmin_1.service.SignInService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class SignINController {

    @Autowired
    private SignInService signInService;

    @SuppressWarnings("unchecked")
    @GetMapping("/UserSubjectStd")
    public Map<String, List<String>> getsubject(HttpSession session) {
        Map<String, List<String>> map = new HashMap<>();
        if (session.getAttribute("user") != null) {
            map.put("subject", (List<String>) session.getAttribute("subjects"));
            map.put("standards", (List<String>) session.getAttribute("standards"));
        }
        return map;
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody UserDetails user, HttpSession session) {
        System.out.println("=== SIGNIN CONTROLLER ===");
        System.out.println("Login attempt for: " + user.getUserName());
        Map<String, Object> map = new HashMap<>();

        // Checks every account with this username/email; only those whose password matches are used.
        // Teacher + admin with the same password -> merged access. Different passwords -> own account.
        UserModel u = signInService.authenticate(user.getUserName(), user.getPassword());

        if (u == null) {
            map.put("status", "failed");
            map.put("message", "Invalid username/email or password");
            return ResponseEntity.ok(map);
        }

        List<String> roles = u.getRoles() == null ? new ArrayList<String>() : u.getRoles();
        String service = u.getService() == null ? "" : u.getService();

        // ---- response for the frontend (never contains a password) ----
        map.put("status", "pass");
        map.put("userName", u.getUserName());
        map.put("userGmail", u.getGmail());
        map.put("phoneNumber", u.getPhoneNumber());
        map.put("role", u.getRole());
        map.put("roles", roles);
        map.put("service", service);
        map.put("coursetype", u.getCoursetype());
        map.put("courseName", u.getCourseName());
        map.put("boardType", u.getBoardType());
        map.put("subjects", u.getSubjects());
        map.put("standards", u.getStandards());
        map.put("id", u.getId());

        // ---- server-side session ----
        session.setAttribute("user", u.getUserName());
        session.setAttribute("id", u.getId());
        session.setAttribute("phoneNumber", u.getPhoneNumber());
        session.setAttribute("gmail", u.getGmail());
        session.setAttribute("role", u.getRole());
        session.setAttribute("roles", roles);
        session.setAttribute("service", service);   // used to allow password viewing for full admins only
        session.setAttribute("coursetype", u.getCoursetype());
        session.setAttribute("courseName", u.getCourseName());
        session.setAttribute("boardType", u.getBoardType());
        session.setAttribute("subjects", u.getSubjects());
        session.setAttribute("standards", u.getStandards());

        System.out.println("Session set for user: " + u.getUserName() + " roles=" + roles);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        System.out.println("inside logout " + session.getAttribute("user"));
        if (session.getAttribute("user") != null) {
            session.invalidate();
            Cookie cookie = new Cookie("user", null);
            cookie.setMaxAge(0); // Deletes cookie
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseEntity.ok("pass");
        }
        return ResponseEntity.ok("failed");
    }

    @GetMapping("/checkSession")
    public ResponseEntity<?> checkSession(HttpSession session, HttpServletRequest request) {

        System.out.println("=== SESSION DEBUG ===");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Session isNew: " + session.isNew());

        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            System.out.println("Session Attribute: " + name + " = " + session.getAttribute(name));
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("Cookie: " + cookie.getName());
            }
        }
        System.out.println("=== END SESSION DEBUG ===");

        Map<String, Object> map = new HashMap<>();
        if (session.getAttribute("user") != null) {
            System.out.println("Session is valid for user: " + session.getAttribute("user"));
            map.put("status", "pass");
            map.put("userName", session.getAttribute("user"));
            map.put("phoneNumber", session.getAttribute("phoneNumber"));
            map.put("userGmail", session.getAttribute("gmail"));
            map.put("role", session.getAttribute("role"));
            map.put("roles", session.getAttribute("roles"));
            map.put("service", session.getAttribute("service"));
            map.put("coursetype", session.getAttribute("coursetype"));
            map.put("courseName", session.getAttribute("courseName"));
            map.put("boardType", session.getAttribute("boardType"));
            map.put("subjects", session.getAttribute("subjects"));
            map.put("standards", session.getAttribute("standards"));
            map.put("id", session.getAttribute("id"));
        } else {
            System.out.println("inside checksession user null");
            map.put("status", "failed");
        }
        return ResponseEntity.ok(map);
    }
}