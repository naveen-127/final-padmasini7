package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
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

    // 1️⃣ Get all head units
    public List<UnitRequest> getAllUnit(String dbName, String collectionName, String subjectName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        return mongoTemplate.findAll(UnitRequest.class, collectionName);
    }

    // 2️⃣ Add new head unit
    public UnitRequest addNewHeadUnit(WrapperUnitRequest wrapper) {
        UnitRequest root = new UnitRequest();
        root.setUnitName(wrapper.getUnitName());
        root.setStandard(wrapper.getStandard());
        root.setId(wrapper.getRootId() != null ? wrapper.getRootId() : new ObjectId().toHexString());
        root.setUnits(new ArrayList<>());
        root.setAudioFileId(new ArrayList<>());
        root.setImageUrls(new ArrayList<>());
        getTemplate(wrapper.getDbname()).save(root, wrapper.getSubjectName());
        return root;
    }

    // 3️⃣ Update head unit name
    public UnitRequest updateHeadUnitName(WrapperUnitRequest wrapper, String oldName) {
        UnitRequest root = getById(wrapper.getRootId(), wrapper.getSubjectName(), wrapper.getDbname());
        if (root != null && root.getUnitName().equals(oldName)) {
            root.setUnitName(wrapper.getUnitName());
            getTemplate(wrapper.getDbname()).save(root, wrapper.getSubjectName());
        }
        return root;
    }

    // 4️⃣ Delete head unit
    public boolean deleteHeadUnit(WrapperUnitRequest wrapper) {
        UnitRequest root = getById(wrapper.getRootId(), wrapper.getSubjectName(), wrapper.getDbname());
        if (root != null) {
            getTemplate(wrapper.getDbname()).remove(root, wrapper.getSubjectName());
            return true;
        }
        return false;
    }

    // 5️⃣ Delete nested unit
    public boolean deleteUnit(WrapperUnit wrapper) {
        UnitRequest root = getById(wrapper.getRootId(), wrapper.getSubjectName(), wrapper.getDbname());
        if (root != null) {
            boolean removed = deleteUnitRecursive(root.getUnits(), wrapper.getUnitId());
            if (removed) getTemplate(wrapper.getDbname()).save(root, wrapper.getSubjectName());
            return removed;
        }
        return false;
    }

    private boolean deleteUnitRecursive(List<Unit> units, String unitId) {
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getId().equals(unitId)) {
                units.remove(i);
                return true;
            } else {
                if (deleteUnitRecursive(units.get(i).getUnits(), unitId)) return true;
            }
        }
        return false;
    }

    // 6️⃣ Update nested unit
    public boolean updateUnit(WrapperUnit wrapper) {
        UnitRequest root = getById(wrapper.getRootId(), wrapper.getSubjectName(), wrapper.getDbname());
        if (root != null) {
            boolean updated = updateUnitRecursive(root.getUnits(), wrapper);
            if (updated) getTemplate(wrapper.getDbname()).save(root, wrapper.getSubjectName());
            return updated;
        }
        return false;
    }

    private boolean updateUnitRecursive(List<Unit> units, WrapperUnit wrapper) {
        for (Unit u : units) {
            if (u.getId().equals(wrapper.getUnitId())) {
                if (wrapper.getUnitName() != null) u.setUnitName(wrapper.getUnitName());
                if (wrapper.getAudioFileId() != null) u.setAudioFileId(wrapper.getAudioFileId());
                if (wrapper.getImageUrls() != null) u.setImageUrls(wrapper.getImageUrls());
                return true;
            } else {
                if (updateUnitRecursive(u.getUnits(), wrapper)) return true;
            }
        }
        return false;
    }

    // 7️⃣ Add nested unit
    public boolean addUnit(WrapperUnit wrapper) {
        UnitRequest root = getById(wrapper.getRootId(), wrapper.getSubjectName(), wrapper.getDbname());
        if (root != null) {
            Unit newUnit = new Unit();
            newUnit.setId(wrapper.getUnitId() != null ? wrapper.getUnitId() : new ObjectId().toHexString());
            newUnit.setUnitName(wrapper.getUnitName());
            newUnit.setAudioFileId(new ArrayList<>());
            newUnit.setImageUrls(new ArrayList<>());
            return addUnitRecursive(root.getUnits(), wrapper.getParentId(), newUnit, root, wrapper);
        }
        return false;
    }

    private boolean addUnitRecursive(List<Unit> units, String parentId, Unit newUnit, UnitRequest root, WrapperUnit wrapper) {
        if (parentId == null || parentId.isEmpty()) {
            units.add(newUnit);
            getTemplate(wrapper.getDbname()).save(root, wrapper.getSubjectName());
            return true;
        }
        for (Unit u : units) {
            if (u.getId().equals(parentId)) {
                u.getUnits().add(newUnit);
                getTemplate(wrapper.getDbname()).save(root, wrapper.getSubjectName());
                return true;
            } else {
                if (addUnitRecursive(u.getUnits(), parentId, newUnit, root, wrapper)) return true;
            }
        }
        return false;
    }

    // Helper: get root by id
    public UnitRequest getById(String id, String collectionName, String dbname) {
        MongoTemplate mongoTemplate = getTemplate(dbname);
        try {
            return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
        } catch (IllegalArgumentException e) {
            return mongoTemplate.findById(id, UnitRequest.class, collectionName);
        }
    }
}
