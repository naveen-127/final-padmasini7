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
@CrossOrigin(origins = "*", allowCredentials = "true") // ✅ allow frontend to send requests with cookies
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
        response.put("status", unitService.addNewHeadUnit(request) ? "pass" : "failed");
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
        response.put("status", unitService.updateHeadUnitName(request, newUnitName) ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // 🟩 Delete head unit
    // -----------------------------
    @DeleteMapping("/deleteHeadUnit")
    public ResponseEntity<Map<String, String>> deleteHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("status", unitService.deleteHeadUnit(request) ? "pass" : "failed");
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
    // 🟩 Add new subtopic
    // -----------------------------
    @PostMapping("/addSubtopic")
    public ResponseEntity<Map<String, Object>> addSubtopic(@RequestBody WrapperUnit data) {
        // Save the subtopic and return inserted ID
        String insertedSubId = unitService.addUnit(data); // make sure this returns the ID
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("insertedSubId", insertedSubId);
        return ResponseEntity.ok(response);
    }

    // -----------------------------
    // 🟩 Optional: check session
    // -----------------------------
    @GetMapping("/checkSession")
    public ResponseEntity<Map<String, Object>> checkSession(@SessionAttribute(name = "user", required = false) String user) {
        Map<String, Object> response = new HashMap<>();
        response.put("user", user); // will be null if not logged in
        response.put("status", user != null ? "loggedIn" : "notLoggedIn");
        return ResponseEntity.ok(response);
    }
}
