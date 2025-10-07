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
@RequestMapping("/api") // ✅ So it matches API_BASE_URL3 = `${API_BASE_URL}/api`
public class UnitController {

    @Autowired
    private UnitService unitService;

    @GetMapping("/getAllUnits/{dbname}/{subjectName}/{standard}")
    public List<UnitRequest> getUnitsBySubject(@PathVariable String dbname,
                                               @PathVariable String subjectName,
                                               @PathVariable String standard) {
        return unitService.getAllUnit(dbname, subjectName, standard);
    }

    @PostMapping("/addNewHeadUnit")
    public ResponseEntity<Map<String, String>> addHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        if (unitService.addNewHeadUnit(request)) response.put("status", "pass");
        else response.put("status", "failed");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateHeadUnit/{newUnitName}")
    public ResponseEntity<Map<String, String>> updateHeadUnit(@RequestBody WrapperUnitRequest request,
                                                              @PathVariable String newUnitName) {
        Map<String, String> response = new HashMap<>();
        if (unitService.updateHeadUnitName(request, newUnitName)) response.put("status", "pass");
        else response.put("status", "failed");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteHeadUnit")
    public ResponseEntity<Map<String, String>> deleteHeadUnit(@RequestBody WrapperUnitRequest request) {
        Map<String, String> response = new HashMap<>();
        if (unitService.deleteHeadUnit(request)) response.put("status", "pass");
        else response.put("status", "failed");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteUnit")
    public ResponseEntity<Map<String, String>> deleteUnit(@RequestBody WrapperUnit unit) {
        unitService.deleteUnit(unit);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    // ✅ FIXED: Accept JSON payload from frontend
    @PostMapping("/addSubtopic")
    public ResponseEntity<Map<String, Object>> addSubtopic(@RequestBody WrapperUnit unit) {
        System.out.println("📥 /addSubtopic called with payload: " + unit);
        String insertedId = unitService.addUnit(unit);

        if (insertedId != null) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "insertedSubId", insertedId
            ));
        } else {
            return ResponseEntity.status(400).body(Map.of(
                "status", "failed",
                "message", "Parent or root unit not found"
            ));
        }
    }

    // ✅ FIXED: Also JSON (for update)
    @PostMapping("/updateSubsection")
    public ResponseEntity<Map<String, String>> updateSubUnit(@RequestBody WrapperUnit unit) {
        unitService.updateUnit(unit);
        return ResponseEntity.ok(Map.of("status", "updated"));
    }
}
