package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.ClassAssignment;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    @Autowired
    private AssignmentRepository repository;

    @PostMapping("/assignClass")
    public ResponseEntity<Map<String, String>> assignClass(@RequestBody ClassAssignment assignment) {
        Map<String, String> response = new HashMap<>();
        try {
            // MongoDB .save() handles both Insert and Update (if ID is present)
            repository.save(assignment);
            response.put("status", "pass");
            response.put("message", "Batch processed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAssignedClasses")
    public ResponseEntity<List<ClassAssignment>> getAssignedClasses() {
        try {
            return ResponseEntity.ok(repository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/deleteAssignedClass/{id}")
    public ResponseEntity<Map<String, String>> deleteClass(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            repository.deleteById(id);
            response.put("status", "pass");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "failed");
            return ResponseEntity.status(500).body(response);
        }
    }
}