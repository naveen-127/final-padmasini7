package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UserDetails;
import com.padmasiniAdmin.padmasiniAdmin_1.service.SignInService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@RestController
public class SignINController {
	
	@Autowired 
	private SignInService signInService;
	@GetMapping("/UserSubjectStd")
	public Map<String,List<String>> getsubject(HttpSession session,HttpServletResponse response){
		Map<String , List<String>> map= new HashMap<String, List<String>>();
		if(session.getAttribute("user")!=null) {
			map.put("subject", (List<String>) session.getAttribute("subjects"));
			map.put("standards", (List<String>) session.getAttribute("standards"));
		}
		return map;
	}
@PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody UserDetails user, HttpSession session, HttpServletResponse response) {
        System.out.println("Checking password for: " + user.getUserName());
        Map<String, String> map = new HashMap<>();

        UserModel checkUser1 = signInService.checkUserName(user.getUserName(), user.getPassword());
        UserModel checkUser2 = signInService.checkUserGmail(user.getUserName(), user.getPassword());

        if (checkUser1 == null && checkUser2 == null) {
            map.put("status", "failed");
        } else {
            UserModel userr = (checkUser1 == null) ? checkUser2 : checkUser1;

            // Store user details in session
            session.setAttribute("user", userr.getUserName());
            session.setAttribute("phoneNumber", userr.getPhoneNumber());
            session.setAttribute("gmail", userr.getGmail());
            session.setAttribute("role", userr.getRole());
            session.setAttribute("coursetype", userr.getCoursetype());
            session.setAttribute("courseName", userr.getCourseName());
            session.setAttribute("subjects", userr.getSubjects());
            session.setAttribute("standards", userr.getStandards());

            map.put("status", "pass");
            map.put("userName", userr.getUserName());
            map.put("userGmail", userr.getGmail());
            map.put("phoneNumber", userr.getPhoneNumber());
            map.put("role", userr.getRole());
            map.put("coursetype", userr.getCoursetype());
            map.put("courseName", userr.getCourseName());

            System.out.println("Session created (signIn) id: " + session.getId());
        }

        return ResponseEntity.ok(map);
    }

    // Logout endpoint
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        System.out.println("Logout called. Session id before invalidate: " + session.getId());
        session.invalidate();

        // Remove JSESSIONID cookie
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        Map<String, String> map = new HashMap<>();
        map.put("message", "Logged out successfully");
        return ResponseEntity.ok(map);
    }
@GetMapping("/checkSession")
public ResponseEntity<?> checkSession(HttpSession session, HttpServletResponse response){
	System.out.println("Session ID: " + session.getId());
	System.out.println("Session Attribute 'user': " + session.getAttribute("user"));

	Map<String, Object> map = new HashMap<>();
	if(session.getAttribute("user")==null) {
		System.out.println("inside checksession user null");
		session.invalidate();
		 Cookie cookie = new Cookie("user", null);
		    cookie.setMaxAge(0); // Deletes cookie
		    cookie.setPath("/");
		    response.addCookie(cookie);
		    map.put("status", "failed");
	}
	else if (session.getAttribute("user") != null) {
	        map.put("status", "pass");
	        map.put("userName", session.getAttribute("user"));
	        map.put("phoneNumber",session.getAttribute("phoneNumber"));
	        map.put("userGmail", session.getAttribute("gmail"));
	        map.put("role", session.getAttribute("role"));
	        map.put("coursetype", session.getAttribute("coursetype"));
	        map.put("courseName", session.getAttribute("courseName"));
	        //response.put("user", session.getAttribute("user"));
	    } else {
	        map.put("status", "failed");
	    }
	    return ResponseEntity.ok(map);
}
}
