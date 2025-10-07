package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.service.UnitService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow all frontend origins
public class UnitController {

    @Autowired
    private UnitService unitService;

    // 🟩 Get all units by subject
    @GetMapping("/getAllUnits/{dbname}/{subjectName}/{standard}")
    public ResponseEntity<List<UnitRequest>> getUnitsBySubject(
            @PathVariable String dbname,
            @PathVariable String subjectName,
            @PathVariable String standard) {

        List<UnitRequest> units = unitService.getAllUnit(dbname, subjectName, standard);
        return ResponseEntity.ok(units);
    }

    // 🟩 Add new head unit
    @PostMapping("/addNewHeadUnit")
    public ResponseEntity<Map<String, String>> addHeadUnit(@RequestBody WrapperUnitRequest request) {
        boolean success = unitService.addNewHeadUnit(request);

        return ResponseEntity.ok(Map.of(
                "status", success ? "pass" : "failed",
                "message", success ? "Head unit added successfully" : "Failed to add head unit"
        ));
    }

    // 🟩 Update head unit
    @PutMapping("/updateHeadUnit/{newUnitName}")
    public ResponseEntity<Map<String, String>> updateHeadUnit(
            @RequestBody WrapperUnitRequest request,
            @PathVariable String newUnitName) {

        boolean success = unitService.updateHeadUnitName(request, newUnitName);

        return ResponseEntity.ok(Map.of(
                "status", success ? "pass" : "failed",
                "message", success ? "Head unit updated successfully" : "Failed to update head unit"
        ));
    }

    // 🟩 Delete head unit
    @DeleteMapping("/deleteHeadUnit")
    public ResponseEntity<Map<String, String>> deleteHeadUnit(@RequestBody WrapperUnitRequest request) {
        boolean success = unitService.deleteHeadUnit(request);

        return ResponseEntity.ok(Map.of(
                "status", success ? "pass" : "failed",
                "message", success ? "Head unit deleted successfully" : "Failed to delete head unit"
        ));
    }

    // 🟩 Delete sub-unit
    @DeleteMapping("/deleteUnit")
    public ResponseEntity<Map<String, String>> deleteUnit(@RequestBody WrapperUnit unit) {
        try {
            unitService.deleteUnit(unit);
            return ResponseEntity.ok(Map.of(
                    "status", "deleted",
                    "message", "Sub-unit deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "failed", "message", e.getMessage()));
        }
    }

    // 🟩 Update sub-section (sub-unit)
    @PostMapping("/updateSubsection")
    public ResponseEntity<Map<String, String>> updateSubUnit(@RequestBody WrapperUnit unit) {
        try {
            unitService.updateUnit(unit);
            return ResponseEntity.ok(Map.of(
                    "status", "updated",
                    "message", "Sub-section updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "failed", "message", e.getMessage()));
        }
    }

    // 🟩 Add new sub-section
    @PostMapping("/addNewSubsection")
    public ResponseEntity<Map<String, String>> addSubUnit(@RequestBody WrapperUnit unit) {
        try {
            unitService.addUnit(unit);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Sub-section added successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "failed", "message", e.getMessage()));
        }
    }

    // 🟩 Add subtopic (with imageUrl from frontend)
    @PostMapping("/addSubtopic")
    public ResponseEntity<Map<String, String>> addSubtopic(@RequestBody WrapperUnit data) {
        try {
            // Example frontend request:
            // {
            //   "dbname": "padmasini",
            //   "unitName": "Biology Unit 1",
            //   "subtopicName": "Cell Division",
            //   "imageUrl": "https://s3.amazonaws.com/.../image.png"
            // }
            unitService.addUnit(data);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Subtopic added successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "failed", "message", e.getMessage()));
        }
    }
}
