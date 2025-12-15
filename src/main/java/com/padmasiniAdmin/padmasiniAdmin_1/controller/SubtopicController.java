package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.result.UpdateResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow all origins for testing
public class SubtopicController {

    @Autowired
    private MongoTemplate mongoTemplate;

    // Helper method to safely get string value from map
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    // NEW: Recursive method to update nested subtopic with AI video
    private boolean updateNestedSubtopicRecursive(List<Map<String, Object>> subtopics, String targetId, String aiVideoUrl) {
        for (Map<String, Object> subtopic : subtopics) {
            // Check if current subtopic matches the target
            String currentId = getStringValue(subtopic, "_id");
            String currentIdAlt = getStringValue(subtopic, "id");
            
            if (targetId.equals(currentId) || targetId.equals(currentIdAlt)) {
                // Found the target subtopic - update it
                subtopic.put("aiVideoUrl", aiVideoUrl);
                subtopic.put("updatedAt", new Date());
                return true;
            }
            
            // Check if this subtopic has children and search recursively
            if (subtopic.containsKey("children") && subtopic.get("children") instanceof List) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) subtopic.get("children");
                boolean found = updateNestedSubtopicRecursive(children, targetId, aiVideoUrl);
                if (found) return true;
            }
            
            // Also check "units" array for nested structure
            if (subtopic.containsKey("units") && subtopic.get("units") instanceof List) {
                List<Map<String, Object>> units = (List<Map<String, Object>>) subtopic.get("units");
                boolean found = updateNestedSubtopicRecursive(units, targetId, aiVideoUrl);
                if (found) return true;
            }
            
