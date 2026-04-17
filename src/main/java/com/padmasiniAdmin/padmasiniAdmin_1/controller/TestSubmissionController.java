package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.TestSubmission;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.TestSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test-submissions")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TestSubmissionController {

    @Autowired
    private TestSubmissionRepository repository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllSubmissions() {
        try {
            List<TestSubmission> submissions = repository.findAll();
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving submissions: " + e.getMessage());
        }
    }

    @GetMapping("/test/{testPaperId}")
    public ResponseEntity<?> getSubmissionsByTest(@PathVariable String testPaperId) {
        try {
            List<TestSubmission> submissions = repository.findByTestPaperId(testPaperId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving submissions for test: " + e.getMessage());
        }
    }

    @PutMapping("/evaluate/{id}")
    public ResponseEntity<?> evaluateSubmission(
            @PathVariable String id,
            @RequestBody TestSubmission updateData) {
        try {
            Optional<TestSubmission> optionalSubmission = repository.findById(id);
            if (optionalSubmission.isPresent()) {
                TestSubmission existing = optionalSubmission.get();
                existing.setStatus("Evaluated");
                existing.setMarks(updateData.getMarks());
                existing.setRemarks(updateData.getRemarks());
                // Can also save who checked it or set checkedAt timestamp if we added that field
                // existing.setSubmittedAt(LocalDateTime.now()); // don't override submission time

                TestSubmission saved = repository.save(existing);
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error evaluating submission: " + e.getMessage());
        }
    }
}
