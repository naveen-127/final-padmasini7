package com.padmasiniAdmin.padmasiniAdmin_1.controller.imageController;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final Region region;
    private final String bucketName;
    private final S3Client s3Client;

    public ImageController(
            @Value("${aws.region:ap-south-1}") String awsRegion,
            @Value("${aws.bucket.name:trilokinnovations-test-admin}") String bucketName
    ) {
        this.region = Region.of(awsRegion);
        this.bucketName = bucketName;

        // ✅ IAM Role credentials are auto-resolved by AWS SDK
        this.s3Client = S3Client.builder()
                .region(region)
                .build();

        System.out.println("✅ S3 client initialized using IAM Role");
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderName") String folderName) {

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

            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Image controller is working!");
    }
}
