package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnitService {

    @Autowired
    private MongoClient mongoClient;

    private MongoTemplate getTemplate(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    // Get all units
    public List<Unit> getAllUnit(String dbName, String collectionName, String subjectName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        Unit root = mongoTemplate.findById(subjectName, Unit.class, collectionName);
        return root != null ? root.getUnits() : new ArrayList<>();
    }

    // Add new head unit
    public String addNewHeadUnit(WrapperUnitRequest wrapper) {
        MongoTemplate mongoTemplate = getTemplate(wrapper.getDbname());
        UnitRequest root = mongoTemplate.findById(wrapper.getRootId(), UnitRequest.class, wrapper.getSubjectName());
        if (root == null) return null;

        Unit newUnit = new Unit(wrapper.getUnitName());
        newUnit.setId(new ObjectId().toHexString());
        root.getUnits().add(newUnit);

        mongoTemplate.save(root, wrapper.getSubjectName());
        return newUnit.getId();
    }

    // Update head unit name
    public boolean updateHeadUnitName(WrapperUnitRequest wrapper, String oldName) {
        MongoTemplate mongoTemplate = getTemplate(wrapper.getDbname());
        UnitRequest root = mongoTemplate.findById(wrapper.getRootId(), UnitRequest.class, wrapper.getSubjectName());
        if (root == null) return false;

        for (Unit u : root.getUnits()) {
            if (u.getUnitName().equals(oldName)) {
                u.setUnitName(wrapper.getUnitName());
                mongoTemplate.save(root, wrapper.getSubjectName());
                return true;
            }
        }
        return false;
    }

    // Delete head unit
    public boolean deleteHeadUnit(WrapperUnitRequest wrapper) {
        MongoTemplate mongoTemplate = getTemplate(wrapper.getDbname());
        UnitRequest root = mongoTemplate.findById(wrapper.getRootId(), UnitRequest.class, wrapper.getSubjectName());
        if (root == null) return false;

        boolean removed = root.getUnits().removeIf(u -> u.getId().equals(wrapper.getUnitId()));
        if (removed) mongoTemplate.save(root, wrapper.getSubjectName());
        return removed;
    }

    // Delete nested unit
    public boolean deleteUnit(WrapperUnit wrapper) {
        MongoTemplate mongoTemplate = getTemplate(wrapper.getDbname());
        UnitRequest root = mongoTemplate.findById(wrapper.getRootId(), UnitRequest.class, wrapper.getSubjectName());
        if (root == null) return false;

        boolean deleted = deleteUnitRecursive(root.getUnits(), wrapper.getUnitId());
        if (deleted) mongoTemplate.save(root, wrapper.getSubjectName());
        return deleted;
    }

    private boolean deleteUnitRecursive(List<Unit> units, String unitId) {
        for (int i = 0; i < units.size(); i++) {
            Unit u = units.get(i);
            if (u.getId().equals(unitId)) {
                units.remove(i);
                return true;
            }
            if (deleteUnitRecursive(u.getUnits(), unitId)) return true;
        }
        return false;
    }

    // Update nested unit
    public boolean updateUnit(WrapperUnit wrapper) {
        MongoTemplate mongoTemplate = getTemplate(wrapper.getDbname());
        UnitRequest root = mongoTemplate.findById(wrapper.getRootId(), UnitRequest.class, wrapper.getSubjectName());
        if (root == null) return false;

        boolean updated = updateUnitRecursive(root.getUnits(), wrapper.getUnitId(), wrapper.getUnitName());
        if (updated) mongoTemplate.save(root, wrapper.getSubjectName());
        return updated;
    }

    private boolean updateUnitRecursive(List<Unit> units, String unitId, String newName) {
        for (Unit u : units) {
            if (u.getId().equals(unitId)) {
                u.setUnitName(newName);
                return true;
            }
            if (updateUnitRecursive(u.getUnits(), unitId, newName)) return true;
        }
        return false;
    }

    // Add nested unit
    public String addUnit(WrapperUnit wrapper) {
        MongoTemplate mongoTemplate = getTemplate(wrapper.getDbname());
        UnitRequest root = mongoTemplate.findById(wrapper.getRootId(), UnitRequest.class, wrapper.getSubjectName());
        if (root == null) return null;

        Unit newUnit = new Unit(wrapper.getUnitName());
        newUnit.setId(new ObjectId().toHexString());

        if (wrapper.getParentUnitId() == null) {
            root.getUnits().add(newUnit);
        } else {
            addUnitRecursive(root.getUnits(), wrapper.getParentUnitId(), newUnit);
        }

        mongoTemplate.save(root, wrapper.getSubjectName());
        return newUnit.getId();
    }

    private boolean addUnitRecursive(List<Unit> units, String parentId, Unit newUnit) {
        for (Unit u : units) {
            if (u.getId().equals(parentId)) {
                u.getUnits().add(newUnit);
                return true;
            }
            if (addUnitRecursive(u.getUnits(), parentId, newUnit)) return true;
        }
        return false;
    }
}
