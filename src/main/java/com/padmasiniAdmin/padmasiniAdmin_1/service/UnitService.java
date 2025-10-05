package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UnitService {

    @Autowired
    private UnitRepository unitRepository;

    public List<Unit> getAllUnit(String dbname, String rootId, String parentId) {
        return unitRepository.findByDbnameAndRootIdAndParentId(dbname, rootId, parentId);
    }

    public Unit addNewHeadUnit(WrapperUnit wrapperUnit) {
        Unit unit = wrapperUnit.getUnit();
        unit.setAssignTest(false);
        return unitRepository.save(unit);
    }

    public Unit updateHeadUnitName(WrapperUnit wrapperUnit, String id) {
        Unit unit = wrapperUnit.getUnit();
        Optional<Unit> existing = unitRepository.findById(id);
        if (existing.isPresent()) {
            Unit existingUnit = existing.get();
            existingUnit.setUnitName(unit.getUnitName());
            return unitRepository.save(existingUnit);
        }
        return null;
    }

    public void deleteHeadUnit(WrapperUnit wrapperUnit) {
        Unit unit = wrapperUnit.getUnit();
        unitRepository.deleteById(unit.getId());
    }

    public Unit addNewSubsection(WrapperUnit wrapperUnit) {
        Unit unit = wrapperUnit.getUnit();
        unit.setAssignTest(false);
        return unitRepository.save(unit);
    }

    public Unit updateUnit(WrapperUnit wrapperUnit) {
        Unit unit = wrapperUnit.getUnit();
        Optional<Unit> existing = unitRepository.findById(unit.getId());
        if (existing.isPresent()) {
            Unit existingUnit = existing.get();
            existingUnit.setUnitName(unit.getUnitName());
            existingUnit.setImageUrls(unit.getImageUrls());
            existingUnit.setAiVideoUrl(unit.getAiVideoUrl());
            existingUnit.setAudioFileId(unit.getAudioFileId());
            existingUnit.setExplanation(unit.getExplanation());
            return unitRepository.save(existingUnit);
        }
        return null;
    }
}
