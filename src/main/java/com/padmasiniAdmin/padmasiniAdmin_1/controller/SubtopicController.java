package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.result.UpdateResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SubtopicController {

    @Autowired
    private MongoTemplate mongoTemplate;

    // SIMPLIFIED: Direct update endpoint
    @PutMapping("/updateSubtopicVideo")
    public ResponseEntity<?> updateSubtopicVideo(@RequestBody UpdateSubtopicRequest request) {
        try {
            System.out.println("🔄 Spring Boot: Updating subtopic video");
            System.out.println("📋 Request: " + request.toString());

            String collectionName = request.getSubjectName();
            if (collectionName == null || collectionName.trim().isEmpty()) { 
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "subjectName is required",
                    "message", "Please provide the collection/subject name"
                ));
            }

            UpdateResult result = null;
            String queryUsed = "";
            boolean updated = false;

            // Try different query strategies
            String[] strategies = {
                "units._id", "units.id", "_id", "children._id", "children.id", 
                "subtopics._id", "subtopics.id"
            };

            for (String field : strategies) {
                try {
                    Query query = new Query(Criteria.where(field).is(request.getSubtopicId()));
                    
                    // Determine if it's a nested field (contains dot)
                    Update update;
                    if (field.contains(".")) {
                        // Nested field update
                        String arrayField = field.substring(0, field.lastIndexOf('.'));
                        String arrayElement = "$." + field.substring(field.lastIndexOf('.') + 1);
                        update = new Update().set(arrayField + ".aiVideoUrl", request.getAiVideoUrl())
                                             .set(arrayField + ".updatedAt", new Date())
                                             .set(arrayField + ".videoStorage", "aws_s3");
                    } else {
                        // Main document update
                        update = new Update().set("aiVideoUrl", request.getAiVideoUrl())
                                             .set("updatedAt", new Date())
                                             .set("videoStorage", "aws_s3");
                    }

                    result = mongoTemplate.updateFirst(query, update, collectionName);
                    queryUsed = field;
                    
                    if (result.getMatchedCount() > 0) {
                        updated = true;
                        System.out.println("✅ Update successful with field: " + field);
                        System.out.println("📊 Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Strategy " + field + " failed: " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            if (updated) {
                response.put("status", "success");
                response.put("message", "AI video URL saved successfully");
                response.put("queryUsed", queryUsed);
                response.put("collection", collectionName);
                response.put("matched", result.getMatchedCount());
                response.put("modified", result.getModifiedCount());
                response.put("aiVideoUrl", request.getAiVideoUrl());
                response.put("subtopicId", request.getSubtopicId());
                response.put("timestamp", new Date().toString());
            } else {
                response.put("status", "not_found");
                response.put("message", "Subtopic not found in collection: " + collectionName);
                response.put("collection", collectionName);
                response.put("subtopicId", request.getSubtopicId());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Spring Boot Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update subtopic: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    // DEBUG endpoint to check if subtopic exists
    @GetMapping("/check-subtopic/{subtopicId}")
    public ResponseEntity<?> checkSubtopic(@PathVariable String subtopicId,
                                          @RequestParam String subjectName) {
        try {
            System.out.println("🔍 Checking subtopic: " + subtopicId + " in " + subjectName);
            
            Query query = new Query(new Criteria().orOperator(
                Criteria.where("_id").is(subtopicId),
                Criteria.where("units._id").is(subtopicId),
                Criteria.where("units.id").is(subtopicId),
                Criteria.where("children._id").is(subtopicId),
                Criteria.where("children.id").is(subtopicId),
                Criteria.where("subtopics._id").is(subtopicId),
                Criteria.where("subtopics.id").is(subtopicId)
            ));
            
            Map<String, Object> document = mongoTemplate.findOne(query, Map.class, subjectName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("found", document != null);
            response.put("subtopicId", subtopicId);
            response.put("collection", subjectName);
            
            if (document != null) {
                // Sanitize the document (remove large fields)
                Map<String, Object> sanitizedDoc = new HashMap<>();
                sanitizedDoc.put("_id", document.get("_id"));
                sanitizedDoc.put("name", document.get("name") != null ? document.get("name") : document.get("subtopic"));
                sanitizedDoc.put("hasUnits", document.containsKey("units"));
                sanitizedDoc.put("hasChildren", document.containsKey("children"));
                sanitizedDoc.put("hasSubtopics", document.containsKey("subtopics"));
                sanitizedDoc.put("aiVideoUrl", document.get("aiVideoUrl"));
                
                response.put("document", sanitizedDoc);
                System.out.println("✅ Found document: " + sanitizedDoc.get("name"));
            } else {
                System.out.println("❌ Document not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Check error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "found", false
            ));
        }
    }

    // Request DTO
    public static class UpdateSubtopicRequest {
        private String subtopicId;
        private String aiVideoUrl;
        private String dbname;
        private String subjectName;
        private String parentId;
        private String rootId;

        // Getters and setters
        public String getSubtopicId() { return subtopicId; }
        public void setSubtopicId(String subtopicId) { this.subtopicId = subtopicId; }

        public String getAiVideoUrl() { return aiVideoUrl; }
        public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

        public String getDbname() { return dbname; }
        public void setDbname(String dbname) { this.dbname = dbname; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getParentId() { return parentId; }
        public void setParentId(String parentId) { this.parentId = parentId; }

        public String getRootId() { return rootId; }
        public void setRootId(String rootId) { this.rootId = rootId; }

        @Override
        public String toString() {
            return "UpdateSubtopicRequest{" +
                    "subtopicId='" + subtopicId + '\'' +
                    ", aiVideoUrl='" + aiVideoUrl + '\'' +
                    ", dbname='" + dbname + '\'' +
                    ", subjectName='" + subjectName + '\'' +
                    '}';
        }
    }
}
