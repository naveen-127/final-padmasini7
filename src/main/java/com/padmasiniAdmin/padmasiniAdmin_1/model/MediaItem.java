// model/MediaItem.java
package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "media")
public class MediaItem {
    @Id
    private String id;
    private String filename;
    private String url;
    private String s3Key;
    private String type; // "poster" or "video"
    private long size;
    private String contentType;
    private Date uploadedAt;
    
    public MediaItem() {}
    
    public MediaItem(String filename, String url, String s3Key, String type, 
                     long size, String contentType, Date uploadedAt) {
        this.filename = filename;
        this.url = url;
        this.s3Key = s3Key;
        this.type = type;
        this.size = size;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }
}