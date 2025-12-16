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
  // SIMPLIFIED Spring Boot controller for your structure
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

        // Try different update strategies
        boolean updated = false;
        String queryUsed = "";
        UpdateResult result = null;

        // Strategy 1: Update in units array with _id (string)
        try {
            Query query1 = new Query(Criteria.where("units._id").is(request.getSubtopicId()));
            Update update1 = new Update()
                .set("units.$.aiVideoUrl", request.getAiVideoUrl())
                .set("units.$.updatedAt", new Date())
                .set("units.$.videoStorage", "aws_s3");
            
            result = mongoTemplate.updateFirst(query1, update1, collectionName);
            queryUsed = "units._id (string)";
            
            if (result.getMatchedCount() > 0) {
                updated = true;
                System.out.println("✅ Strategy 1 succeeded: " + queryUsed);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Strategy 1 failed: " + e.getMessage());
        }

        // Strategy 2: Try with ObjectId
        if (!updated) {
            try {
                // Check if subtopicId is a valid ObjectId
                if (ObjectId.isValid(request.getSubtopicId())) {
                    ObjectId objectId = new ObjectId(request.getSubtopicId());
                    
                    Query query2 = new Query(Criteria.where("units._id").is(objectId));
                    Update update2 = new Update()
                        .set("units.$.aiVideoUrl", request.getAiVideoUrl())
                        .set("units.$.updatedAt", new Date())
                        .set("units.$.videoStorage", "aws_s3");
                    
                    result = mongoTemplate.updateFirst(query2, update2, collectionName);
                    queryUsed = "units._id (ObjectId)";
                    
                    if (result.getMatchedCount() > 0) {
                        updated = true;
                        System.out.println("✅ Strategy 2 succeeded: " + queryUsed);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Strategy 2 failed: " + e.getMessage());
            }
        }

        // Strategy 3: Try with id field
        if (!updated) {
            try {
                Query query3 = new Query(Criteria.where("units.id").is(request.getSubtopicId()));
                Update update3 = new Update()
                    .set("units.$.aiVideoUrl", request.getAiVideoUrl())
                    .set("units.$.updatedAt", new Date())
                    .set("units.$.videoStorage", "aws_s3");
                
                result = mongoTemplate.updateFirst(query3, update3, collectionName);
                queryUsed = "units.id";
                
                if (result.getMatchedCount() > 0) {
                    updated = true;
                    System.out.println("✅ Strategy 3 succeeded: " + queryUsed);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Strategy 3 failed: " + e.getMessage());
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
        } else {
            response.put("status", "not_found");
            response.put("message", "Could not find subtopic with ID: " + request.getSubtopicId());
            response.put("collection", collectionName);
            response.put("suggestion", "Check if the subtopicId exists in the 'units' array");
            
            // Debug: Check what's in the collection
            try {
                List<Object> sampleDocs = mongoTemplate.findAll(Object.class, collectionName);
                response.put("totalDocuments", sampleDocs.size());
                if (sampleDocs.size() > 0) {
                    Map<String, Object> firstDoc = (Map<String, Object>) sampleDocs.get(0);
                    response.put("sampleDocumentStructure", Map.of(
                        "hasUnits", firstDoc.containsKey("units"),
                        "unitsCount", firstDoc.containsKey("units") ? ((List<?>)firstDoc.get("units")).size() : 0
                    ));
                }
            } catch (Exception e) {
                response.put("debugError", e.getMessage());
            }
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
