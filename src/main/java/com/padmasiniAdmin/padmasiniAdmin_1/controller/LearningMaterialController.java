package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.LearningMaterial;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.LearningMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/learning-materials")
public class LearningMaterialController {

    @Autowired
    private LearningMaterialRepository repository;

    private final Region region;
    private final String bucketName;
    private final S3Client s3Client;

    public LearningMaterialController(
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
    public ResponseEntity<?> createLearningMaterial(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam("standard") String standard,
            @RequestParam("materialType") String materialType,
            @RequestParam(value = "assignedStudents", required = false) List<String> assignedStudents,
            @RequestParam(value = "assignedBatches", required = false) List<String> assignedBatches,
            @RequestParam(value = "videoLink", required = false) String videoLink,
            @RequestParam(value = "teacherId", required = false) String teacherId,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            LearningMaterial material = new LearningMaterial();
            material.setTitle(title);
            material.setDescription(description);
            material.setSubject(subject);
            material.setStandard(standard);
            material.setMaterialType(materialType);
            material.setAssignedStudents(assignedStudents);
            material.setAssignedBatches(assignedBatches);
            material.setVideoLink(videoLink);
            material.setTeacherId(teacherId);

            if (file != null && !file.isEmpty()) {
                saveFile(file, material);
            }

            LearningMaterial saved = repository.save(material);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllMaterials() {
        try {
            return ResponseEntity.ok(repository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable String id) {
        try {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                return ResponseEntity.ok().body("Material deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete Error: " + e.getMessage());
        }
    }

    private void saveFile(MultipartFile file, LearningMaterial material) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String folder = material.getMaterialType().equalsIgnoreCase("Video") ? "videos/" : "notes/";
        String key = folder + fileName;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        String fileUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key;
        material.setFilePath(fileUrl);
    }
}
