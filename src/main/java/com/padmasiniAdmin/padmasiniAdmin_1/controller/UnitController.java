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


@GetMapping("/getAllUnitsWithoutStandard/{dbname}/{subjectName}")
public List<UnitRequest> getUnitsWithoutStandard(@PathVariable String dbname,
                                                @PathVariable String subjectName) {
    // You can call the service method or implement logic here
    return unitService.getAllUnitWithoutStandard(dbname, subjectName);
}

//Add this new endpoint to UnitController.java
@GetMapping("/getSpecialSubjectStructure/{dbname}/{subjectName}")
public ResponseEntity<Map<String, Object>> getSpecialSubjectStructure(
     @PathVariable String dbname,
     @PathVariable String subjectName) {
 
 Map<String, Object> response = new HashMap<>();
 
 try {
     List<UnitRequest> units = unitService.getAllUnitWithoutStandard(dbname, subjectName);
     
     // Transform the data for special subjects
     List<Map<String, Object>> transformedUnits = new ArrayList<>();
     
     for (UnitRequest unit : units) {
         Map<String, Object> unitMap = new HashMap<>();
         unitMap.put("id", unit.getId());
         unitMap.put("unitName", unit.getUnitName());
         unitMap.put("explanation", unit.getExplanation());
         unitMap.put("description", unit.getDescription());
         unitMap.put("customDescription", unit.getCustomDescription());
         unitMap.put("imageUrls", unit.getImageUrls() != null ? unit.getImageUrls() : new ArrayList<>());
         unitMap.put("audioFileId", unit.getAudioFileId() != null ? unit.getAudioFileId() : new ArrayList<>());
         unitMap.put("aiVideoUrl", unit.getAiVideoUrl());
         unitMap.put("tags", unit.getTags() != null ? unit.getTags() : new ArrayList<>());
         unitMap.put("test", unit.getTest() != null ? unit.getTest() : new ArrayList<>());
         unitMap.put("units", unit.getUnits() != null ? unit.getUnits() : new ArrayList<>());
         unitMap.put("isLesson", true); // Always true for special subjects
         unitMap.put("standard", null); // No standard for special subjects
         
         transformedUnits.add(unitMap);
     }
     
     response.put("status", "success");
     response.put("units", transformedUnits);
     response.put("subjectType", "special");
     response.put("count", transformedUnits.size());
     
 } catch (Exception e) {
     response.put("status", "error");
     response.put("message", e.getMessage());
 }
 
 return ResponseEntity.ok(response);
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
 // Move subtopic/unit up/down
 // -----------------------------
 @PostMapping("/moveUnit/{direction}")
 public ResponseEntity<Map<String, Object>> moveUnit(@RequestBody WrapperUnit unit,
                                                    @PathVariable String direction) {
     Map<String, Object> response = new HashMap<>();
     
     System.out.println("🎯 Received moveUnit request:");
     System.out.println("  - Direction: " + direction);
     System.out.println("  - Unit ID: " + unit.getParentId());
     System.out.println("  - Unit Name: " + unit.getUnitName());
     System.out.println("  - Root ID: " + unit.getRootId());
     System.out.println("  - DB: " + unit.getDbname());
     System.out.println("  - Subject: " + unit.getSubjectName());
     
     if (!direction.equals("up") && !direction.equals("down")) {
         response.put("status", "failed");
         response.put("message", "Direction must be 'up' or 'down'");
         return ResponseEntity.badRequest().body(response);
     }
     
     try {
         boolean moved = unitService.moveUnit(unit, direction);
         
         if (moved) {
             response.put("status", "success");
             response.put("message", "Unit moved successfully");
         } else {
             response.put("status", "failed");
             response.put("message", "Unit not found or cannot be moved");
         }
         
         return ResponseEntity.ok(response);
         
     } catch (Exception e) {
         System.err.println("❌ Error in moveUnit: " + e.getMessage());
         e.printStackTrace();
         
         response.put("status", "error");
         response.put("message", "Internal server error: " + e.getMessage());
         return ResponseEntity.status(500).body(response);
     }
 }

 // -----------------------------
 // Move test up/down
 // -----------------------------
 @PostMapping("/moveTest/{direction}")
 public ResponseEntity<Map<String, Object>> moveTest(
         @RequestParam String rootId,
         @RequestParam String parentId,
         @RequestParam String testName,
         @RequestParam String dbname,
         @RequestParam String subjectName,
         @PathVariable String direction) {
     
     Map<String, Object> response = new HashMap<>();
     
     System.out.println("🎯 Received moveTest request:");
     System.out.println("  - Direction: " + direction);
     System.out.println("  - Root ID: " + rootId);
     System.out.println("  - Parent ID: " + parentId);
     System.out.println("  - Test Name: " + testName);
     System.out.println("  - DB: " + dbname);
     System.out.println("  - Subject: " + subjectName);
     
     if (!direction.equals("up") && !direction.equals("down")) {
         response.put("status", "failed");
         response.put("message", "Direction must be 'up' or 'down'");
         return ResponseEntity.badRequest().body(response);
     }
     
     try {
         boolean moved = unitService.moveTest(rootId, parentId, testName, direction, dbname, subjectName);
         
         if (moved) {
             response.put("status", "success");
             response.put("message", "Test moved successfully");
         } else {
             response.put("status", "failed");
             response.put("message", "Test not found or cannot be moved");
         }
         
         return ResponseEntity.ok(response);
         
     } catch (Exception e) {
         System.err.println("❌ Error in moveTest: " + e.getMessage());
         e.printStackTrace();
         
         response.put("status", "error");
         response.put("message", "Internal server error: " + e.getMessage());
         return ResponseEntity.status(500).body(response);
     }
 }
}
