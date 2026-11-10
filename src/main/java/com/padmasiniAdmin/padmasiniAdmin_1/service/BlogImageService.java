package com.padmasiniAdmin.padmasiniAdmin_1.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.padmasiniAdmin.padmasiniAdmin_1.model.MediaItem;
import com.padmasiniAdmin.padmasiniAdmin_1.service.BlogService.BlogException;

/**
 * Uploads blog images through the same S3Service the Media Manager uses,
 * and returns the permanent public URL. Nothing is saved to the media collection.
 */
@Service
public class BlogImageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");

    private final S3Service s3Service;

    // Same properties MediaService already uses
    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public BlogImageService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BlogException(HttpStatus.BAD_REQUEST, "Choose an image to upload.");
        }
        String type = file.getContentType();
        if (type == null || !ALLOWED.contains(type.toLowerCase())) {
            throw new BlogException(HttpStatus.BAD_REQUEST, "Only JPG, PNG or WebP images are allowed.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BlogException(HttpStatus.BAD_REQUEST, "Image must be 5 MB or smaller.");
        }

        try {
            // "poster" is the type the Media Manager already uses for images
            List<MediaItem> items = s3Service.uploadFiles(List.of(file), "poster");
            if (items == null || items.isEmpty()) {
                throw new BlogException(HttpStatus.BAD_GATEWAY, "Image upload returned no result.");
            }
            MediaItem item = items.get(0);

            // Permanent URL from the S3 key (same format as migrateToPermanentUrls)
            if (item.getS3Key() != null && !item.getS3Key().isEmpty()) {
                return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, item.getS3Key());
            }
            if (item.getUrl() != null && !item.getUrl().isEmpty()) {
                return item.getUrl();
            }
            throw new BlogException(HttpStatus.BAD_GATEWAY, "Image uploaded but no URL was returned.");
        } catch (BlogException e) {
            throw e;
        } catch (Exception e) {
            throw new BlogException(HttpStatus.BAD_GATEWAY, "Image upload failed: " + e.getMessage());
        }
    }
}