            // Check "subtopics" array
            if (subtopic.containsKey("subtopics") && subtopic.get("subtopics") instanceof List) {
                List<Map<String, Object>> nestedSubtopics = (List<Map<String, Object>>) subtopic.get("subtopics");
                boolean found = updateNestedSubtopicRecursive(nestedSubtopics, targetId, aiVideoUrl);
                if (found) return true;
            }
        }
        return false;
    }

    // Helper method for deep copy of list
    private List<Map<String, Object>> deepCopyList(List<Map<String, Object>> original) {
        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> item : original) {
            copy.add(new HashMap<>(item));
        }
        return copy;
    }

    // Original direct update methods as fallback
    private boolean performDirectUpdates(UpdateSubtopicRequest request, String collectionName) {
        try {
            // Try updating as nested unit in units array
            Query query1 = new Query(Criteria.where("units._id").is(request.getSubtopicId()));
            Update update1 = new Update().set("units.$.aiVideoUrl", request.getAiVideoUrl())
                                         .set("units.$.updatedAt", new Date());
            UpdateResult result1 = mongoTemplate.updateFirst(query1, update1, collectionName);
            
            if (result1.getMatchedCount() > 0) {
                System.out.println("✅ Direct update successful via units._id");
                return true;
            }

            // Try with units.id field
            Query query2 = new Query(Criteria.where("units.id").is(request.getSubtopicId()));
            Update update2 = new Update().set("units.$.aiVideoUrl", request.getAiVideoUrl())
                                         .set("units.$.updatedAt", new Date());
            UpdateResult result2 = mongoTemplate.updateFirst(query2, update2, collectionName);
            
            if (result2.getMatchedCount() > 0) {
                System.out.println("✅ Direct update successful via units.id");
                return true;
            }

            // Try as main document
            Query query3 = new Query(Criteria.where("_id").is(request.getSubtopicId()));
            Update update3 = new Update().set("aiVideoUrl", request.getAiVideoUrl())
                                         .set("updatedAt", new Date());
            UpdateResult result3 = mongoTemplate.updateFirst(query3, update3, collectionName);
            
            if (result3.getMatchedCount() > 0) {
                System.out.println("✅ Direct update successful as main document");
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Direct update fallback error: " + e.getMessage());
            return false;
        }
    }

    // NEW: Enhanced recursive update endpoint
    @PutMapping("/updateSubtopicVideoRecursive")
    public ResponseEntity<?> updateSubtopicVideoRecursive(@RequestBody UpdateSubtopicRequest request) {
        try {
            System.out.println("🔄 Recursive update for subtopic: " + request.getSubtopicId());
            System.out.println("🎥 AI Video URL: " + request.getAiVideoUrl());
            System.out.println("📚 Subject Name: " + request.getSubjectName());

            String collectionName = request.getSubjectName();
            if (collectionName == null || collectionName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "subjectName is required"
                ));
            }

            boolean updated = false;
            String queryUsed = "not_found";
            UpdateResult result = null;

            // Strategy 1: Search in all documents with nested structures
            Query nestedQuery = new Query(new Criteria().orOperator(
                Criteria.where("units").exists(true),
                Criteria.where("children").exists(true),
                Criteria.where("subtopics").exists(true)
            ));
            
            List<Object> documents = mongoTemplate.find(nestedQuery, Object.class, collectionName);
            
            for (Object doc : documents) {
                Map<String, Object> document = (Map<String, Object>) doc;
                
                // Check and update in units array
                if (document.containsKey("units") && document.get("units") instanceof List) {
                    List<Map<String, Object>> units = (List<Map<String, Object>>) document.get("units");
                    List<Map<String, Object>> unitsCopy = deepCopyList(units);
                    
                    if (updateNestedSubtopicRecursive(unitsCopy, request.getSubtopicId(), request.getAiVideoUrl())) {
                        Query updateQuery = new Query(Criteria.where("_id").is(document.get("_id")));
                        Update update = new Update().set("units", unitsCopy);
                        result = mongoTemplate.updateFirst(updateQuery, update, collectionName);
                        updated = true;
                        queryUsed = "recursive_units_update";
                        System.out.println("✅ Recursive update successful in units array");
                        break;
                    }
                }
                
                // Check and update in children array
                if (!updated && document.containsKey("children") && document.get("children") instanceof List) {
                    List<Map<String, Object>> children = (List<Map<String, Object>>) document.get("children");
                    List<Map<String, Object>> childrenCopy = deepCopyList(children);
                    
                    if (updateNestedSubtopicRecursive(childrenCopy, request.getSubtopicId(), request.getAiVideoUrl())) {
                        Query updateQuery = new Query(Criteria.where("_id").is(document.get("_id")));
                        Update update = new Update().set("children", childrenCopy);
                        result = mongoTemplate.updateFirst(updateQuery, update, collectionName);
                        updated = true;
                        queryUsed = "recursive_children_update";
                        System.out.println("✅ Recursive update successful in children array");
                        break;
                    }
                }
            }

            // Strategy 2: Fallback to original direct update methods
            if (!updated) {
                System.out.println("🔄 Recursive search failed, trying direct updates...");
                updated = performDirectUpdates(request, collectionName);
                queryUsed = updated ? "direct_update_fallback" : "no_update_performed";
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("updated", updated);
            response.put("queryUsed", queryUsed);
            response.put("recursive", true);
            response.put("message", updated ? 
                "AI video URL saved successfully in nested structure" : 
                "Subtopic not found in any nested structure");

            System.out.println("✅ Recursive update completed: " + (updated ? "SUCCESS" : "NOT_FOUND"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Recursive update error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update subtopic: " + e.getMessage()));
        }
    }

    // Original update endpoint for backward compatibility
    @PutMapping("/updateSubtopicVideo")
    public ResponseEntity<?> updateSubtopicVideo(@RequestBody UpdateSubtopicRequest request) {
        try {
            System.out.println("🔄 Spring Boot: Updating subtopic with AI video: " + request.getSubtopicId());
            System.out.println("🎥 AI Video URL: " + request.getAiVideoUrl());
            System.out.println("💾 Database: " + request.getDbname());
            System.out.println("📚 Subject Name: " + request.getSubjectName());

            String collectionName = request.getSubjectName();
            if (collectionName == null || collectionName.trim().isEmpty()) { 
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "subjectName is required",
                    "message", "Please provide the subject name to identify the correct collection"
                ));
            }

            UpdateResult result = null;
            String queryUsed = "";

            // Try Query 1: Update nested unit in units array using _id field
            queryUsed = "Query 1: units._id with String";
            Query query1 = new Query(Criteria.where("units._id").is(request.getSubtopicId()));
            Update update1 = new Update().set("units.$.aiVideoUrl", request.getAiVideoUrl())
                                         .set("units.$.updatedAt", new Date());
            result = mongoTemplate.updateFirst(query1, update1, collectionName);

            System.out.println("🔍 Query 1 result - Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());

            // Try Query 2: Update nested unit using id field
            if (result.getMatchedCount() == 0) {
                queryUsed = "Query 2: units.id with String";
                Query query2 = new Query(Criteria.where("units.id").is(request.getSubtopicId()));
                Update update2 = new Update().set("units.$.aiVideoUrl", request.getAiVideoUrl())
                                             .set("units.$.updatedAt", new Date());
                result = mongoTemplate.updateFirst(query2, update2, collectionName);
                System.out.println("🔍 Query 2 result - Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
            }

            // Try Query 3: Update as main document
            if (result.getMatchedCount() == 0) {
                queryUsed = "Query 3: _id with String";
                Query query3 = new Query(Criteria.where("_id").is(request.getSubtopicId()));
                Update update3 = new Update().set("aiVideoUrl", request.getAiVideoUrl())
                                             .set("updatedAt", new Date());
                result = mongoTemplate.updateFirst(query3, update3, collectionName);
                System.out.println("🔍 Query 3 result - Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("updated", result.getModifiedCount());
            response.put("matched", result.getMatchedCount());
            response.put("queryUsed", queryUsed);
            response.put("collection", collectionName);
            response.put("message", "AI video URL saved successfully");

            System.out.println("✅ Spring Boot: AI video URL saved. Query: " + queryUsed + 
                             ", Collection: " + collectionName + 
                             ", Modified: " + result.getModifiedCount());

            if (result.getMatchedCount() == 0) {
                System.out.println("❌ No documents found with subtopic ID: " + request.getSubtopicId());
                response.put("warning", "No matching documents found");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Spring Boot: Error updating subtopic with AI video: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update subtopic: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // DEBUG: Check if subtopic exists
    @GetMapping("/debug-subtopic/{subtopicId}")
    public ResponseEntity<?> debugSubtopic(@PathVariable String subtopicId, 
                                          @RequestParam(defaultValue = "professional") String dbname,
                                          @RequestParam String subjectName) {
        try {
            System.out.println("🔍 Debug subtopic: " + subtopicId + " in collection: " + subjectName);
            
            // First try the specified collection
            Query query = new Query(new Criteria().orOperator(
                Criteria.where("_id").is(subtopicId),
                Criteria.where("units._id").is(subtopicId),
                Criteria.where("units.id").is(subtopicId),
                Criteria.where("children._id").is(subtopicId),
                Criteria.where("children.id").is(subtopicId),
                Criteria.where("subtopics._id").is(subtopicId),
                Criteria.where("subtopics.id").is(subtopicId)
            ));
            
            List<Object> documents = mongoTemplate.find(query, Object.class, subjectName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("found", !documents.isEmpty());
            response.put("count", documents.size());
            response.put("collection", subjectName);
            response.put("subtopicId", subtopicId);
            
            if (!documents.isEmpty()) {
                System.out.println("✅ Found " + documents.size() + " document(s) in " + subjectName);
                // Return first document (simplified for debugging)
                Map<String, Object> firstDoc = (Map<String, Object>) documents.get(0);
                response.put("documentId", firstDoc.get("_id"));
                response.put("hasUnits", firstDoc.containsKey("units"));
                response.put("hasChildren", firstDoc.containsKey("children"));
                response.put("hasSubtopics", firstDoc.containsKey("subtopics"));
            } else {
                System.out.println("❌ Not found in collection: " + subjectName);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Debug error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "found", false
            ));
        }
    }

    @GetMapping("/debug-subtopic-all/{subtopicId}")
    public ResponseEntity<?> debugSubtopicAllCollections(@PathVariable String subtopicId, 
                                                        @RequestParam(defaultValue = "professional") String dbname) {
        try {
            System.out.println("🔍 Debug subtopic in ALL collections: " + subtopicId);
            
            // Get all collections in the database
            List<String> collectionNames = mongoTemplate.getCollectionNames();
            System.out.println("📚 Available collections: " + collectionNames);
            
            List<Map<String, Object>> foundIn = new ArrayList<>();
            
            for (String collectionName : collectionNames) {
                Query query = new Query(new Criteria().orOperator(
                    Criteria.where("_id").is(subtopicId),
                    Criteria.where("units._id").is(subtopicId),
                    Criteria.where("units.id").is(subtopicId)
                ));
                
                List<Object> documents = mongoTemplate.find(query, Object.class, collectionName);
                
                if (!documents.isEmpty()) {
                    Map<String, Object> foundInfo = new HashMap<>();
                    foundInfo.put("collection", collectionName);
                    foundInfo.put("count", documents.size());
                    foundIn.add(foundInfo);
                    System.out.println("✅ Found in collection: " + collectionName);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("subtopicId", subtopicId);
            response.put("found", !foundIn.isEmpty());
            response.put("foundIn", foundIn);
            response.put("totalCollections", collectionNames.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Debug all error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "found", false
            ));
        }
    }

    // Request DTO for updateSubtopicVideo
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
                    ", parentId='" + parentId + '\'' +
                    ", rootId='" + rootId + '\'' +
                    '}';
        }
    }
}
