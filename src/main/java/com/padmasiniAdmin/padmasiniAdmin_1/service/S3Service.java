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

import java.io.IOException;
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
            
            // Upload to S3 with PUBLIC READ access (THIS IS THE KEY CHANGE)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)  // ✅ ADD THIS LINE - Makes file public
                    .build();
            
            s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // ✅ GENERATE PERMANENT URL (never expires) - REPLACE the presigned URL method
            String permanentUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, s3Key);
            
            // Create MediaItem with PERMANENT URL
            MediaItem mediaItem = new MediaItem();
            mediaItem.setFilename(originalFilename);
            mediaItem.setUrl(permanentUrl);  // ✅ NOW USING PERMANENT URL
            mediaItem.setS3Key(s3Key);
            mediaItem.setType(type);
            mediaItem.setSize(file.getSize());
            mediaItem.setContentType(file.getContentType());
            mediaItem.setUploadedAt(new Date());
            
            uploadedItems.add(mediaItem);
        }
        
        return uploadedItems;
    }
    
    // ✅ REMOVE or COMMENT OUT the generatePresignedUrl method - no longer needed
    // private String generatePresignedUrl(String s3Key) { ... }
    
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