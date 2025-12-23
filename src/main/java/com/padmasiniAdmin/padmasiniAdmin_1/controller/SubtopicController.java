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

    // ✅ IMPROVED: Recursive helper method for finding and updating nested subtopics
    @SuppressWarnings("unchecked")
    private boolean updateNestedSubtopicRecursive(List<Map<String, Object>> items, 
                                                 String targetId, 
                                                 String aiVideoUrl,
                                                 String collectionName,
                                                 String parentDocumentId,
                                                 String path,
                                                 int depth) {
        if (depth > 10) { // Prevent infinite recursion
            System.out.println("⚠️ Max recursion depth reached");
            return false;
        }
        
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            
            // Check if this is the target subtopic
            String itemId = null;
            if (item.get("_id") != null) {
                itemId = item.get("_id").toString();
            } else if (item.get("id") != null) {
                itemId = item.get("id").toString();
            }
            
            boolean isMatch = (targetId.equals(itemId) || 
                             targetId.equals(item.get("_id")) || 
                             targetId.equals(item.get("id")));
            
            if (isMatch) {
                // Found it! Update this subtopic
                System.out.println("✅ Found target at path: " + path + "[" + i + "]");
                
                // Build the query using the parent document ID
                Query query = new Query(Criteria.where("_id").is(parentDocumentId));
                
                // Build the update path correctly
                String updatePath = buildUpdatePath(path, i);
                
                // Build update
                Update update = new Update()
                    .set(updatePath + ".aiVideoUrl", aiVideoUrl)
                    .set(updatePath + ".updatedAt", new Date())
                    .set(updatePath + ".videoStorage", "aws_s3");
                
                System.out.println("📝 Update path: " + updatePath);
                
                UpdateResult result = mongoTemplate.updateFirst(query, update, collectionName);
                
                System.out.println("📊 Update result: " + result.getMatchedCount() + " matched, " + 
                                  result.getModifiedCount() + " modified");
                
                return result.getMatchedCount() > 0;
            }
            
            // Recursively search in nested arrays
            List<String> arrayFields = new ArrayList<>();
            arrayFields.add("units");
            arrayFields.add("subtopics");
            arrayFields.add("children");
            
            for (String field : arrayFields) {
                if (item.containsKey(field) && item.get(field) instanceof List) {
                    List<Map<String, Object>> nestedItems = (List<Map<String, Object>>) item.get(field);
                    String newPath = path.isEmpty() ? field : path + "." + field;
                    boolean found = updateNestedSubtopicRecursive(
                        nestedItems,
                        targetId, 
                        aiVideoUrl, 
                        collectionName, 
                        parentDocumentId,
                        newPath,
                        depth + 1
                    );
                    if (found) return true;
                }
            }
        }
        return false;
    }
    
    // ✅ NEW: Helper method to build correct update path
    private String buildUpdatePath(String path, int index) {
        if (path.isEmpty()) return "";
        
        String[] parts = path.split("\\.");
        StringBuilder updatePath = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) updatePath.append(".");
            if (i == parts.length - 1) {
                // Last part gets the positional operator $
                updatePath.append(parts[i]).append(".$");
            } else {
                updatePath.append(parts[i]);
            }
        }
        
        // Add the index for the specific item
        updatePath.append("[").append(index).append("]");
        return updatePath.toString();
    }

    // ✅ SIMPLIFIED: Direct update endpoint
    @PutMapping("/updateSubtopicVideo")
    @SuppressWarnings("unchecked")
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

            // Try to find and update the subtopic
            boolean updated = updateSubtopicInCollection(collectionName, request.getSubtopicId(), request.getAiVideoUrl());

            Map<String, Object> response = new HashMap<>();
            
            if (updated) {
                response.put("status", "success");
                response.put("message", "AI video URL saved successfully");
                response.put("collection", collectionName);
                response.put("aiVideoUrl", request.getAiVideoUrl());
                response.put("subtopicId", request.getSubtopicId());
                response.put("timestamp", new Date().toString());
            } else {
                response.put("status", "not_found");
                response.put("message", "Could not find subtopic with ID: " + request.getSubtopicId());
                response.put("collection", collectionName);
                response.put("suggestion", "Try the recursive endpoint: /updateSubtopicVideoRecursive");
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
    
    // ✅ NEW: Helper method for finding and updating subtopic
    @SuppressWarnings("unchecked")
    private boolean updateSubtopicInCollection(String collectionName, String subtopicId, String aiVideoUrl) {
        // Get all documents in the collection
        List<Map<String, Object>> allDocuments = new ArrayList<>();
        List<?> rawDocuments = mongoTemplate.findAll(Map.class, collectionName);
        for (Object doc : rawDocuments) {
            if (doc instanceof Map) {
                allDocuments.add((Map<String, Object>) doc);
            }
        }
        
        System.out.println("🔍 Searching in " + allDocuments.size() + " documents for subtopic: " + subtopicId);
        
        for (Map<String, Object> document : allDocuments) {
            String documentId = document.get("_id").toString();
            
            // Check if this document itself is the target
            if (subtopicId.equals(documentId) || subtopicId.equals(document.get("_id"))) {
                Query query = new Query(Criteria.where("_id").is(documentId));
                Update update = new Update()
                    .set("aiVideoUrl", aiVideoUrl)
                    .set("updatedAt", new Date())
                    .set("videoStorage", "aws_s3");
                
                UpdateResult result = mongoTemplate.updateFirst(query, update, collectionName);
                if (result.getMatchedCount() > 0) {
                    System.out.println("✅ Updated main document: " + documentId);
                    return true;
                }
            }
            
            // Check in units array
            if (updateInArray(document, "units", subtopicId, documentId, aiVideoUrl, collectionName)) {
                return true;
            }
            
            // Check in subtopics array
            if (updateInArray(document, "subtopics", subtopicId, documentId, aiVideoUrl, collectionName)) {
                return true;
            }
            
            // Check in children array
            if (updateInArray(document, "children", subtopicId, documentId, aiVideoUrl, collectionName)) {
                return true;
            }
        }
        
        return false;
    }
    
    // ✅ NEW: Helper method for updating in array
    @SuppressWarnings("unchecked")
    private boolean updateInArray(Map<String, Object> document, String arrayField, 
                                 String subtopicId, String documentId, 
                                 String aiVideoUrl, String collectionName) {
        if (document.containsKey(arrayField) && document.get(arrayField) instanceof List) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) document.get(arrayField);
            
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                String itemId = null;
                if (item.get("_id") != null) {
                    itemId = item.get("_id").toString();
                } else if (item.get("id") != null) {
                    itemId = item.get("id").toString();
                }
                
                if (subtopicId.equals(itemId) || subtopicId.equals(item.get("_id")) || subtopicId.equals(item.get("id"))) {
                    // Found it in this array, update it
                    Query query = new Query(Criteria.where("_id").is(documentId));
                    Update update = new Update()
                        .set(arrayField + ".$.aiVideoUrl", aiVideoUrl)
                        .set(arrayField + ".$.updatedAt", new Date())
                        .set(arrayField + ".$.videoStorage", "aws_s3");
                    
                    UpdateResult result = mongoTemplate.updateFirst(query, update, collectionName);
                    if (result.getMatchedCount() > 0) {
                        System.out.println("✅ Updated in " + arrayField + " array at index " + i + " in document: " + documentId);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ✅ NEW: Improved recursive update endpoint for deeply nested subtopics
    @PutMapping("/updateSubtopicVideoRecursive")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> updateSubtopicVideoRecursive(@RequestBody UpdateSubtopicRequest request) {
        try {
            System.out.println("🔄 RECURSIVE UPDATE: Starting deep nested search");
            System.out.println("📋 Request: " + request.toString());

            String collectionName = request.getSubjectName();
            if (collectionName == null || collectionName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "subjectName is required",
                    "message", "Please provide the collection/subject name"
                ));
            }

            // Get ALL documents in the collection
            List<Map<String, Object>> allDocuments = new ArrayList<>();
            List<?> rawDocuments = mongoTemplate.findAll(Map.class, collectionName);
            for (Object doc : rawDocuments) {
                if (doc instanceof Map) {
                    allDocuments.add((Map<String, Object>) doc);
                }
            }
            System.out.println("📊 Found " + allDocuments.size() + " documents in collection");

            boolean updated = false;
            String foundPath = null;
            String foundInDocumentId = null;

            // Search through all documents
            for (Map<String, Object> document : allDocuments) {
                String documentId = document.get("_id").toString();
                
                System.out.println("🔍 Searching in document: " + documentId);
                
                // Try recursive search starting from various array fields
                List<String> rootFields = new ArrayList<>();
                rootFields.add("units");
                rootFields.add("subtopics");
                rootFields.add("children");
                
                // Also check main document
                String itemId = null;
                if (document.get("_id") != null) itemId = document.get("_id").toString();
                else if (document.get("id") != null) itemId = document.get("id").toString();
                
                if (request.getSubtopicId().equals(itemId)) {
                    // Update main document
                    Query query = new Query(Criteria.where("_id").is(documentId));
                    Update update = new Update()
                        .set("aiVideoUrl", request.getAiVideoUrl())
                        .set("updatedAt", new Date())
                        .set("videoStorage", "aws_s3");
                    
                    UpdateResult result = mongoTemplate.updateFirst(query, update, collectionName);
                    if (result.getMatchedCount() > 0) {
                        updated = true;
                        foundPath = "main_document";
                        foundInDocumentId = documentId;
                        System.out.println("✅ Updated main document: " + documentId);
                        break;
                    }
                }
                
                for (String rootField : rootFields) {
                    if (document.containsKey(rootField) && document.get(rootField) instanceof List) {
                        List<Map<String, Object>> rootItems = (List<Map<String, Object>>) document.get(rootField);
                        
                        System.out.println("🔍 Searching in " + rootField + " array with " + rootItems.size() + " items");
                        
                        // Perform recursive search
                        boolean found = updateNestedSubtopicRecursive(
                            rootItems,
                            request.getSubtopicId(),
                            request.getAiVideoUrl(),
                            collectionName,
                            documentId,
                            rootField,
                            0
                        );
                        
                        if (found) {
                            updated = true;
                            foundPath = rootField + " → nested";
                            foundInDocumentId = documentId;
                            System.out.println("✅ Found in " + rootField + " array of document: " + documentId);
                            break;
                        }
                    }
                }
                
                if (updated) break;
            }

            Map<String, Object> response = new HashMap<>();
            
            if (updated) {
                response.put("status", "success");
                response.put("message", "AI video URL saved successfully in nested structure");
                response.put("foundIn", foundPath);
                response.put("documentId", foundInDocumentId);
                response.put("collection", collectionName);
                response.put("aiVideoUrl", request.getAiVideoUrl());
                response.put("subtopicId", request.getSubtopicId());
                response.put("timestamp", new Date().toString());
            } else {
                response.put("status", "not_found");
                response.put("message", "Could not find subtopic with ID: " + request.getSubtopicId() + " in any nested structure");
                response.put("collection", collectionName);
                response.put("searchedDocuments", allDocuments.size());
                response.put("suggestion", "Check if the subtopicId exists in units/subtopics/children arrays");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Recursive update error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update nested subtopic: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    // ✅ NEW: Enhanced debug endpoint with recursive search
    @GetMapping("/check-subtopic/{subtopicId}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> checkSubtopic(@PathVariable String subtopicId,
                                          @RequestParam String subjectName) {
        try {
            System.out.println("🔍 Checking subtopic: " + subtopicId + " in " + subjectName);
            
            // Get all documents
            List<Map<String, Object>> allDocuments = new ArrayList<>();
            List<?> rawDocuments = mongoTemplate.findAll(Map.class, subjectName);
            for (Object doc : rawDocuments) {
                if (doc instanceof Map) {
                    allDocuments.add((Map<String, Object>) doc);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("found", false);
            response.put("subtopicId", subtopicId);
            response.put("collection", subjectName);
            response.put("totalDocuments", allDocuments.size());
            
            // Search recursively
            for (Map<String, Object> document : allDocuments) {
                String documentId = document.get("_id").toString();
                
                // Check if found in this document
                Map<String, Object> foundInfo = findSubtopicRecursively(document, subtopicId, "", documentId);
                if (foundInfo.get("found").equals(true)) {
                    response.put("found", true);
                    response.put("location", foundInfo.get("path"));
                    response.put("documentId", documentId);
                    response.put("document", createSanitizedDoc(document));
                    break;
                }
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
    
    // ✅ NEW: Recursive search helper
    @SuppressWarnings("unchecked")
    private Map<String, Object> findSubtopicRecursively(Map<String, Object> item, String targetId, String path, String documentId) {
        Map<String, Object> result = new HashMap<>();
        result.put("found", false);
        
        // Check current item
        String itemId = null;
        if (item.get("_id") != null) itemId = item.get("_id").toString();
        else if (item.get("id") != null) itemId = item.get("id").toString();
        
        if (targetId.equals(itemId) || targetId.equals(item.get("_id")) || targetId.equals(item.get("id"))) {
            result.put("found", true);
            result.put("path", path.isEmpty() ? "main_document" : path);
            return result;
        }
        
        // Search in nested arrays
        List<String> arrayFields = new ArrayList<>();
        arrayFields.add("units");
        arrayFields.add("subtopics");
        arrayFields.add("children");
        
        for (String field : arrayFields) {
            if (item.containsKey(field) && item.get(field) instanceof List) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) item.get(field);
                for (int i = 0; i < items.size(); i++) {
                    Map<String, Object> nestedItem = items.get(i);
                    String newPath = path.isEmpty() ? field + "[" + i + "]" : path + "." + field + "[" + i + "]";
                    Map<String, Object> nestedResult = findSubtopicRecursively(nestedItem, targetId, newPath, documentId);
                    if (nestedResult.get("found").equals(true)) {
                        return nestedResult;
                    }
                }
            }
        }
        
        return result;
    }
    
    // ✅ NEW: Create sanitized document
    @SuppressWarnings("unchecked")
    private Map<String, Object> createSanitizedDoc(Map<String, Object> document) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("_id", document.get("_id"));
        sanitized.put("name", document.get("name") != null ? document.get("name") : 
                       document.get("unitName") != null ? document.get("unitName") : 
                       document.get("title"));
        
        // Check for arrays
        List<String> arrayFields = new ArrayList<>();
        arrayFields.add("units");
        arrayFields.add("subtopics");
        arrayFields.add("children");
        
        for (String field : arrayFields) {
            if (document.containsKey(field) && document.get(field) instanceof List) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) document.get(field);
                sanitized.put(field + "_count", items.size());
                
                // Sample first 2 items
                List<Map<String, Object>> samples = new ArrayList<>();
                int count = Math.min(items.size(), 2);
                for (int i = 0; i < count; i++) {
                    Map<String, Object> item = items.get(i);
                    Map<String, Object> sample = new HashMap<>();
                    sample.put("_id", item.get("_id"));
                    sample.put("id", item.get("id"));
                    sample.put("name", item.get("name") != null ? item.get("name") : 
                              item.get("unitName") != null ? item.get("unitName") : 
                              item.get("title"));
                    samples.add(sample);
                }
                sanitized.put(field + "_samples", samples);
            }
        }
        
        return sanitized;
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
