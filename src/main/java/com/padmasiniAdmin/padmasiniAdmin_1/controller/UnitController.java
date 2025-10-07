package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.service.UnitService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // ✅ allow frontend to send requests
public class UnitController {

    @Autowired
    private UnitService unitService;

    // -----------------------------
    // 🟩 Get all units
    // -----------------------------
    @GetMapping("/getAllUnits/{dbname}/{subjectName}/{standard}")
    public List<UnitRequest> getUnitsBySubject(
            @PathVariable String dbname,
            @PathVariable String subjectName,
            @PathVariable String standard) {
        return unitService.getAllUnit(dbname, subjectName, standard);
    }

    // -----------------------------
    // 🟩 Add head unit
    // -----------------------------
    @PostMapping("/addNewHeadUnit")
    public ResponseEntity<Map<String, String>> addHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        if (unitService.addNewHeadUnit(request)) response.put("status", "pass");
        else response.put("status", "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // 🟩 Update head unit
    // -----------------------------
    @PutMapping("/updateHeadUnit/{newUnitName}")
    public ResponseEntity<Map<String, String>> updateHeadUnit(
            @RequestBody WrapperUnitRequest request,
            @PathVariable String newUnitName) {
        Map<String, String> response = new HashMap<>();
        if (unitService.updateHeadUnitName(request, newUnitName)) response.put("status", "pass");
        else response.put("status", "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // 🟩 Delete head unit
    // -----------------------------
    @DeleteMapping("/deleteHeadUnit")
    public ResponseEntity<Map<String, String>> deleteHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        if (unitService.deleteHeadUnit(request)) response.put("status", "pass");
        else response.put("status", "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // 🟩 Delete sub-unit
    // -----------------------------
    @DeleteMapping("/deleteUnit")
    public ResponseEntity<Map<String, String>> deleteUnit(@RequestBody WrapperUnit unit) {
        unitService.deleteUnit(unit);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    // -----------------------------
    // 🟩 Update sub-section
    // -----------------------------
    @PostMapping("/updateSubsection")
    public ResponseEntity<Map<String, String>> updateSubUnit(@RequestBody WrapperUnit unit) {
        unitService.updateUnit(unit);
        return ResponseEntity.ok(Map.of("status", "updated"));
    }

    // -----------------------------
    // 🟩 Add new sub-section
    // -----------------------------
    @PostMapping("/addNewSubsection")
    public ResponseEntity<Map<String, String>> addSubUnit(@RequestBody WrapperUnit unit) {
        unitService.addUnit(unit);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // -----------------------------
    // 🟩 Add new subtopic (used by your frontend)
    // -----------------------------
    @PostMapping("/addSubtopic")
    public ResponseEntity<Map<String, String>> addSubtopic(@RequestBody WrapperUnit data) {
        unitService.addUnit(data);
        return ResponseEntity.ok(Map.of("status", "success"));
    }
}
