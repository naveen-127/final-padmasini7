package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SubtopicController {

    @Autowired
    private MongoTemplate mongoTemplate;

    // ✅ FIXED: Universal recursive update method for ALL nested structures
    @PutMapping("/updateSubtopicVideo")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> updateSubtopicVideo(@RequestBody UpdateSubtopicRequest request) {
        try {
            System.out.println("🔄 Spring Boot: Updating subtopic video (Universal)");
            System.out.println("📋 Request: " + request.toString());

            String collectionName = request.getSubjectName();
            if (collectionName == null || collectionName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "subjectName is required",
                    "message", "Please provide the collection/subject name"
                ));
            }

            // First try with ObjectId if valid
            if (ObjectId.isValid(request.getSubtopicId())) {
                ObjectId targetObjectId = new ObjectId(request.getSubtopicId());
                
                // Strategy 1: Try updating as a main document with ObjectId
                Query query1 = new Query(Criteria.where("_id").is(targetObjectId));
                Update update = new Update()
                    .set("aiVideoUrl", request.getAiVideoUrl())
                    .set("updatedAt", new Date())
                    .set("videoStorage", "aws_s3");
                
                UpdateResult result1 = mongoTemplate.updateFirst(query1, update, collectionName);
                if (result1.getModifiedCount() > 0) {
                    System.out.println("✅ Updated main document with ObjectId: " + request.getSubtopicId());
                    return ResponseEntity.ok(createSuccessResponse(
                        "Main document (ObjectId)", 
                        collectionName, 
                        request
                    ));
                }
            }

            // Strategy 2: Try updating with string _id as main document
            Query query2 = new Query(Criteria.where("_id").is(request.getSubtopicId()));
            Update update2 = new Update()
                .set("aiVideoUrl", request.getAiVideoUrl())
                .set("updatedAt", new Date())
                .set("videoStorage", "aws_s3");
            
            UpdateResult result2 = mongoTemplate.updateFirst(query2, update2, collectionName);
            if (result2.getModifiedCount() > 0) {
                System.out.println("✅ Updated main document with string _id: " + request.getSubtopicId());
                return ResponseEntity.ok(createSuccessResponse(
                    "Main document (string _id)", 
                    collectionName, 
                    request
                ));
            }

            // Strategy 3: Try universal recursive search and update
            boolean updated = updateSubtopicRecursiveUniversal(collectionName, request.getSubtopicId(), request.getAiVideoUrl());

            if (updated) {
                return ResponseEntity.ok(createSuccessResponse(
                    "Nested unit (recursive)", 
                    collectionName, 
                    request
                ));
            } else {
                // Strategy 4: Try direct query for nested units
                updated = updateDirectNestedUnit(collectionName, request.getSubtopicId(), request.getAiVideoUrl());
                
                if (updated) {
                    return ResponseEntity.ok(createSuccessResponse(
                        "Direct nested update", 
                        collectionName, 
                        request
                    ));
                }
            }

            // If nothing worked, return not found
            return ResponseEntity.ok(Map.of(
                "status", "not_found",
                "message", "Could not find subtopic with ID: " + request.getSubtopicId(),
                "collection", collectionName,
                "strategies_tried", List.of("ObjectId main doc", "String _id main doc", "Recursive search", "Direct nested update")
            ));

        } catch (Exception e) {
            System.err.println("❌ Spring Boot Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update subtopic: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    // ✅ NEW: Universal recursive update method for deeply nested structures
    @SuppressWarnings("unchecked")
    private boolean updateSubtopicRecursiveUniversal(String collectionName, String subtopicId, String aiVideoUrl) {
        try {
            System.out.println("🔍 Starting universal recursive search for: " + subtopicId);
            
            // Get all documents in the collection
            List<Map<String, Object>> allDocuments = new ArrayList<>();
            List<?> rawDocuments = mongoTemplate.findAll(Map.class, collectionName);
            for (Object doc : rawDocuments) {
                if (doc instanceof Map) {
                    allDocuments.add((Map<String, Object>) doc);
                }
            }
            
            System.out.println("📊 Searching in " + allDocuments.size() + " documents");
            
            for (Map<String, Object> document : allDocuments) {
                String documentId = document.get("_id").toString();
                
                // Try to update in this document's nested structures
                boolean updated = updateInDocumentRecursive(document, documentId, collectionName, subtopicId, aiVideoUrl);
                if (updated) {
                    System.out.println("✅ Found and updated in document: " + documentId);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Recursive search error: " + e.getMessage());
            return false;
        }
    }
    
    // ✅ NEW: Recursive update within a single document
    @SuppressWarnings("unchecked")
    private boolean updateInDocumentRecursive(Map<String, Object> document, String documentId, 
                                             String collectionName, String targetId, String aiVideoUrl) {
        // Check if this document itself matches
        String docId = null;
        if (document.get("_id") != null) docId = document.get("_id").toString();
        else if (document.get("id") != null) docId = document.get("id").toString();
        
        if (targetId.equals(docId) || targetId.equals(document.get("_id")) || targetId.equals(document.get("id"))) {
            // Update this main document
            Query query = new Query(Criteria.where("_id").is(documentId));
            Update update = new Update()
                .set("aiVideoUrl", aiVideoUrl)
                .set("updatedAt", new Date())
                .set("videoStorage", "aws_s3");
            
            UpdateResult result = mongoTemplate.updateFirst(query, update, collectionName);
            return result.getModifiedCount() > 0;
        }
        
        // Define all possible array field names that might contain subtopics
        List<String> arrayFields = new ArrayList<>();
        arrayFields.add("units");
        arrayFields.add("subtopics");
        arrayFields.add("children");
        arrayFields.add("topics"); // Add more if needed
        arrayFields.add("lessons");
        arrayFields.add("subunits");
        
        // Try each array field
        for (String field : arrayFields) {
            if (document.containsKey(field) && document.get(field) instanceof List) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) document.get(field);
                
                // Search through items at this level
                for (int i = 0; i < items.size(); i++) {
                    Map<String, Object> item = items.get(i);
                    
                    // Check if this item matches
                    String itemId = null;
                    if (item.get("_id") != null) itemId = item.get("_id").toString();
                    else if (item.get("id") != null) itemId = item.get("id").toString();
                    
                    if (targetId.equals(itemId) || targetId.equals(item.get("_id")) || targetId.equals(item.get("id"))) {
                        // Found it! Update using positional operator
                        Query query = Query.query(Criteria.where("_id").is(documentId)
                            .and(field + "._id").is(targetId));
                        
                        // Also try with string comparison for id field
                        if (query == null || mongoTemplate.count(query, collectionName) == 0) {
                            query = Query.query(Criteria.where("_id").is(documentId)
                                .and(field + ".id").is(targetId));
                        }
                        
                        Update update = new Update()
                            .set(field + ".$.aiVideoUrl", aiVideoUrl)
                            .set(field + ".$.updatedAt", new Date())
                            .set(field + ".$.videoStorage", "aws_s3");
                        
                        UpdateResult result = mongoTemplate.updateFirst(query, update, collectionName);
                        if (result.getModifiedCount() > 0) {
                            System.out.println("✅ Updated in " + field + " array at index " + i);
                            return true;
                        }
                    }
                    
                    // Recursively search deeper
                    if (updateInDocumentRecursive(item, documentId, collectionName, targetId, aiVideoUrl)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    // ✅ NEW: Direct update for nested units using aggregate query
    private boolean updateDirectNestedUnit(String collectionName, String subtopicId, String aiVideoUrl) {
        try {
            System.out.println("🎯 Trying direct nested unit update for: " + subtopicId);
            
            // Method 1: Using positional operator with _id field
            Query query1 = new Query(Criteria.where("units._id").is(subtopicId));
            Update update1 = new Update()
                .set("units.$.aiVideoUrl", aiVideoUrl)
                .set("units.$.updatedAt", new Date())
                .set("units.$.videoStorage", "aws_s3");
            
            UpdateResult result1 = mongoTemplate.updateFirst(query1, update1, collectionName);
            if (result1.getModifiedCount() > 0) {
                System.out.println("✅ Updated in units._id");
                return true;
            }
            
            // Method 2: Using positional operator with id field
            Query query2 = new Query(Criteria.where("units.id").is(subtopicId));
            Update update2 = new Update()
                .set("units.$.aiVideoUrl", aiVideoUrl)
                .set("units.$.updatedAt", new Date())
                .set("units.$.videoStorage", "aws_s3");
            
            UpdateResult result2 = mongoTemplate.updateFirst(query2, update2, collectionName);
            if (result2.getModifiedCount() > 0) {
                System.out.println("✅ Updated in units.id");
                return true;
            }
            
            // Method 3: Try other array fields
            String[] arrayFields = {"subtopics", "children", "topics", "lessons"};
            for (String field : arrayFields) {
                Query query = new Query(Criteria.where(field + "._id").is(subtopicId));
                Update update = new Update()
                    .set(field + ".$.aiVideoUrl", aiVideoUrl)
                    .set(field + ".$.updatedAt", new Date())
                    .set(field + ".$.videoStorage", "aws_s3");
                
                UpdateResult result = mongoTemplate.updateFirst(query, update, collectionName);
                if (result.getModifiedCount() > 0) {
                    System.out.println("✅ Updated in " + field + "._id");
                    return true;
                }
                
                // Try with id field
                Query queryId = new Query(Criteria.where(field + ".id").is(subtopicId));
                Update updateId = new Update()
                    .set(field + ".$.aiVideoUrl", aiVideoUrl)
                    .set(field + ".$.updatedAt", new Date())
                    .set(field + ".$.videoStorage", "aws_s3");
                
                UpdateResult resultId = mongoTemplate.updateFirst(queryId, updateId, collectionName);
                if (resultId.getModifiedCount() > 0) {
                    System.out.println("✅ Updated in " + field + ".id");
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Direct nested update error: " + e.getMessage());
            return false;
        }
    }
    
    // ✅ Helper method to create success response
    private Map<String, Object> createSuccessResponse(String location, String collectionName, UpdateSubtopicRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "AI video URL saved successfully");
        response.put("location", location);
        response.put("collection", collectionName);
        response.put("aiVideoUrl", request.getAiVideoUrl());
        response.put("subtopicId", request.getSubtopicId());
        response.put("timestamp", new Date().toString());
        response.put("updatedAt", new Date());
        return response;
    }

    // ✅ IMPROVED recursive update endpoint (for backward compatibility)
    @PutMapping("/updateSubtopicVideoRecursive")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> updateSubtopicVideoRecursive(@RequestBody UpdateSubtopicRequest request) {
        try {
            System.out.println("🔄 RECURSIVE UPDATE: Deep nested search");
            
            // Just call the main update method - it now handles all cases
            return updateSubtopicVideo(request);
            
        } catch (Exception e) {
            System.err.println("❌ Recursive update error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update nested subtopic: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    // ✅ NEW: Debug endpoint to check document structure
    @GetMapping("/debug-document/{documentId}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> debugDocument(@PathVariable String documentId,
                                          @RequestParam String subjectName) {
        try {
            System.out.println("🔍 Debugging document: " + documentId + " in " + subjectName);
            
            // Try ObjectId first
            Object doc = null;
            if (ObjectId.isValid(documentId)) {
                ObjectId objectId = new ObjectId(documentId);
                doc = mongoTemplate.findById(objectId, Map.class, subjectName);
            }
            
            // Try string _id if not found
            if (doc == null) {
                doc = mongoTemplate.findOne(
                    Query.query(Criteria.where("_id").is(documentId)), 
                    Map.class, 
                    subjectName
                );
            }
            
            if (doc == null) {
                return ResponseEntity.ok(Map.of(
                    "found", false,
                    "message", "Document not found",
                    "documentId", documentId
                ));
            }
            
            Map<String, Object> document = (Map<String, Object>) doc;
            
            // Create simplified view of document structure
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("_id", document.get("_id"));
            debugInfo.put("name", document.get("name") != null ? document.get("name") : 
                         document.get("unitName") != null ? document.get("unitName") : 
                         document.get("title"));
            
            // Check for nested arrays
            List<String> arrayFields = List.of("units", "subtopics", "children", "topics", "lessons");
            List<Map<String, Object>> arraysInfo = new ArrayList<>();
            
            for (String field : arrayFields) {
                if (document.containsKey(field) && document.get(field) instanceof List) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) document.get(field);
                    Map<String, Object> arrayInfo = new HashMap<>();
                    arrayInfo.put("field", field);
                    arrayInfo.put("count", items.size());
                    
                    // Get IDs of first few items
                    List<String> sampleIds = new ArrayList<>();
                    int limit = Math.min(items.size(), 3);
                    for (int i = 0; i < limit; i++) {
                        Map<String, Object> item = items.get(i);
                        if (item.get("_id") != null) {
                            sampleIds.add(item.get("_id").toString());
                        } else if (item.get("id") != null) {
                            sampleIds.add(item.get("id").toString());
                        }
                    }
                    arrayInfo.put("sampleIds", sampleIds);
                    
                    arraysInfo.add(arrayInfo);
                }
            }
            
            debugInfo.put("nestedArrays", arraysInfo);
            debugInfo.put("hasAiVideoUrl", document.containsKey("aiVideoUrl"));
            debugInfo.put("aiVideoUrl", document.get("aiVideoUrl"));
            
            return ResponseEntity.ok(Map.of(
                "found", true,
                "document", debugInfo
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Debug error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "found", false
            ));
        }
    }

    // ✅ Request DTO
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
