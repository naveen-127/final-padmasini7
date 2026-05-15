package com.padmasiniAdmin.padmasiniAdmin_1.controller;

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
import com.padmasiniAdmin.padmasiniAdmin_1.utils.PasswordEncoder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class SignINController {
    
    @Autowired 
    private SignInService signInService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/UserSubjectStd")
    public Map<String,List<String>> getsubject(HttpSession session) {
        Map<String , List<String>> map = new HashMap<>();
        if(session.getAttribute("user") != null) {
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
        
        // IMPORTANT: Use the check methods that verify password and auto-migrate
        UserModel authenticatedUser = signInService.checkUserName(user.getUserName(), user.getPassword());
        
        if (authenticatedUser == null) {
            authenticatedUser = signInService.checkUserGmail(user.getUserName(), user.getPassword());
        }
        
        if (authenticatedUser == null) {
            System.out.println("❌ Login failed for: " + user.getUserName());
            map.put("status", "failed");
            map.put("message", "Invalid username/email or password");
        } else {
            System.out.println("✅ Login successful for: " + authenticatedUser.getUserName());
            
            // Return ALL user data in response for frontend storage
            map.put("status", "pass");
            map.put("userName", authenticatedUser.getUserName());
            map.put("userGmail", authenticatedUser.getGmail());
            map.put("phoneNumber", authenticatedUser.getPhoneNumber());
            map.put("role", authenticatedUser.getRole());
            map.put("coursetype", authenticatedUser.getCoursetype());
            map.put("courseName", authenticatedUser.getCourseName());
            map.put("subjects", authenticatedUser.getSubjects());
            map.put("standards", authenticatedUser.getStandards());
            map.put("id", authenticatedUser.getId());
            
            // Set session for server-side operations
            session.setAttribute("user", authenticatedUser.getUserName());
            session.setAttribute("id", authenticatedUser.getId());
            session.setAttribute("phoneNumber", authenticatedUser.getPhoneNumber());
            session.setAttribute("gmail", authenticatedUser.getGmail());
            session.setAttribute("role", authenticatedUser.getRole());
            session.setAttribute("coursetype", authenticatedUser.getCoursetype());
            session.setAttribute("courseName", authenticatedUser.getCourseName());
            session.setAttribute("subjects", authenticatedUser.getSubjects());
            session.setAttribute("standards", authenticatedUser.getStandards());
            
            System.out.println("Session attributes set for user: " + authenticatedUser.getUserName());
        }
        
        return ResponseEntity.ok(map);
    }
    
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        System.out.println("inside logout" + session.getAttribute("user"));
        if(session.getAttribute("user") != null) {
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
        
        // Debug all attributes
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            System.out.println("Session Attribute: " + name + " = " + session.getAttribute(name));
        }
        
        // Check cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("Cookie: " + cookie.getName() + " = " + cookie.getValue());
            }
        }
        
        System.out.println("=== END SESSION DEBUG ===");

        Map<String, Object> map = new HashMap<>();
        if(session.getAttribute("user") != null) {
            System.out.println("Session is valid for user: " + session.getAttribute("user"));
            map.put("status", "pass");
            map.put("userName", session.getAttribute("user"));
            map.put("phoneNumber", session.getAttribute("phoneNumber"));
            map.put("userGmail", session.getAttribute("gmail"));
            map.put("role", session.getAttribute("role"));
            map.put("coursetype", session.getAttribute("coursetype"));
            map.put("courseName", session.getAttribute("courseName"));
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