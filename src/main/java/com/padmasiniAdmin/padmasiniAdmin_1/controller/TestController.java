package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.TestPaper;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.TestPaperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/api/tests")
// Allow your React dev server (usually 5173) to access the Spring backend (Port 80)
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") 
public class TestController {

    @Autowired
    private TestPaperRepository repository;

    // Use a relative path without a leading slash for better compatibility
    private final String UPLOAD_DIR = "uploads/tests";

    @PostMapping("/create")
    public ResponseEntity<?> createTest(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("subject") String subject,
            @RequestParam("standard") String standard,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            // Validate required fields to prevent DB errors
            if (title.isEmpty() || subject.isEmpty()) {
                return ResponseEntity.badRequest().body("Title and Subject are required");
            }

            TestPaper test = new TestPaper();
            test.setTitle(title);
            test.setContent(content);
            test.setSubject(subject);
            test.setStandard(standard);

            if (file != null && !file.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                test.setFilePath(fileName);
            }

            TestPaper savedTest = repository.save(test);
            return ResponseEntity.ok(savedTest);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File System Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database Error: " + e.getMessage());
        }
    }

    @GetMapping("/subject/{subjectName}")
    public ResponseEntity<?> getTestsBySubject(@PathVariable String subjectName) {
        try {
            List<TestPaper> tests = repository.findBySubject(subjectName);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving tests: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTests() {
        try {
            List<TestPaper> tests = repository.findAll();
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving tests: " + e.getMessage());
        }
    }
}