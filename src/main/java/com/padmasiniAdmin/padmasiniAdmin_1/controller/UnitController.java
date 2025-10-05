package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/unit")
@CrossOrigin(origins = "*")
public class UnitController {

    @Autowired
    private UnitService unitService;

    @GetMapping("/getAll")
    public List<Unit> getAllUnit(@RequestParam String dbname, @RequestParam String rootId, @RequestParam String parentId) {
        return unitService.getAllUnit(dbname, rootId, parentId);
    }

    @PostMapping("/addNewHeadUnit")
    public Unit addNewHeadUnit(@RequestPart("unit") WrapperUnit wrapperUnit) {
        return unitService.addNewHeadUnit(wrapperUnit);
    }

    @PostMapping("/updateHeadUnitName/{id}")
    public Unit updateHeadUnitName(@RequestPart("unit") WrapperUnit wrapperUnit, @PathVariable String id) {
        return unitService.updateHeadUnitName(wrapperUnit, id);
    }

    @PostMapping("/deleteHeadUnit")
    public void deleteHeadUnit(@RequestPart("unit") WrapperUnit wrapperUnit) {
        unitService.deleteHeadUnit(wrapperUnit);
    }

    @PostMapping("/addNewSubsection")
    public Unit addNewSubsection(@RequestPart("unit") WrapperUnit wrapperUnit) {
        return unitService.addNewSubsection(wrapperUnit);
    }

    @PostMapping("/updateUnit")
    public Unit updateUnit(@RequestPart("unit") WrapperUnit wrapperUnit) {
        return unitService.updateUnit(wrapperUnit);
    }
}
