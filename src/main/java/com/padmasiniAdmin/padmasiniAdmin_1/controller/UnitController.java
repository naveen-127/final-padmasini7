package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.service.UnitService;

@RestController
@RequestMapping("/unit")
public class UnitController {

    @Autowired
    private UnitService unitService;

    /* ---------------------------------------------------------
     *  HEAD-UNIT ENDPOINTS
     * --------------------------------------------------------- */

    @GetMapping("/getAllUnits/{dbname}/{subjectName}/{standard}")
    public List<Unit> getUnitsBySubject(@PathVariable String dbname,
                                        @PathVariable String subjectName,
                                        @PathVariable String standard) {
        return unitService.getAllUnit(dbname, subjectName, standard);
    }

    @PostMapping("/addHeadUnit")
    public ResponseEntity<Map<String, String>> addHeadUnit(@RequestBody WrapperUnit request) {
        Map<String, String> response = new HashMap<>();
        Unit unit = convertWrapperToUnit(request);
        boolean success = unitService.addNewHeadUnit(unit, request.getDbname(), request.getSubjectName());
        response.put("status", success ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateHeadUnit")
    public ResponseEntity<Map<String, String>> updateHeadUnit(@RequestBody WrapperUnit request,
                                                              @RequestParam String newUnitName) {
        Map<String, String> response = new HashMap<>();
        Unit unit = convertWrapperToUnit(request);
        boolean success = unitService.updateHeadUnitName(unit, newUnitName, request.getDbname(), request.getSubjectName());
        response.put("status", success ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteHeadUnit")
    public ResponseEntity<Map<String, String>> deleteHeadUnit(@RequestBody WrapperUnit request) {
        Map<String, String> response = new HashMap<>();
        Unit unit = convertWrapperToUnit(request);
        boolean success = unitService.deleteHeadUnit(unit, request.getDbname(), request.getSubjectName());
        response.put("status", success ? "pass" : "failed");
        return ResponseEntity.ok(response);
    }

    /* ---------------------------------------------------------
     *  SUB-UNIT ENDPOINTS
     * --------------------------------------------------------- */

    @DeleteMapping("/deleteUnit")
    public ResponseEntity<Map<String, String>> deleteUnit(@RequestBody WrapperUnit request) {
        unitService.deleteUnit(request);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @PostMapping("/updateSubUnit")
    public ResponseEntity<Map<String, String>> updateSubUnit(@RequestBody WrapperUnit request) {
        unitService.updateUnit(request);
        return ResponseEntity.ok(Map.of("status", "updated"));
    }

    @PostMapping("/addSubUnit")
    public ResponseEntity<Map<String, String>> addSubUnit(@RequestBody WrapperUnit request) {
        unitService.addUnit(request);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    /* ---------------------------------------------------------
     *  UTILITY: Convert WrapperUnit → Unit
     * --------------------------------------------------------- */
    private Unit convertWrapperToUnit(WrapperUnit wrapper) {
        Unit unit = new Unit();
        unit.setId(wrapper.getRootUnitId() != null ? wrapper.getRootUnitId() : unit.getId());
        unit.setUnitName(wrapper.getUnitName());
        unit.setParentId(wrapper.getParentId());
        unit.setExplanation(wrapper.getExplanation());
        unit.setAudioFileId(wrapper.getAudioFileId());
        unit.setAiVideoUrl(wrapper.getAiVideoUrl());
        unit.setImageUrls(wrapper.getImageUrls());
        unit.setUnits(null); // head unit has no subunits initially
        unit.setTest(null);  // head unit tests can be added later
        return unit;
    }
}
