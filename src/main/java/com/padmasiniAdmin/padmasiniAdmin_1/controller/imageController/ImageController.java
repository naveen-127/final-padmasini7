package com.padmasiniAdmin.padmasiniAdmin_1.controller.imageController;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*") // allow frontend (Netlify) access
public class ImageController {

    private final Region region;
    private final String bucketName;
    private final DefaultCredentialsProvider credentialsProvider;

    public ImageController(
            @Value("${aws.region:ap-south-1}") String awsRegion,
            @Value("${aws.bucket.name:trilokinnovations-test-admin}") String bucketName
    ) {
        this.region = Region.of(awsRegion);
        this.bucketName = bucketName;
        this.credentialsProvider = DefaultCredentialsProvider.create();
    }

    /**
     * ✅ STEP 1: Generate Pre-signed URL
     * Frontend will call this endpoint first.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> generatePresignedUrl(
            @RequestParam("fileName") String fileName,
            @RequestParam("fileType") String fileType,
            @RequestParam("folderName") String folderName) {

        try (S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build()) {

            String key = folderName + "/" + System.currentTimeMillis() + "-" + fileName;

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(fileType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15)) // 15 min valid
                    .putObjectRequest(objectRequest)
                    .build();

            URL uploadUrl = presigner.presignPutObject(presignRequest).url();
            String fileUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key;

            Map<String, String> response = new HashMap<>();
            response.put("uploadUrl", uploadUrl.toString());
            response.put("fileUrl", fileUrl);

            System.out.println("✅ Presigned URL generated: " + uploadUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not generate presigned URL: " + e.getMessage()));
        }
    }
}
