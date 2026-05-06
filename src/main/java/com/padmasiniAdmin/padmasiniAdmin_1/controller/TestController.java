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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired
    private TestPaperRepository repository;

    private final Region region;
    private final String bucketName;
    private final S3Client s3Client;

    public TestController(
            @Value("${aws.region:ap-south-1}") String awsRegion,
            @Value("${aws.bucket.name:trilokinnovations-test-admin}") String bucketName
    ) {
        this.region = Region.of(awsRegion);
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(region)
                .build();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTest(
            @RequestParam("title") String title,
            @RequestParam("questions") String questions,
            @RequestParam("boardType") String boardType,
            @RequestParam("subject") String subject,
            @RequestParam("standard") String standard,
            @RequestParam(value = "teacherId", required = false) String teacherId,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            if (title.isEmpty() || subject.isEmpty()) {
                return ResponseEntity.badRequest().body("Title and Subject are required");
            }

            TestPaper test = new TestPaper();
            test.setTitle(title);
            test.setQuestions(questions);
            test.setBoardType(boardType);
            test.setSubject(subject);
            test.setStandard(standard);
            test.setTeacherId(teacherId);

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
            @RequestParam("questions") String questions,
            @RequestParam("boardType") String boardType,
            @RequestParam("subject") String subject,
            @RequestParam("standard") String standard,
            @RequestParam(value = "teacherId", required = false) String teacherId,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Optional<TestPaper> optionalTest = repository.findById(id);
            if (optionalTest.isPresent()) {
                TestPaper existingTest = optionalTest.get();
                existingTest.setTitle(title);
                existingTest.setQuestions(questions);
                existingTest.setBoardType(boardType);
                existingTest.setSubject(subject);
                existingTest.setStandard(standard);
                existingTest.setTeacherId(teacherId);

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
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String key = "tests/" + fileName;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        String fileUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key;
        test.setFilePath(fileUrl);
    }
}