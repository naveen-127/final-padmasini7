package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.TestPaperSubmission;
import com.padmasiniAdmin.padmasiniAdmin_1.model.TestPaper;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.TestPaperSubmissionRepository;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.TestPaperRepository;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.StudentModel;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/test-paper-submissions")
public class TestPaperSubmissionController {

    @Autowired
    private TestPaperSubmissionRepository repository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TestPaperRepository testPaperRepository;

    private final Region region;
    private final String bucketName;
    private final S3Client s3Client;

    public TestPaperSubmissionController(
            @Value("${aws.region:ap-south-1}") String awsRegion,
            @Value("${aws.bucket.name:trilokinnovations-test-admin}") String bucketName
    ) {
        this.region = Region.of(awsRegion);
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(region)
                .build();
    }

    // Fetch all submissions
    @GetMapping("/all")
    public ResponseEntity<?> getAllSubmissions() {
        try {
            List<TestPaperSubmission> submissions = repository.findAll();
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving submissions: " + e.getMessage());
        }
    }

    // Fetch submissions with student names
    @GetMapping("/with-names")
    public ResponseEntity<?> getSubmissionsWithNames() {
        try {
            List<TestPaperSubmission> submissions = repository.findAll();
            List<Map<String, Object>> result = submissions.stream().map(sub -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", sub.getId());
                map.put("testId", sub.getTestId());
                map.put("studentId", sub.getStudentId());
                map.put("answerText", sub.getAnswerText());
                map.put("filePath", sub.getFilePath());
                map.put("submittedAt", sub.getSubmittedAt());
                map.put("status", sub.getStatus());
                map.put("marks", sub.getMarks());
                map.put("remarks", sub.getRemarks());

                // Try to get test details
                try {
                    Optional<TestPaper> testPaper = testPaperRepository.findById(sub.getTestId());
                    if (testPaper.isPresent()) {
                        map.put("testTitle", testPaper.get().getTitle());
                        map.put("subject", testPaper.get().getSubject());
                        map.put("standard", testPaper.get().getStandard());
                        map.put("boardType", testPaper.get().getBoardType());
                        map.put("testTeacherId", testPaper.get().getTeacherId());
                    }
                } catch (Exception e) {
                    // Ignore
                }

                // Try to get student name
                try {
                    StudentModel student = studentService.getStudentById(sub.getStudentId());
                    if (student != null) {
                        map.put("studentName", student.getFirstname() + " " + student.getLastname());
                    } else {
                        map.put("studentName", "Unknown Student (" + sub.getStudentId() + ")");
                    }
                } catch (Exception e) {
                    map.put("studentName", "Student ID: " + sub.getStudentId());
                }
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving submissions with names: " + e.getMessage());
        }
    }

    @GetMapping("/proxy-image")
    public ResponseEntity<?> proxyImage(@RequestParam("url") String imageUrl) {
        try {
            // Encode spaces and other special characters if they aren't encoded
            String encodedUrl = imageUrl.replace(" ", "%20");
            URL url = new URL(encodedUrl);
            
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0"); // Add User-Agent to avoid blocks
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            if (status != java.net.HttpURLConnection.HTTP_OK) {
                return ResponseEntity.status(status).body("Failed to fetch image: " + status);
            }

            InputStream in = connection.getInputStream();
            String contentType = connection.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg";
                if (imageUrl.toLowerCase().endsWith(".png")) contentType = "image/png";
                else if (imageUrl.toLowerCase().endsWith(".pdf")) contentType = "application/pdf";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Proxy Error: " + e.getMessage());
        }
    }

    @PutMapping("/evaluate/{id}")
    public ResponseEntity<?> evaluateSubmission(
            @PathVariable String id,
            @RequestParam("marks") String marks,
            @RequestParam("remarks") String remarks,
            @RequestParam(value = "evaluationDetails", required = false) String evaluationDetailsJson,
            @RequestParam(value = "evaluatedFile", required = false) MultipartFile evaluatedFile) {
        try {
            Optional<TestPaperSubmission> optionalSubmission = repository.findById(id);
            if (optionalSubmission.isPresent()) {
                TestPaperSubmission existing = optionalSubmission.get();
                existing.setStatus("Evaluated");
                existing.setMarks(marks);
                existing.setRemarks(remarks);

                if (evaluationDetailsJson != null && !evaluationDetailsJson.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> details = mapper.readValue(evaluationDetailsJson, List.class);
                    existing.setEvaluationDetails(details);
                }

                if (evaluatedFile != null && !evaluatedFile.isEmpty()) {
                    String fileName = "evaluated_" + System.currentTimeMillis() + "_" + evaluatedFile.getOriginalFilename();
                    String key = "tuition_assessments/" + fileName;

                    s3Client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(key)
                                    .contentType(evaluatedFile.getContentType())
                                    .build(),
                            RequestBody.fromBytes(evaluatedFile.getBytes())
                    );

                    String fileUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key;
                    existing.setEvaluatedFilePath(fileUrl);
                }

                TestPaperSubmission saved = repository.save(existing);
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
