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

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SubtopicController {

    @Autowired
    private MongoTemplate mongoTemplate;

    // ✅ UPDATED: Now handles both aiVideoUrl and customDescription
    @PutMapping("/updateSubtopicVideo")
    public ResponseEntity<?> updateSubtopicVideo(@RequestBody UpdateSubtopicRequest request) {
        try {
            System.out.println("🔄 Spring Boot: Updating subtopic video and description");
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

            // ✅ Strategy 1: Update in units array with _id (string) - WITH customDescription
            try {
                Query query1 = new Query(Criteria.where("units._id").is(request.getSubtopicId()));
                Update update1 = new Update()
                    .set("units.$.aiVideoUrl", request.getAiVideoUrl())
                    .set("units.$.updatedAt", new Date())
                    .set("units.$.videoStorage", "aws_s3");
                
                // ✅ ADD THIS: Update customDescription if provided
                if (request.getCustomDescription() != null && !request.getCustomDescription().isEmpty()) {
                    update1.set("units.$.customDescription", request.getCustomDescription());
                    update1.set("units.$.updatedDescriptionAt", new Date());
                    System.out.println("✅ Also updating customDescription");
                }
                
                result = mongoTemplate.updateFirst(query1, update1, collectionName);
                queryUsed = "units._id (string)";
                
                if (result.getMatchedCount() > 0) {
                    updated = true;
                    System.out.println("✅ Strategy 1 succeeded: " + queryUsed);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Strategy 1 failed: " + e.getMessage());
            }

            // ✅ Strategy 2: Try with ObjectId - WITH customDescription
            if (!updated) {
                try {
                    if (ObjectId.isValid(request.getSubtopicId())) {
                        ObjectId objectId = new ObjectId(request.getSubtopicId());
                        
                        Query query2 = new Query(Criteria.where("units._id").is(objectId));
                        Update update2 = new Update()
                            .set("units.$.aiVideoUrl", request.getAiVideoUrl())
                            .set("units.$.updatedAt", new Date())
                            .set("units.$.videoStorage", "aws_s3");
                        
                        // ✅ ADD THIS: Update customDescription if provided
                        if (request.getCustomDescription() != null && !request.getCustomDescription().isEmpty()) {
                            update2.set("units.$.customDescription", request.getCustomDescription());
                            update2.set("units.$.updatedDescriptionAt", new Date());
                        }
                        
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

            // ✅ Strategy 3: Try with id field - WITH customDescription
            if (!updated) {
                try {
                    Query query3 = new Query(Criteria.where("units.id").is(request.getSubtopicId()));
                    Update update3 = new Update()
                        .set("units.$.aiVideoUrl", request.getAiVideoUrl())
                        .set("units.$.updatedAt", new Date())
                        .set("units.$.videoStorage", "aws_s3");
                    
                    // ✅ ADD THIS: Update customDescription if provided
                    if (request.getCustomDescription() != null && !request.getCustomDescription().isEmpty()) {
                        update3.set("units.$.customDescription", request.getCustomDescription());
                        update3.set("units.$.updatedDescriptionAt", new Date());
                    }
                    
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

            // ✅ Strategy 4: Try direct document update - WITH customDescription
            if (!updated) {
                try {
                    Query query4 = new Query(Criteria.where("_id").is(request.getSubtopicId()));
                    Update update4 = new Update()
                        .set("aiVideoUrl", request.getAiVideoUrl())
                        .set("updatedAt", new Date())
                        .set("videoStorage", "aws_s3");
                    
                    // ✅ ADD THIS: Update customDescription if provided
                    if (request.getCustomDescription() != null && !request.getCustomDescription().isEmpty()) {
                        update4.set("customDescription", request.getCustomDescription());
                        update4.set("updatedDescriptionAt", new Date());
                    }
                    
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

            // ✅ Strategy 5: Try direct document update with ObjectId - WITH customDescription
            if (!updated) {
                try {
                    if (ObjectId.isValid(request.getSubtopicId())) {
                        ObjectId objectId = new ObjectId(request.getSubtopicId());
                        
                        Query query5 = new Query(Criteria.where("_id").is(objectId));
                        Update update5 = new Update()
                            .set("aiVideoUrl", request.getAiVideoUrl())
                            .set("updatedAt", new Date())
                            .set("videoStorage", "aws_s3");
                        
                        // ✅ ADD THIS: Update customDescription if provided
                        if (request.getCustomDescription() != null && !request.getCustomDescription().isEmpty()) {
                            update5.set("customDescription", request.getCustomDescription());
                            update5.set("updatedDescriptionAt", new Date());
                        }
                        
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
                response.put("message", "AI video URL and description saved successfully");
                response.put("queryUsed", queryUsed);
                response.put("collection", collectionName);
                response.put("matched", result.getMatchedCount());
                response.put("modified", result.getModifiedCount());
                response.put("aiVideoUrl", request.getAiVideoUrl());
                response.put("subtopicId", request.getSubtopicId());
                response.put("customDescriptionUpdated", request.getCustomDescription() != null && !request.getCustomDescription().isEmpty());
                response.put("timestamp", new Date().toString());
            } else {
                response.put("status", "not_found");
                response.put("message", "Could not find subtopic with ID: " + request.getSubtopicId());
                response.put("collection", collectionName);
                response.put("suggestion", "Check if the subtopicId exists in the 'units' array");
                
                // Debug information
                try {
                    List<Object> sampleDocs = mongoTemplate.findAll(Object.class, collectionName);
                    response.put("totalDocuments", sampleDocs.size());
                    
                    if (sampleDocs.size() > 0) {
                        Map<String, Object> sampleResponse = new HashMap<>();
                        
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
                                
                                if (units.size() > 0) {
                                    List<Map<String, Object>> unitSamples = new java.util.ArrayList<>();
                                    int unitCount = Math.min(units.size(), 2);
                                    for (int j = 0; j < unitCount; j++) {
                                        Map<String, Object> unit = (Map<String, Object>) units.get(j);
                                        Map<String, Object> unitInfo = new HashMap<>();
                                        unitInfo.put("_id", unit.get("_id"));
                                        unitInfo.put("unitName", unit.get("unitName"));
                                        unitInfo.put("id", unit.get("id"));
                                        unitInfo.put("hasCustomDescription", unit.containsKey("customDescription"));
                                        unitInfo.put("hasDescription", unit.containsKey("description"));
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

    // ✅ NEW: Separate endpoint to update ONLY custom description
    @PutMapping("/updateSubtopicDescription")
    public ResponseEntity<?> updateSubtopicDescription(@RequestBody UpdateDescriptionRequest request) {
        try {
            System.out.println("📝 Spring Boot: Updating subtopic description only");
            System.out.println("📋 Request: " + request.toString());

            String collectionName = request.getSubjectName();
            if (collectionName == null || collectionName.trim().isEmpty()) { 
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "subjectName is required",
                    "message", "Please provide the collection/subject name"
                ));
            }

            boolean updated = false;
            String queryUsed = "";
            UpdateResult result = null;

            // Try to find and update in units array
            try {
                Query query = new Query(Criteria.where("units._id").is(request.getSubtopicId()));
                Update update = new Update()
                    .set("units.$.customDescription", request.getCustomDescription())
                    .set("units.$.description", request.getCustomDescription()) // Also update description field
                    .set("units.$.updatedDescriptionAt", new Date());
                
                result = mongoTemplate.updateFirst(query, update, collectionName);
                queryUsed = "units._id (string)";
                
                if (result.getMatchedCount() > 0) {
                    updated = true;
                    System.out.println("✅ Description update succeeded");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Description update failed: " + e.getMessage());
            }

            // Try with ObjectId
            if (!updated && ObjectId.isValid(request.getSubtopicId())) {
                try {
                    ObjectId objectId = new ObjectId(request.getSubtopicId());
                    
                    Query query = new Query(Criteria.where("units._id").is(objectId));
                    Update update = new Update()
                        .set("units.$.customDescription", request.getCustomDescription())
                        .set("units.$.description", request.getCustomDescription())
                        .set("units.$.updatedDescriptionAt", new Date());
                    
                    result = mongoTemplate.updateFirst(query, update, collectionName);
                    queryUsed = "units._id (ObjectId)";
                    
                    if (result.getMatchedCount() > 0) {
                        updated = true;
                        System.out.println("✅ Description update succeeded with ObjectId");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ ObjectId update failed: " + e.getMessage());
                }
            }

            // Try main document
            if (!updated) {
                try {
                    Query query = new Query(Criteria.where("_id").is(request.getSubtopicId()));
                    Update update = new Update()
                        .set("customDescription", request.getCustomDescription())
                        .set("description", request.getCustomDescription())
                        .set("updatedDescriptionAt", new Date());
                    
                    result = mongoTemplate.updateFirst(query, update, collectionName);
                    queryUsed = "main_document";
                    
                    if (result.getMatchedCount() > 0) {
                        updated = true;
                        System.out.println("✅ Description update succeeded in main document");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Main document update failed: " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            
            if (updated) {
                response.put("status", "success");
                response.put("message", "Description updated successfully");
                response.put("queryUsed", queryUsed);
                response.put("collection", collectionName);
                response.put("matched", result.getMatchedCount());
                response.put("modified", result.getModifiedCount());
                response.put("customDescription", request.getCustomDescription());
                response.put("timestamp", new Date().toString());
            } else {
                response.put("status", "not_found");
                response.put("message", "Could not find subtopic with ID: " + request.getSubtopicId());
                response.put("collection", collectionName);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Description update error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update description: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    // ✅ NEW: Get subtopic details (including customDescription)
    @GetMapping("/get-subtopic/{subtopicId}")
    public ResponseEntity<?> getSubtopicDetails(@PathVariable String subtopicId,
                                               @RequestParam String dbname,
                                               @RequestParam String subjectName) {
        try {
            System.out.println("🔍 Getting subtopic details for: " + subtopicId);
            System.out.println("📁 DB: " + dbname + ", Collection: " + subjectName);

            // First try: Search in units array
            Query query1 = new Query(Criteria.where("units._id").is(subtopicId));
            Map<String, Object> parentDoc = mongoTemplate.findOne(query1, Map.class, subjectName);
            
            if (parentDoc != null && parentDoc.containsKey("units")) {
                List<?> units = (List<?>) parentDoc.get("units");
                
                for (Object unitObj : units) {
                    Map<String, Object> unit = (Map<String, Object>) unitObj;
                    if (subtopicId.equals(unit.get("_id")) || subtopicId.equals(unit.get("id"))) {
                        System.out.println("✅ Found in units array");
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Subtopic found in nested units");
                        response.put("document", Map.of(
                        ));
                        response.put("parentDocument", Map.of(
                            "_id", parentDoc.get("_id"),
                            "unitName", parentDoc.get("unitName")
                        ));
                        
                        return ResponseEntity.ok(response);
                    }
                }
            }

            // Second try: Search as main document
            Query query2 = new Query();
            if (ObjectId.isValid(subtopicId)) {
                query2.addCriteria(Criteria.where("_id").is(new ObjectId(subtopicId)));
            } else {
                query2.addCriteria(Criteria.where("_id").is(subtopicId));
            }
            
            Map<String, Object> mainDoc = mongoTemplate.findOne(query2, Map.class, subjectName);
            
            if (mainDoc != null) {
                System.out.println("✅ Found as main document");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Subtopic found as main document");
                response.put("document", Map.of(
                ));
                
                return ResponseEntity.ok(response);
            }

            // Not found
            System.out.println("❌ Subtopic not found");
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Subtopic not found in database",
                "subtopicId", subtopicId,
                "collection", subjectName
            ));

        } catch (Exception e) {
            System.err.println("❌ Get subtopic error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to get subtopic: " + e.getMessage()
            ));
        }
    }

    // ✅ UPDATED Request DTO with customDescription field
    public static class UpdateSubtopicRequest {
        private String subtopicId;
        private String aiVideoUrl;
        private String dbname;
        private String subjectName;
        private String parentId;
        private String rootId;
        private String customDescription; // ✅ NEW FIELD
        
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

        public String getCustomDescription() { return customDescription; } // ✅ NEW
        public void setCustomDescription(String customDescription) { this.customDescription = customDescription; } // ✅ NEW

        @Override
        public String toString() {
            return "UpdateSubtopicRequest{" +
                    "subtopicId='" + subtopicId + '\'' +
                    ", aiVideoUrl='" + aiVideoUrl + '\'' +
                    ", dbname='" + dbname + '\'' +
                    ", subjectName='" + subjectName + '\'' +
                    ", customDescription='" + (customDescription != null ? "present" : "null") + '\'' +
                    '}';
        }
    }

    // ✅ NEW Request DTO for description-only updates
    public static class UpdateDescriptionRequest {
        private String subtopicId;
        private String customDescription;
        private String dbname;
        private String subjectName;
        
        // Getters and setters
        public String getSubtopicId() { return subtopicId; }
        public void setSubtopicId(String subtopicId) { this.subtopicId = subtopicId; }

        public String getCustomDescription() { return customDescription; }
        public void setCustomDescription(String customDescription) { this.customDescription = customDescription; }

        public String getDbname() { return dbname; }
        public void setDbname(String dbname) { this.dbname = dbname; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        @Override
        public String toString() {
            return "UpdateDescriptionRequest{" +
                    "subtopicId='" + subtopicId + '\'' +
                    ", customDescription='" + customDescription + '\'' +
                    ", dbname='" + dbname + '\'' +
                    ", subjectName='" + subjectName + '\'' +
                    '}';
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
                Map<String, Object> sanitizedDoc = new HashMap<>();
                sanitizedDoc.put("_id", document.get("_id"));
                sanitizedDoc.put("unitName", document.get("unitName") != null ? document.get("unitName") : document.get("name"));
                sanitizedDoc.put("hasUnits", document.containsKey("units"));
                
                if (document.containsKey("units") && document.get("units") instanceof List) {
                    List<?> units = (List<?>) document.get("units");
                    sanitizedDoc.put("unitsCount", units.size());
                    
                    for (Object unitObj : units) {
                        Map<String, Object> unit = (Map<String, Object>) unitObj;
                        if (subtopicId.equals(unit.get("_id")) || subtopicId.equals(unit.get("id"))) {
                            sanitizedDoc.put("foundUnit", Map.of(
                                "unitName", unit.get("unitName"),
                                "_id", unit.get("_id"),
                                "id", unit.get("id"),
                                "aiVideoUrl", unit.get("aiVideoUrl"),
                                "customDescription", unit.get("customDescription"),
                                "description", unit.get("description")
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
}
