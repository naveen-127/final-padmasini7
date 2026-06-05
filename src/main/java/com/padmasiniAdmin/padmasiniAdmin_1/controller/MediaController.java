// controller/MediaController.java
package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.MediaItem;
import com.padmasiniAdmin.padmasiniAdmin_1.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MediaController {
    
    @Autowired
    private MediaService mediaService;
    
    
    @GetMapping("/migrateToPermanentUrls")
    public ResponseEntity<?> migrateToPermanentUrls() {
        try {
            List<MediaItem> migratedMedia = mediaService.migrateToPermanentUrls();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Successfully migrated " + migratedMedia.size() + " media items");
            response.put("count", migratedMedia.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/uploadMedia")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("media") List<MultipartFile> media,
            @RequestParam("type") String type) {
        try {
            List<MediaItem> uploadedMedia = mediaService.uploadMedia(media, type);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Successfully uploaded " + uploadedMedia.size() + " files");
            response.put("data", uploadedMedia);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/getMedia")
    public ResponseEntity<?> getMedia() {
        try {
            Map<String, List<Map<String, Object>>> media = mediaService.getAllMedia();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("posters", media.get("posters"));
            response.put("videos", media.get("videos"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/deleteMedia/{mediaId}")
    public ResponseEntity<?> deleteMedia(
            @PathVariable String mediaId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String type = body != null ? body.get("type") : null;
            mediaService.deleteMedia(mediaId, type);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Media deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}