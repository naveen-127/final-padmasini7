package com.padmasiniAdmin.padmasiniAdmin_1.controller.imageController;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final Region region;
    private final String bucketName;
    private final StaticCredentialsProvider credentialsProvider;
    private final S3Client s3Client;

    public ImageController(
            @Value("${aws.region:ap-south-1}") String awsRegion,
            @Value("${aws.bucket.name:trilokinnovations-test-admin}") String bucketName,
            @Value("${aws.access.key:}") String accessKey,
            @Value("${aws.secret.key:}") String secretKey
    ) {
        this.region = Region.of(awsRegion);
        this.bucketName = bucketName;
        
        System.out.println("=== AWS Configuration ===");
        System.out.println("Region: " + awsRegion);
        System.out.println("Bucket: " + bucketName);
        System.out.println("Access Key provided: " + (!accessKey.isEmpty() ? "YES" : "NO"));
        System.out.println("Secret Key provided: " + (!secretKey.isEmpty() ? "YES" : "NO"));
        
        // Always use explicit credentials from application.properties
        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            System.out.println("Using credentials from application.properties");
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
            this.credentialsProvider = StaticCredentialsProvider.create(awsCreds);
        } else {
            System.out.println("⚠️ WARNING: No AWS credentials found in application.properties!");
            System.out.println("Please add: aws.access.key=YOUR_KEY and aws.secret.key=YOUR_SECRET");
            throw new RuntimeException("AWS credentials not configured");
        }
        
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
        
        System.out.println("AWS Client initialized successfully");
        System.out.println("===========================");
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderName") String folderName) {

        System.out.println("=== Upload Request ===");
        System.out.println("File: " + file.getOriginalFilename());
        System.out.println("Size: " + file.getSize());
        System.out.println("Folder: " + folderName);

        try {
            String key = folderName + "/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            String fileUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key;
            
            System.out.println("✅ Upload successful!");
            System.out.println("URL: " + fileUrl);

            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));

        } catch (Exception e) {
            System.out.println("❌ Upload failed!");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Simple test endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Image controller is working!");
    }
}
