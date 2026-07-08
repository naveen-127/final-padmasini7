package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Visitor;
import com.padmasiniAdmin.padmasiniAdmin_1.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private MongoClient mongoClient; // reuses the existing connection Spring Boot already created

    // Get all visitors — reads from the studentUsers database directly, ignoring whatever DB is in the URI
    @GetMapping("/getVisitors")
    public ResponseEntity<?> getAllVisitors() {
        try {
            MongoTemplate studentUsersTemplate = new MongoTemplate(mongoClient, "studentUsers");
            List<Visitor> visitors = studentUsersTemplate.findAll(Visitor.class, "visitor");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", visitors);
            response.put("count", visitors.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to fetch visitors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get visitor by ID
    @GetMapping("/getVisitor/{id}")
    public ResponseEntity<?> getVisitorById(@PathVariable String id) {
        try {
            MongoTemplate studentUsersTemplate = new MongoTemplate(mongoClient, "studentUsers");
            Visitor visitor = studentUsersTemplate.findById(id, Visitor.class, "visitor");
            if (visitor != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("data", visitor);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Visitor not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to fetch visitor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get visitor statistics — same DB fix applied
    @GetMapping("/visitorStats")
    public ResponseEntity<?> getVisitorStats() {
        try {
            MongoTemplate studentUsersTemplate = new MongoTemplate(mongoClient, "studentUsers");
            List<Visitor> all = studentUsersTemplate.findAll(Visitor.class, "visitor");

            int total = all.size();
            long passed = all.stream().filter(v -> "Passed".equalsIgnoreCase(v.getStatus())).count();
            long failed = all.stream().filter(v -> "Failed".equalsIgnoreCase(v.getStatus())).count();
            double avgPercentage = all.stream().mapToDouble(Visitor::getPercentage).average().orElse(0.0);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("total", total);
            response.put("passed", passed);
            response.put("failed", failed);
            response.put("averagePercentage", avgPercentage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to get visitor statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Everything else (create/update/delete/search/byStatus) still uses visitorService as before,
    // since those aren't part of your immediate bug — leave them unchanged unless you want them
    // pointed at studentUsers too, in which case apply the same MongoTemplate pattern.

    @PostMapping("/createVisitor")
    public ResponseEntity<?> createVisitor(@RequestBody Visitor visitor) {
        try {
            Visitor savedVisitor = visitorService.saveVisitor(visitor);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Visitor created successfully");
            response.put("data", savedVisitor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to create visitor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/updateVisitor/{id}")
    public ResponseEntity<?> updateVisitor(@PathVariable String id, @RequestBody Visitor visitor) {
        try {
            Optional<Visitor> existing = visitorService.getVisitorById(id);
            if (existing.isPresent()) {
                visitor.setId(id);
                Visitor updated = visitorService.saveVisitor(visitor);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Visitor updated successfully");
                response.put("data", updated);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Visitor not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to update visitor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/deleteVisitor/{id}")
    public ResponseEntity<?> deleteVisitor(@PathVariable String id) {
        try {
            Optional<Visitor> existing = visitorService.getVisitorById(id);
            if (existing.isPresent()) {
                visitorService.deleteVisitor(id);
                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Visitor deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Visitor not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to delete visitor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/getVisitorsByStatus/{status}")
    public ResponseEntity<?> getVisitorsByStatus(@PathVariable String status) {
        try {
            List<Visitor> visitors = visitorService.getVisitorsByStatus(status);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", visitors);
            response.put("count", visitors.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to fetch visitors by status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/searchVisitors")
    public ResponseEntity<?> searchVisitors(@RequestParam String keyword) {
        try {
            List<Visitor> visitors = visitorService.searchVisitors(keyword);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", visitors);
            response.put("count", visitors.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to search visitors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}