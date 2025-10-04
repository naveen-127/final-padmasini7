package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unit")
public class UnitController {

    @Autowired
    private UnitService unitService;

    @GetMapping("/all")
    public List<?> getAllUnits(@RequestParam String db, @RequestParam String collection, @RequestParam String subject) {
        return unitService.getAllUnit(db, collection, subject);
    }

    @PostMapping("/head/add")
    public String addHeadUnit(@RequestBody WrapperUnitRequest wrapper) {
        return unitService.addNewHeadUnit(wrapper);
    }

    @PutMapping("/head/update")
    public boolean updateHeadUnit(@RequestBody WrapperUnitRequest wrapper, @RequestParam String oldName) {
        return unitService.updateHeadUnitName(wrapper, oldName);
    }

    @DeleteMapping("/head/delete")
    public boolean deleteHeadUnit(@RequestBody WrapperUnitRequest wrapper) {
        return unitService.deleteHeadUnit(wrapper);
    }

    @DeleteMapping("/delete")
    public boolean deleteUnit(@RequestBody WrapperUnit wrapper) {
        return unitService.deleteUnit(wrapper);
    }

    @PutMapping("/update")
    public boolean updateUnit(@RequestBody WrapperUnit wrapper) {
        return unitService.updateUnit(wrapper);
    }

    @PostMapping("/add")
    public String addUnit(@RequestBody WrapperUnit wrapper) {
        return unitService.addUnit(wrapper);
    }
}
