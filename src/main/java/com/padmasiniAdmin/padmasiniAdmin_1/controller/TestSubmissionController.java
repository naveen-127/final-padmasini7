package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.TestSubmission;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.TestSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test-submissions")
public class TestSubmissionController {

    @Autowired
    private TestSubmissionRepository repository;

    // Fetch all submissions
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

    // NEW: Serve images from the local 'uploads' folder
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            // Ensure this matches the folder where your student images are actually stored
        	Path filePath = Paths.get("uploads", "tests").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) 
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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