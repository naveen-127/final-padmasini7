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
import java.util.Optional;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") 
public class TestController {

    @Autowired
    private TestPaperRepository repository;

    private final String UPLOAD_DIR = "uploads/tests";

    @PostMapping("/create")
    public ResponseEntity<?> createTest(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("subject") String subject,
            @RequestParam("standard") String standard,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            if (title.isEmpty() || subject.isEmpty()) {
                return ResponseEntity.badRequest().body("Title and Subject are required");
            }

            TestPaper test = new TestPaper();
            test.setTitle(title);
            test.setContent(content);
            test.setSubject(subject);
            test.setStandard(standard);

            if (file != null && !file.isEmpty()) {
                saveFile(file, test);
            }

            TestPaper savedTest = repository.save(test);
            return ResponseEntity.ok(savedTest);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTest(
            @PathVariable String id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("subject") String subject,
            @RequestParam("standard") String standard,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Optional<TestPaper> optionalTest = repository.findById(id);
            if (optionalTest.isPresent()) {
                TestPaper existingTest = optionalTest.get();
                existingTest.setTitle(title);
                existingTest.setContent(content);
                existingTest.setSubject(subject);
                existingTest.setStandard(standard);

                if (file != null && !file.isEmpty()) {
                    saveFile(file, existingTest);
                }

                TestPaper updated = repository.save(existingTest);
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Update Error: " + e.getMessage());
        }
    }

   
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTest(@PathVariable String id) {
        try {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                return ResponseEntity.ok().body("Test deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete Error: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTests() {
        try {
            List<TestPaper> tests = repository.findAll();
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    
    private void saveFile(MultipartFile file, TestPaper test) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        test.setFilePath(fileName);
    }
}