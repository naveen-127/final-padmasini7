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

    // -----------------------------
    // Get all units for a course/subject/standard
    // -----------------------------
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
    
    // -----------------------------
    // ✅ NEW: Update units order (drag and drop)
    // -----------------------------
    @PutMapping("/updateUnitsOrder")
    public ResponseEntity<Map<String, Object>> updateUnitsOrder(@RequestBody UpdateUnitsOrderRequest request) {
        try {
            System.out.println("🔄 Updating units order for: " + request.getSubjectName());
            System.out.println("📊 Units count: " + request.getUnits().size());
            System.out.println("📁 Parent path: " + request.getParentPath());
            System.out.println("📁 DB Name: " + request.getDbname());
            System.out.println("🎯 Standard: " + request.getStandard());
            
            String collectionName = request.getSubjectName();
            String dbname = request.getDbname();
            
            // Switch to the correct database
            // mongoTemplate.setDatabaseName(dbname); // Uncomment if needed
            
            if (request.getParentPath() == null || request.getParentPath().isEmpty()) {
                // Update top-level units order
                Query query = new Query(Criteria.where("standard").is(request.getStandard()));
                
                // Get documents from the specified collection
                List<Document> allDocs = mongoTemplate.findAll(Document.class, collectionName);
                
                System.out.println("📄 Found " + allDocs.size() + " documents in collection: " + collectionName);
                
                boolean updated = false;
                for (Document doc : allDocs) {
                    if (doc.containsKey("units")) {
                        System.out.println("✅ Found 'units' field in document: " + doc.get("_id"));
                        doc.put("units", request.getUnits());
                        mongoTemplate.save(doc, collectionName);
                        updated = true;
                        System.out.println("✅ Updated units order in document: " + doc.get("_id"));
                    }
                }
                
                if (updated) {
                    return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Units order updated successfully",
                        "updatedCount", request.getUnits().size()
                    ));
                } else {
                    return ResponseEntity.ok(Map.of(
                        "status", "warning",
                        "message", "No documents with 'units' field found",
                        "collection", collectionName
                    ));
                }
                
            } else {
                // Update nested units order (more complex)
                System.out.println("⚠️ Nested unit reordering requested - parentPath: " + request.getParentPath());
                return ResponseEntity.ok(Map.of(
                    "status", "partial",
                    "message", "Nested unit reordering - implement specific logic if needed",
                    "note", "Currently only top-level unit reordering is fully implemented"
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error updating units order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update units order: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
    
    // -----------------------------
    // ✅ DTO for UpdateUnitsOrderRequest
    // -----------------------------
    public static class UpdateUnitsOrderRequest {
        private String dbname;
        private String subjectName;
        private String standard;
        private List<Map<String, Object>> units;
        private String parentPath;
        private String unitId;
        
        // Getters and setters
        public String getDbname() { return dbname; }
        public void setDbname(String dbname) { this.dbname = dbname; }
        
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        
        public String getStandard() { return standard; }
        public void setStandard(String standard) { this.standard = standard; }
        
        public List<Map<String, Object>> getUnits() { return units; }
        public void setUnits(List<Map<String, Object>> units) { this.units = units; }
        
        public String getParentPath() { return parentPath; }
        public void setParentPath(String parentPath) { this.parentPath = parentPath; }
        
        public String getUnitId() { return unitId; }
        public void setUnitId(String unitId) { this.unitId = unitId; }
    }
}
