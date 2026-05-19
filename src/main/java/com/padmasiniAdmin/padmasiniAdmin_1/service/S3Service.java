// service/S3Service.java
package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.MediaItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Service
public class S3Service {
    
    @Autowired
    private S3Client s3Client;
    
    @Autowired
    private S3Presigner s3Presigner;
    
    @Value("${aws.bucket.name}")
    private String bucketName;
    
    @Value("${aws.region}")
    private String region;
    
    public List<MediaItem> uploadFiles(List<MultipartFile> files, String type) throws IOException {
        List<MediaItem> uploadedItems = new ArrayList<>();
        String folder = type.equals("poster") ? "posters/" : "videos/";
        
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            String s3Key = folder + uniqueFilename;
            
            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();
            
            s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // Generate presigned URL for viewing (valid for 7 days)
            String fileUrl = generatePresignedUrl(s3Key);
            
            // Create MediaItem
            MediaItem mediaItem = new MediaItem();
            mediaItem.setFilename(originalFilename);
            mediaItem.setUrl(fileUrl);
            mediaItem.setS3Key(s3Key);
            mediaItem.setType(type);
            mediaItem.setSize(file.getSize());
            mediaItem.setContentType(file.getContentType());
            mediaItem.setUploadedAt(new Date());
            
            uploadedItems.add(mediaItem);
        }
        
        return uploadedItems;
    }
    
    private String generatePresignedUrl(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(7))
                    .getObjectRequest(getObjectRequest)
                    .build();
            
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            // Fallback to public URL if presigning fails
            return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, s3Key);
        }
    }
    
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage());
        }
    }
}