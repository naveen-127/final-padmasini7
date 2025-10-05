package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UnitService {

    @Autowired
    private UnitRepository unitRepository;

    public Unit createUnit(WrapperUnitRequest data) {
        Unit unit = new Unit();

        // ✅ Basic Fields
        unit.setUnitName(data.getUnitName());
        unit.setExplanation(data.getExplanation());
        unit.setParentId(data.getParentId());
        unit.setAssignTest(data.isAssignTest());

        // ✅ Audio files
        if (data.getAudioFileId() != null && !data.getAudioFileId().isEmpty()) {
            unit.setAudioFileId(new ArrayList<>(data.getAudioFileId()));
        } else {
            unit.setAudioFileId(new ArrayList<>());
        }

        // ✅ Image URLs
        if (data.getImageUrls() != null && !data.getImageUrls().isEmpty()) {
            unit.setImageUrls(new ArrayList<>(data.getImageUrls()));
        } else {
            unit.setImageUrls(new ArrayList<>());
        }

        // ✅ AI Video URL
        if (data.getAiVideoUrl() != null && !data.getAiVideoUrl().isEmpty()) {
            unit.setAiVideoUrl(data.getAiVideoUrl());
        } else {
            unit.setAiVideoUrl(null);
        }

        // ✅ Recursive subunit creation
        List<Unit> subUnits = new ArrayList<>();
        if (data.getUnits() != null && !data.getUnits().isEmpty()) {
            for (WrapperUnitRequest subData : data.getUnits()) {
                Unit subUnit = createUnit(subData);
                subUnits.add(subUnit);
            }
        }
        unit.setUnits(subUnits);

        // ✅ Save to MongoDB
        return unitRepository.save(unit);
    }

    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }

    public Optional<Unit> getUnitById(String id) {
        return unitRepository.findById(id);
    }

    public Unit updateUnit(String id, WrapperUnitRequest data) {
        Optional<Unit> optionalUnit = unitRepository.findById(id);
        if (optionalUnit.isEmpty()) {
            throw new RuntimeException("Unit not found with ID: " + id);
        }

        Unit unit = optionalUnit.get();

        // ✅ Update fields
        unit.setUnitName(data.getUnitName());
        unit.setExplanation(data.getExplanation());
        unit.setAssignTest(data.isAssignTest());

        if (data.getAudioFileId() != null) unit.setAudioFileId(data.getAudioFileId());
        if (data.getImageUrls() != null) unit.setImageUrls(data.getImageUrls());
        if (data.getAiVideoUrl() != null) unit.setAiVideoUrl(data.getAiVideoUrl());

        // ✅ Update sub-units
        if (data.getUnits() != null && !data.getUnits().isEmpty()) {
            List<Unit> subUnits = new ArrayList<>();
            for (WrapperUnitRequest subData : data.getUnits()) {
                Unit subUnit = createUnit(subData);
                subUnits.add(subUnit);
            }
            unit.setUnits(subUnits);
        }

        return unitRepository.save(unit);
    }

    public void deleteUnit(String id) {
        unitRepository.deleteById(id);
    }
}
