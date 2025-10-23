package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.bson.types.ObjectId;

import com.mongodb.client.result.UpdateResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SubtopicController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PutMapping("/updateSubtopicVideo")
    public ResponseEntity<?> updateSubtopicVideo(@RequestBody UpdateSubtopicRequest request) {
        try {
            System.out.println("🔄 Spring Boot: Updating subtopic with AI video: " + request.getSubtopicId());
            System.out.println("🎥 AI Video URL: " + request.getAiVideoUrl());
            System.out.println("💾 Database: " + request.getDbname());
            System.out.println("📚 Subject Name: " + request.getSubjectName());

            // ✅ Use subjectName as collection name (e.g., "Physics", "Chemistry")
            String collectionName = request.getSubjectName();
            if (collectionName == null || collectionName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "subjectName is required",
                    "message", "Please provide the subject name to identify the correct collection"
                ));
            }

            UpdateResult result = null;
            String queryUsed = "";

            // 🔍 Try Query 1: Update nested unit in units array using _id field (correct field name)
            queryUsed = "Query 1: units._id with String";
            Query query1 = new Query(Criteria.where("units._id").is(request.getSubtopicId()));
            Update update1 = new Update().set("units.$.aiVideoUrl", request.getAiVideoUrl());
            result = mongoTemplate.updateFirst(query1, update1, collectionName);

            System.out.println("🔍 Query 1 result - Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());

            // 🔍 Try Query 2: Update nested unit using id field (alternative field name)
            if (result.getMatchedCount() == 0) {
                queryUsed = "Query 2: units.id with String";
                Query query2 = new Query(Criteria.where("units.id").is(request.getSubtopicId()));
                Update update2 = new Update().set("units.$.aiVideoUrl", request.getAiVideoUrl());
                result = mongoTemplate.updateFirst(query2, update2, collectionName);
                System.out.println("🔍 Query 2 result - Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
            }

            // 🔍 Try Query 3: Update as main document
            if (result.getMatchedCount() == 0) {
                queryUsed = "Query 3: _id with String";
                Query query3 = new Query(Criteria.where("_id").is(request.getSubtopicId()));
                Update update3 = new Update().set("aiVideoUrl", request.getAiVideoUrl());
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

            // Enhanced debugging for failures
            if (result.getMatchedCount() == 0) {
                System.out.println("❌ No documents found with subtopic ID: " + request.getSubtopicId());
                
                // Debug: Check what's actually in the collection
                Query allDocsQuery = new Query();
                long totalDocs = mongoTemplate.count(allDocsQuery, collectionName);
                System.out.println("🔍 Total documents in " + collectionName + ": " + totalDocs);
                
                // Get sample documents to see the structure
                Object sampleDoc = mongoTemplate.findOne(new Query().limit(1), Object.class, collectionName);
                System.out.println("🔍 Sample document structure: " + sampleDoc);
                
                response.put("warning", "No matching documents found");
                response.put("debug", Map.of(
                    "subtopicId", request.getSubtopicId(),
                    "collection", collectionName,
                    "totalDocuments", totalDocs,
                    "sampleDocument", sampleDoc,
                    "queriesTried", new String[]{"units._id", "units.id", "_id"}
                ));
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

    // Enhanced debug endpoint
    @GetMapping("/debug-subtopic/{subtopicId}")
    public ResponseEntity<?> debugSubtopic(@PathVariable String subtopicId, 
                                          @RequestParam(defaultValue = "professional") String dbname,
                                          @RequestParam String subjectName) {
        try {
            System.out.println("🔍 Debugging subtopic: " + subtopicId + " in subject: " + subjectName);
            
            String collectionName = subjectName;
            
            // Try to find parent document containing this subtopic
            Query query1 = new Query(Criteria.where("units._id").is(subtopicId));
            Object parentDoc = mongoTemplate.findOne(query1, Object.class, collectionName);
            
            // Try with id field
            Query query1a = new Query(Criteria.where("units.id").is(subtopicId));
            Object parentDocAlt = mongoTemplate.findOne(query1a, Object.class, collectionName);
            
            // Try to find as main document
            Query query2 = new Query(Criteria.where("_id").is(subtopicId));
            Object mainDoc = mongoTemplate.findOne(query2, Object.class, collectionName);
            
            // Count total documents
            Query countQuery = new Query();
            long totalDocs = mongoTemplate.count(countQuery, collectionName);
            
            // Get all documents to inspect structure
            Query allDocsQuery = new Query();
            List<Object> allDocs = mongoTemplate.find(allDocsQuery, Object.class, collectionName);
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("subtopicId", subtopicId);
            debugInfo.put("collection", collectionName);
            debugInfo.put("foundInUnitsWith_Id", parentDoc != null);
            debugInfo.put("foundInUnitsWithId", parentDocAlt != null);
            debugInfo.put("foundAsMain", mainDoc != null);
            debugInfo.put("totalDocuments", totalDocs);
            debugInfo.put("parentDocumentWith_Id", parentDoc);
            debugInfo.put("parentDocumentWithId", parentDocAlt);
            debugInfo.put("mainDocument", mainDoc);
            debugInfo.put("allDocumentsSample", allDocs);
            
            System.out.println("📊 Debug result: " + debugInfo);
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // NEW: Search across all collections
    @GetMapping("/debug-subtopic-all/{subtopicId}")
    public ResponseEntity<?> debugSubtopicAllCollections(@PathVariable String subtopicId, 
                                                        @RequestParam(defaultValue = "professional") String dbname) {
        try {
            System.out.println("🔍 Debugging subtopic across ALL collections: " + subtopicId);
            
            // Get all collection names
            var collectionNames = mongoTemplate.getCollectionNames();
            Map<String, Object> results = new HashMap<>();
            
            for (String collectionName : collectionNames) {
                System.out.println("🔍 Searching in collection: " + collectionName);
                
                // Try all query types
                Query query1 = new Query(Criteria.where("units._id").is(subtopicId));
                Object result1 = mongoTemplate.findOne(query1, Object.class, collectionName);
                
                Query query2 = new Query(Criteria.where("units.id").is(subtopicId));
                Object result2 = mongoTemplate.findOne(query2, Object.class, collectionName);
                
                Query query3 = new Query(Criteria.where("_id").is(subtopicId));
                Object result3 = mongoTemplate.findOne(query3, Object.class, collectionName);
                
                if (result1 != null || result2 != null || result3 != null) {
                    results.put(collectionName, Map.of(
                        "foundWithUnits_Id", result1 != null,
                        "foundWithUnitsId", result2 != null,
                        "foundAsMain", result3 != null,
                        "document", result1 != null ? result1 : (result2 != null ? result2 : result3)
                    ));
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("subtopicId", subtopicId);
            response.put("found", !results.isEmpty());
            response.put("results", results);
            response.put("collectionsSearched", collectionNames);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // FIXED: Debug subtopic structure in detail
    @GetMapping("/debug-subtopic-structure/{subtopicId}")
    public ResponseEntity<?> debugSubtopicStructure(@PathVariable String subtopicId,
                                                  @RequestParam String subjectName,
                                                  @RequestParam(defaultValue = "professional") String dbname) {
        try {
            System.out.println("🔍 DEBUG: Searching for subtopic: " + subtopicId + " in subject: " + subjectName);
            
            String collectionName = subjectName;
            
            // Query 1: Search as nested unit in units array using _id field
            Query nestedQuery = new Query(Criteria.where("units._id").is(subtopicId));
            Object nestedResult = mongoTemplate.findOne(nestedQuery, Object.class, collectionName);
            
            // Query 2: Search as nested unit using id field
            Query nestedQuery2 = new Query(Criteria.where("units.id").is(subtopicId));
            Object nestedResult2 = mongoTemplate.findOne(nestedQuery2, Object.class, collectionName);
            
            // Query 3: Search as main document
            Query mainQuery = new Query(Criteria.where("_id").is(subtopicId));
            Object mainResult = mongoTemplate.findOne(mainQuery, Object.class, collectionName);
            
            // Query 4: Get all documents to understand structure
            Query allQuery = new Query();
            List<Object> allDocuments = mongoTemplate.find(allQuery, Object.class, collectionName);
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("subtopicId", subtopicId);
            debugInfo.put("collection", collectionName);
            debugInfo.put("foundAsNestedUnitWith_Id", nestedResult != null);
            debugInfo.put("foundAsNestedUnitWithId", nestedResult2 != null);
            debugInfo.put("foundAsMainDocument", mainResult != null);
            debugInfo.put("totalDocumentsInCollection", allDocuments.size());
            
            if (nestedResult != null) {
                debugInfo.put("parentDocument", nestedResult);
                // Extract the specific subtopic
                Map<String, Object> parentMap = (Map<String, Object>) nestedResult;
                List<Map<String, Object>> units = (List<Map<String, Object>>) parentMap.get("units");
                if (units != null) {
                    Map<String, Object> foundSubtopic = units.stream()
                        .filter(unit -> subtopicId.equals(unit.get("_id")))
                        .findFirst()
                        .orElse(null);
                    debugInfo.put("foundSubtopic", foundSubtopic);
                }
            }
            
            if (nestedResult2 != null && debugInfo.get("foundSubtopic") == null) {
                debugInfo.put("parentDocumentWithId", nestedResult2);
                // Extract the specific subtopic using id field
                Map<String, Object> parentMap = (Map<String, Object>) nestedResult2;
                List<Map<String, Object>> units = (List<Map<String, Object>>) parentMap.get("units");
                if (units != null) {
                    Map<String, Object> foundSubtopic = units.stream()
                        .filter(unit -> subtopicId.equals(unit.get("id")))
                        .findFirst()
                        .orElse(null);
                    debugInfo.put("foundSubtopicWithId", foundSubtopic);
                }
            }
            
            // Sample of all documents for debugging
            List<Map<String, Object>> sampleDocs = new ArrayList<>();
            for (Object doc : allDocuments) {
                Map<String, Object> docMap = (Map<String, Object>) doc;
                Map<String, Object> sample = new HashMap<>();
                sample.put("_id", docMap.get("_id"));
                sample.put("unitName", docMap.get("unitName"));
                sample.put("hasUnits", docMap.containsKey("units"));
                if (docMap.get("units") != null) {
                    List<Map<String, Object>> units = (List<Map<String, Object>>) docMap.get("units");
                    sample.put("unitsCount", units.size());
                    List<String> unitIds = new ArrayList<>();
                    for (Map<String, Object> unit : units) {
                        unitIds.add("_id: " + unit.get("_id") + ", id: " + unit.get("id"));
                    }
                    sample.put("unitInfo", unitIds);
                }
                sampleDocs.add(sample);
            }
            debugInfo.put("allDocumentsSample", sampleDocs);
            
            System.out.println("📊 DEBUG RESULT: " + debugInfo);
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            System.err.println("❌ DEBUG ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // FIXED: Verify subtopic creation - uses ObjectId for parent lookup
    @GetMapping("/verify-subtopic-creation")
    public ResponseEntity<?> verifySubtopicCreation(@RequestParam String parentId,
                                                   @RequestParam String subjectName,
                                                   @RequestParam(defaultValue = "professional") String dbname) {
        try {
            System.out.println("🔍 VERIFY: Checking if parent exists and has subtopics: " + parentId);
            
            String collectionName = subjectName;
            
            // Find the parent document - convert string to ObjectId
            Query parentQuery = new Query(Criteria.where("_id").is(new ObjectId(parentId)));
            Object parentDoc = mongoTemplate.findOne(parentQuery, Object.class, collectionName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("parentExists", parentDoc != null);
            
            if (parentDoc != null) {
                Map<String, Object> parentMap = (Map<String, Object>) parentDoc;
                result.put("parentName", parentMap.get("unitName"));
                result.put("parentId", parentMap.get("_id"));
                
                if (parentMap.get("units") != null) {
                    List<Map<String, Object>> units = (List<Map<String, Object>>) parentMap.get("units");
                    result.put("subtopicsCount", units.size());
                    
                    List<Map<String, Object>> subtopics = new ArrayList<>();
                    for (Map<String, Object> unit : units) {
                        Map<String, Object> subtopicInfo = new HashMap<>();
                        subtopicInfo.put("_id", unit.get("_id")); // Use _id field
                        subtopicInfo.put("id", unit.get("id"));   // Use id field (if exists)
                        subtopicInfo.put("unitName", unit.get("unitName"));
                        subtopicInfo.put("parentId", unit.get("parentId"));
                        subtopics.add(subtopicInfo);
                    }
                    result.put("subtopics", subtopics);
                } else {
                    result.put("subtopicsCount", 0);
                    result.put("subtopics", List.of());
                }
            } else {
                System.out.println("❌ Parent document not found with ID: " + parentId);
                // Debug: List all documents in the collection
                Query allQuery = new Query();
                List<Object> allDocs = mongoTemplate.find(allQuery, Object.class, collectionName);
                System.out.println("🔍 All documents in " + collectionName + ": " + allDocs.size());
                for (Object doc : allDocs) {
                    Map<String, Object> docMap = (Map<String, Object>) doc;
                    System.out.println("📄 Document: _id=" + docMap.get("_id") + ", unitName=" + docMap.get("unitName"));
                }
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("❌ Error in verify-subtopic-creation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // FIXED: Get all subtopics for a parent - uses ObjectId
    @GetMapping("/get-all-subtopics/{parentId}")
    public ResponseEntity<?> getAllSubtopics(@PathVariable String parentId,
                                            @RequestParam String subjectName,
                                            @RequestParam(defaultValue = "professional") String dbname) {
        try {
            System.out.println("🔍 Getting all subtopics for parent: " + parentId);
            
            String collectionName = subjectName;
            
            // Find the parent document - convert string to ObjectId
            Query parentQuery = new Query(Criteria.where("_id").is(new ObjectId(parentId)));
            Object parentDoc = mongoTemplate.findOne(parentQuery, Object.class, collectionName);
            
            if (parentDoc == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Parent not found",
                    "parentId", parentId
                ));
            }
            
            Map<String, Object> parentMap = (Map<String, Object>) parentDoc;
            List<Map<String, Object>> subtopics = new ArrayList<>();
            
            if (parentMap.get("units") != null) {
                List<Map<String, Object>> units = (List<Map<String, Object>>) parentMap.get("units");
                for (Map<String, Object> unit : units) {
                    Map<String, Object> subtopicInfo = new HashMap<>();
                    subtopicInfo.put("_id", unit.get("_id"));     // Primary identifier
                    subtopicInfo.put("id", unit.get("id"));       // Alternative identifier
                    subtopicInfo.put("unitName", unit.get("unitName"));
                    subtopicInfo.put("parentId", unit.get("parentId"));
                    subtopicInfo.put("explanation", unit.get("explanation"));
                    subtopicInfo.put("aiVideoUrl", unit.get("aiVideoUrl"));
                    subtopicInfo.put("imageUrls", unit.get("imageUrls"));
                    subtopicInfo.put("audioFileId", unit.get("audioFileId"));
                    subtopics.add(subtopicInfo);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("parentId", parentId);
            response.put("parentName", parentMap.get("unitName"));
            response.put("subtopics", subtopics);
            response.put("totalSubtopics", subtopics.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

 // Add this method to your SubtopicController
    @GetMapping("/debug-parent-search")
    public ResponseEntity<?> debugParentSearch(@RequestParam String parentId,
                                              @RequestParam String subjectName,
                                              @RequestParam(defaultValue = "professional") String dbname) {
        try {
            System.out.println("🔍 DEBUG PARENT SEARCH: parentId=" + parentId + ", subjectName=" + subjectName);
            
            String collectionName = subjectName;
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("searchingForParentId", parentId);
            debugInfo.put("collection", collectionName);
            
            // Method 1: Try as ObjectId
            try {
                Query query1 = new Query(Criteria.where("_id").is(new ObjectId(parentId)));
                Object result1 = mongoTemplate.findOne(query1, Object.class, collectionName);
                debugInfo.put("foundWithObjectId", result1 != null);
                if (result1 != null) {
                    Map<String, Object> doc = (Map<String, Object>) result1;
                    debugInfo.put("documentWithObjectId", Map.of(
                        "_id", doc.get("_id"),
                        "unitName", doc.get("unitName")
                    ));
                }
            } catch (Exception e) {
                debugInfo.put("objectIdError", e.getMessage());
            }
            
            // Method 2: Try as String
            Query query2 = new Query(Criteria.where("_id").is(parentId));
            Object result2 = mongoTemplate.findOne(query2, Object.class, collectionName);
            debugInfo.put("foundWithString", result2 != null);
            if (result2 != null) {
                Map<String, Object> doc = (Map<String, Object>) result2;
                debugInfo.put("documentWithString", Map.of(
                    "_id", doc.get("_id"),
                    "unitName", doc.get("unitName")
                ));
            }
            
            // Method 3: List ALL documents in the collection
            Query allQuery = new Query();
            List<Object> allDocuments = mongoTemplate.find(allQuery, Object.class, collectionName);
            debugInfo.put("totalDocumentsInCollection", allDocuments.size());
            
            List<Map<String, Object>> allDocsInfo = new ArrayList<>();
            for (Object doc : allDocuments) {
                Map<String, Object> docMap = (Map<String, Object>) doc;
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("_id", docMap.get("_id"));
                docInfo.put("unitName", docMap.get("unitName"));
                docInfo.put("_idType", docMap.get("_id") != null ? docMap.get("_id").getClass().getSimpleName() : "null");
                
                if (docMap.get("units") != null) {
                    List<Map<String, Object>> units = (List<Map<String, Object>>) docMap.get("units");
                    docInfo.put("subtopicsCount", units.size());
                    List<String> subtopicIds = new ArrayList<>();
                    for (Map<String, Object> unit : units) {
                        subtopicIds.add("_id: " + unit.get("_id") + " (type: " + 
                                       (unit.get("_id") != null ? unit.get("_id").getClass().getSimpleName() : "null") + ")");
                    }
                    docInfo.put("subtopicIds", subtopicIds);
                }
                allDocsInfo.add(docInfo);
            }
            debugInfo.put("allDocuments", allDocsInfo);
            
            System.out.println("📊 DEBUG PARENT RESULT: " + debugInfo);
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            System.err.println("❌ DEBUG PARENT ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    // Request DTO for updateSubtopicVideo
    public static class UpdateSubtopicRequest {
        private String subtopicId;
        private String aiVideoUrl;
        private String dbname;
        private String subjectName;

        // Getters and setters
        public String getSubtopicId() { return subtopicId; }
        public void setSubtopicId(String subtopicId) { this.subtopicId = subtopicId; }

        public String getAiVideoUrl() { return aiVideoUrl; }
        public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

        public String getDbname() { return dbname; }
        public void setDbname(String dbname) { this.dbname = dbname; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

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