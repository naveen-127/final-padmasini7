// service/MediaService.java
package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.MediaItem;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MediaService {
    
    @Autowired
    private MediaRepository mediaRepository;
    
    @Autowired
    private S3Service s3Service;
    
    public List<MediaItem> uploadMedia(List<MultipartFile> files, String type) throws Exception {
        // Upload to S3
        List<MediaItem> mediaItems = s3Service.uploadFiles(files, type);
        
        // Save to MongoDB
        return mediaRepository.saveAll(mediaItems);
    }
    
    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public List<MediaItem> migrateToPermanentUrls() {
        List<MediaItem> allMedia = mediaRepository.findAll();
        List<MediaItem> migratedMedia = new ArrayList<>();
        
        for (MediaItem media : allMedia) {
            if (media.getS3Key() != null && !media.getS3Key().isEmpty()) {
                // Generate permanent URL from S3 key
                String permanentUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    bucketName, region, media.getS3Key());
                
                // Update the URL
                media.setUrl(permanentUrl);
                migratedMedia.add(mediaRepository.save(media));
                
                System.out.println("Migrated: " + media.getFilename() + " -> " + permanentUrl);
            }
        }
        
        return migratedMedia;
    }
    
    public Map<String, List<Map<String, Object>>> getAllMedia() {
        List<MediaItem> allMedia = mediaRepository.findAll();
        
        List<Map<String, Object>> posters = allMedia.stream()
                .filter(media -> "poster".equals(media.getType()))
                .map(this::convertToMap)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> videos = allMedia.stream()
                .filter(media -> "video".equals(media.getType()))
                .map(this::convertToMap)
                .collect(Collectors.toList());
        
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("posters", posters);
        result.put("videos", videos);
        
        return result;
    }
    
    
    
    public void deleteMedia(String mediaId, String type) throws Exception {
        MediaItem mediaItem = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + mediaId));
        
        // Delete from S3
        s3Service.deleteFile(mediaItem.getS3Key());
        
        // Delete from MongoDB
        mediaRepository.deleteById(mediaId);
    }
    
    private Map<String, Object> convertToMap(MediaItem media) {
        Map<String, Object> map = new HashMap<>();
        map.put("_id", media.getId());
        map.put("filename", media.getFilename());
        map.put("url", media.getUrl());
        map.put("type", media.getType());
        map.put("size", media.getSize());
        map.put("contentType", media.getContentType());
        map.put("uploadedAt", media.getUploadedAt());
        return map;
    }
}