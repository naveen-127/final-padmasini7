package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;  // ✅ ADD THIS IMPORT

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;  // ✅ ADD THIS IMPORT

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SubtopicController {

    @Autowired
    private MongoTemplate mongoTemplate;

    // SIMPLIFIED: Direct update endpoint for your units array structure
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

            // Strategy 4: Try direct document update (if it's a main document, not in units array)
            if (!updated) {
                try {
                    Query query4 = new Query(Criteria.where("_id").is(request.getSubtopicId()));
                    Update update4 = new Update()
                        .set("aiVideoUrl", request.getAiVideoUrl())
                        .set("updatedAt", new Date())
                        .set("videoStorage", "aws_s3");
                    
                    result = mongoTemplate.updateFirst(query4, update4, collectionName);
                    queryUsed = "main_document";
                    
                    if (result.getMatchedCount() > 0) {
                        updated = true;
                        System.out.println("✅ Strategy 4 succeeded: " + queryUsed);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Strategy 4 failed: " + e.getMessage());
                }
            }

            // Strategy 5: Try direct document update with ObjectId
            if (!updated) {
                try {
                    if (ObjectId.isValid(request.getSubtopicId())) {
                        ObjectId objectId = new ObjectId(request.getSubtopicId());
                        
                        Query query5 = new Query(Criteria.where("_id").is(objectId));
                        Update update5 = new Update()
                            .set("aiVideoUrl", request.getAiVideoUrl())
                            .set("updatedAt", new Date())
                            .set("videoStorage", "aws_s3");
                        
                        result = mongoTemplate.updateFirst(query5, update5, collectionName);
                        queryUsed = "main_document_objectid";
                        
                        if (result.getMatchedCount() > 0) {
                            updated = true;
                            System.out.println("✅ Strategy 5 succeeded: " + queryUsed);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Strategy 5 failed: " + e.getMessage());
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
                response.put("message", "Could not find subtopic with ID: " + request.getSubtopicId());
                response.put("collection", collectionName);
                response.put("suggestion", "Check if the subtopicId exists in the 'units' array");
                
                // Debug: Check what's in the collection
                try {
                    List<Object> sampleDocs = mongoTemplate.findAll(Object.class, collectionName);
                    response.put("totalDocuments", sampleDocs.size());
                    
                    // Show structure of first few documents
                    if (sampleDocs.size() > 0) {
                        Map<String, Object> sampleResponse = new HashMap<>();
                        
                        // Take first 3 documents max
                        int count = Math.min(sampleDocs.size(), 3);
                        for (int i = 0; i < count; i++) {
                            Map<String, Object> doc = (Map<String, Object>) sampleDocs.get(i);
                            Map<String, Object> docInfo = new HashMap<>();
                            
                            docInfo.put("_id", doc.get("_id"));
                            docInfo.put("unitName", doc.get("unitName"));
                            docInfo.put("hasUnits", doc.containsKey("units"));
                            
                            if (doc.containsKey("units") && doc.get("units") instanceof List) {
                                List<?> units = (List<?>) doc.get("units");
                                docInfo.put("unitsCount", units.size());
                                
                                // Show first few units
                                if (units.size() > 0) {
                                    List<Map<String, Object>> unitSamples = new java.util.ArrayList<>();
                                    int unitCount = Math.min(units.size(), 2);
                                    for (int j = 0; j < unitCount; j++) {
                                        Map<String, Object> unit = (Map<String, Object>) units.get(j);
                                        Map<String, Object> unitInfo = new HashMap<>();
                                        unitInfo.put("_id", unit.get("_id"));
                                        unitInfo.put("unitName", unit.get("unitName"));
                                        unitInfo.put("id", unit.get("id"));
                                        unitSamples.add(unitInfo);
                                    }
                                    docInfo.put("sampleUnits", unitSamples);
                                }
                            }
                            
                            sampleResponse.put("document_" + i, docInfo);
                        }
                        
                        response.put("sampleDocuments", sampleResponse);
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

    // Debug endpoint to check if subtopic exists
    @GetMapping("/check-subtopic/{subtopicId}")
    public ResponseEntity<?> checkSubtopic(@PathVariable String subtopicId,
                                          @RequestParam String subjectName) {
        try {
            System.out.println("🔍 Checking subtopic: " + subtopicId + " in " + subjectName);
            
            Query query = new Query(new Criteria().orOperator(
                Criteria.where("_id").is(subtopicId),
                Criteria.where("units._id").is(subtopicId),
                Criteria.where("units.id").is(subtopicId)
            ));
            
            Map<String, Object> document = mongoTemplate.findOne(query, Map.class, subjectName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("found", document != null);
            response.put("subtopicId", subtopicId);
            response.put("collection", subjectName);
            
            if (document != null) {
                // Create a sanitized response
                Map<String, Object> sanitizedDoc = new HashMap<>();
                sanitizedDoc.put("_id", document.get("_id"));
                sanitizedDoc.put("unitName", document.get("unitName") != null ? document.get("unitName") : document.get("name"));
                sanitizedDoc.put("hasUnits", document.containsKey("units"));
                
                if (document.containsKey("units") && document.get("units") instanceof List) {
                    List<?> units = (List<?>) document.get("units");
                    sanitizedDoc.put("unitsCount", units.size());
                    
                    // Find the specific unit
                    for (Object unitObj : units) {
                        Map<String, Object> unit = (Map<String, Object>) unitObj;
                        if (subtopicId.equals(unit.get("_id")) || subtopicId.equals(unit.get("id"))) {
                            sanitizedDoc.put("foundUnit", Map.of(
                                "unitName", unit.get("unitName"),
                                "_id", unit.get("_id"),
                                "id", unit.get("id"),
                                "aiVideoUrl", unit.get("aiVideoUrl")
                            ));
                            break;
                        }
                    }
                }
                
                response.put("document", sanitizedDoc);
                System.out.println("✅ Found document");
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
