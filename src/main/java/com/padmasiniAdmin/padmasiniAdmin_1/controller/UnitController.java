package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.bson.Document;

import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.service.UnitService;

@RestController
@RequestMapping("/api")
public class UnitController {

    @Autowired
    private UnitService unitService;
    
    @Autowired
    private MongoTemplate mongoTemplate; // Add this

@GetMapping("/getAllUnits/{dbname}/{subjectName}/{standard}")
    public List<UnitRequest> getUnitsBySubject(@PathVariable String dbname,
                                               @PathVariable String subjectName,
                                               @PathVariable String standard) {
        List<UnitRequest> units = unitService.getAllUnit(dbname, subjectName, standard);
        return units != null ? units : new ArrayList<>();
    }

    // -----------------------------
    // Add new head unit
    // -----------------------------
    @PostMapping("/addNewHeadUnit")
    public ResponseEntity<Map<String, String>> addHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("status", unitService.addNewHeadUnit(request) ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Add new subtopic/unit
    // -----------------------------
    @PostMapping("/addSubtopic")
    public ResponseEntity<Map<String, Object>> addSubtopic(@RequestBody WrapperUnit unit) {
        String insertedSubId = unitService.addUnit(unit);

        Map<String, Object> response = new HashMap<>();
        response.put("status", insertedSubId != null ? "success" : "failed");
        response.put("insertedSubId", insertedSubId);

        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Update head unit
    // -----------------------------
    @PutMapping("/updateHeadUnit/{newUnitName}")
    public ResponseEntity<Map<String, String>> updateHeadUnit(@RequestBody WrapperUnitRequest request,
                                                              @PathVariable String newUnitName) {
        Map<String, String> response = new HashMap<>();
        response.put("status", unitService.updateHeadUnitName(request, newUnitName) ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Delete head unit
    // -----------------------------
    @DeleteMapping("/deleteHeadUnit")
    public ResponseEntity<Map<String, String>> deleteHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("status", unitService.deleteHeadUnit(request) ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Delete a subunit
    // -----------------------------
    @DeleteMapping("/deleteUnit")
    public ResponseEntity<Map<String, String>> deleteUnit(@RequestBody WrapperUnit unit) {
        boolean deleted = unitService.deleteUnit(unit);
        Map<String, String> response = new HashMap<>();
        response.put("status", deleted ? "deleted" : "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Update a subunit
    // -----------------------------
    @PutMapping("/updateSubsection")
    public ResponseEntity<Map<String, String>> updateSubUnit(@RequestBody WrapperUnit unit) {
        boolean updated = unitService.updateUnit(unit);
        Map<String, String> response = new HashMap<>();
        response.put("status", updated ? "updated" : "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // Add subunit (alternate endpoint)
    // -----------------------------
    @PostMapping("/addNewSubsection")
    public ResponseEntity<Map<String, Object>> addSubUnit(@RequestBody WrapperUnit unit) {
        String insertedSubId = unitService.addUnit(unit);

        Map<String, Object> response = new HashMap<>();
        response.put("status", insertedSubId != null ? "success" : "failed");
        response.put("insertedSubId", insertedSubId);

        return ResponseEntity.ok(response);
    }
    
    // =============================
    // NEW ENDPOINT: Save Unit Order
    // =============================
    @PostMapping("/saveUnitOrder")
    public ResponseEntity<Map<String, Object>> saveUnitOrder(@RequestBody SaveUnitOrderRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("💾 Saving unit order for: " + 
                             request.getSubjectName() + ", Standard: " + 
                             request.getStandard() + ", DB: " + request.getDbname());
            
            if (request.getUnitData() == null || request.getUnitData().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Unit data is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get the database and collection
            MongoDatabase database = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(request.getDbname());
            MongoCollection<Document> collection = database.getCollection("units");
            
            // Process and update order for all units
            int updatedCount = processAndUpdateOrder(
                collection, 
                request.getUnitData(), 
                request.getSubjectName(), 
                request.getStandard(), 
                null // parentId for root units
            );
            
            response.put("status", "success");
            response.put("message", "Unit order saved successfully");
            response.put("count", updatedCount);
            
            // Refresh and return the updated ordered data
            List<UnitRequest> orderedUnits = unitService.getAllUnit(
                request.getDbname(), 
                request.getSubjectName(), 
                request.getStandard()
            );
            response.put("unitData", orderedUnits);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error saving unit order: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "error");
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // =============================
    // Helper method to process and update order recursively
    // =============================
    private int processAndUpdateOrder(
            MongoCollection<Document> collection,
            List<Map<String, Object>> units,
            String subjectName,
            String standard,
            String parentId) {
        
        int updatedCount = 0;
        
        if (units == null || units.isEmpty()) {
            return 0;
        }
        
        for (int i = 0; i < units.size(); i++) {
            Map<String, Object> unit = units.get(i);
            
            String unitId = extractUnitId(unit);
            String unitName = (String) unit.get("unitName");
            
            if (unitId != null) {
                // Build filter - try multiple approaches
                Document filter = new Document();
                
                // Try by _id first
                try {
                    filter.append("_id", new ObjectId(unitId));
                } catch (IllegalArgumentException e) {
                    filter.append("_id", unitId);
                }
                
                // Add additional filters for safety
                filter.append("subjectName", subjectName);
                filter.append("standard", standard);
                
                if (parentId != null) {
                    filter.append("parentId", parentId);
                }
                
                // Create update with order
                Document update = new Document("$set", 
                    new Document("order", i)
                        .append("parentId", parentId)
                );
                
                try {
                    UpdateResult result = collection.updateOne(filter, update);
                    
                    if (result.getModifiedCount() == 0) {
                        // Try alternative: by unitName and parentId
                        Document altFilter = new Document("unitName", unitName)
                            .append("subjectName", subjectName)
                            .append("standard", standard);
                        
                        if (parentId != null) {
                            altFilter.append("parentId", parentId);
                        }
                        
                        UpdateResult altResult = collection.updateOne(altFilter, update);
                        if (altResult.getModifiedCount() > 0) {
                            updatedCount++;
                            System.out.println("✅ Updated order for unit (by name): " + unitName + " to position: " + i);
                        } else {
                            System.out.println("⚠️ No unit found: " + unitName + " (ID: " + unitId + ")");
                        }
                    } else {
                        updatedCount++;
                        System.out.println("✅ Updated order for unit: " + unitName + " to position: " + i);
                    }
                    
                } catch (Exception e) {
                    System.err.println("⚠️ Error updating unit " + unitName + ": " + e.getMessage());
                }
                
                // Recursively update children
                if (unit.get("units") != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> childUnits = (List<Map<String, Object>>) unit.get("units");
                    updatedCount += processAndUpdateOrder(
                        collection, 
                        childUnits, 
                        subjectName, 
                        standard, 
                        unitId
                    );
                }
            } else {
                System.err.println("⚠️ Skipping unit with null ID: " + unit.get("unitName"));
            }
        }
        
        return updatedCount;
    }
    
    private String extractUnitId(Map<String, Object> unit) {
        Object idObj = unit.get("id");
        if (idObj == null) {
            idObj = unit.get("_id");
        }
        
        if (idObj instanceof String) {
            return (String) idObj;
        } else if (idObj instanceof ObjectId) {
            return ((ObjectId) idObj).toString();
        }
        return null;
    }
    
    // =============================
    // Inner class for SaveUnitOrder request
    // =============================
    public static class SaveUnitOrderRequest {
        private String dbname;
        private String subjectName;
        private String standard;
        private List<Map<String, Object>> unitData;
        
        // Getters and setters
        public String getDbname() { return dbname; }
        public void setDbname(String dbname) { this.dbname = dbname; }
        
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        
        public String getStandard() { return standard; }
        public void setStandard(String standard) { this.standard = standard; }
        
        public List<Map<String, Object>> getUnitData() { return unitData; }
        public void setUnitData(List<Map<String, Object>> unitData) { this.unitData = unitData; }
    }
}
