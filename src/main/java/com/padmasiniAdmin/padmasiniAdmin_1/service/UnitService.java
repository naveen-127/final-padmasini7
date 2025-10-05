package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.*;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.types.ObjectId;
import java.util.*;

@Service
public class UnitService {

    @Autowired
    private MongoClient mongoClient;

    // ----------------------------------------------------------------------
    // ✅ Add new Unit (Subtopic)
    // ----------------------------------------------------------------------
    public Map<String, Object> addUnit(WrapperUnitRequest data) {
        Map<String, Object> response = new HashMap<>();

        if (data == null) {
            System.out.println("❌ addUnit: data is null");
            response.put("error", "Invalid payload");
            return response;
        }

        boolean assignTest = false;
        if (data.getParentId() != null && data.getParentId().equals(data.getRootUnitId())) {
            assignTest = true;
            System.out.println("🟢 Adding top-level unit");
        }

        Unit unit = new Unit(assignTest);
        unit.setId(new ObjectId().toHexString());
        unit.setUnitName(data.getUnitName());
        unit.setExplanation(data.getExplanation());
        unit.setParentId(data.getParentId());

        // ✅ Handle audioFileId
        if (data.getAudioFileId() != null && !data.getAudioFileId().isEmpty()) {
            unit.setAudioFileId(new ArrayList<>(data.getAudioFileId()));
        } else {
            unit.setAudioFileId(new ArrayList<>());
        }

        // ✅ Handle keepAudioFileIds (retain old ones)
        if (data.getKeepAudioFileIds() != null && !data.getKeepAudioFileIds().isEmpty()) {
            if (unit.getAudioFileId() == null) unit.setAudioFileId(new ArrayList<>());
            unit.getAudioFileId().addAll(data.getKeepAudioFileIds());
        }

        // ✅ Handle imageUrls
        if (data.getImageUrls() != null && !data.getImageUrls().isEmpty()) {
            unit.setImageUrls(new ArrayList<>(data.getImageUrls()));
        } else {
            unit.setImageUrls(new ArrayList<>());
        }

        // ✅ Handle aiVideoUrl
        if (data.getAiVideoUrl() != null && !data.getAiVideoUrl().isEmpty()) {
            unit.setAiVideoUrl(data.getAiVideoUrl());
        }

        // Debug info
        System.out.println("📦 Using DB: " + data.getDbname());
        System.out.println("📘 Using Collection: " + data.getSubjectName());
        System.out.println("🔍 Looking for Root ID: " + data.getRootUnitId());

        // ✅ Fetch the root document (UnitRequest)
        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found: " + data.getRootUnitId());
            response.put("error", "Root unit not found");
            return response;
        }

        // ✅ Insert into correct parent
        boolean inserted = false;
        if (root.getId().equals(data.getParentId())) {
            root.getUnits().add(unit);
            inserted = true;
        } else if (root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                if (insertIntoParent(u, data.getParentId(), unit)) {
                    inserted = true;
                    break;
                }
            }
        }

        // ✅ Save back to DB
        if (inserted) {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Subunit inserted and saved successfully.");
            response.put("insertedSubId", unit.getId());
            response.put("message", "ok");
        } else {
            System.out.println("⚠️ Parent ID not found in subunits.");
            response.put("error", "Parent not found");
        }

        return response;
    }

    // ----------------------------------------------------------------------
    // ✅ Recursive helper to insert into correct parent
    // ----------------------------------------------------------------------
    private boolean insertIntoParent(Unit current, String parentId, Unit newUnit) {
        if (current.getId().equals(parentId)) {
            if (current.getUnits() == null) current.setUnits(new ArrayList<>());
            current.getUnits().add(newUnit);
            return true;
        }

        if (current.getUnits() != null) {
            for (Unit u : current.getUnits()) {
                if (insertIntoParent(u, parentId, newUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ----------------------------------------------------------------------
    // ✅ Fetch by ID
    // ----------------------------------------------------------------------
    public UnitRequest getById(String id, String collection, String dbname) {
        if (id == null || id.isEmpty()) {
            System.out.println("⚠️ getById: ID is null or empty");
            return null;
        }
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbname);
            return mongoTemplate.findById(id, UnitRequest.class, collection);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ----------------------------------------------------------------------
    // ✅ Get all units in a collection
    // ----------------------------------------------------------------------
    public List<UnitRequest> getAll(String collection, String dbname) {
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbname);
            return mongoTemplate.findAll(UnitRequest.class, collection);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ----------------------------------------------------------------------
    // ✅ Delete a unit or subunit recursively by ID
    // ----------------------------------------------------------------------
    public boolean deleteUnit(String dbname, String collection, String rootId, String unitId) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbname);
        UnitRequest root = mongoTemplate.findById(rootId, UnitRequest.class, collection);

        if (root == null) return false;

        if (root.getId().equals(unitId)) {
            mongoTemplate.remove(root, collection);
            return true;
        }

        boolean deleted = deleteFromParent(root.getUnits(), unitId);
        if (deleted) mongoTemplate.save(root, collection);
        return deleted;
    }

    private boolean deleteFromParent(List<Unit> units, String unitId) {
        if (units == null) return false;

        Iterator<Unit> it = units.iterator();
        while (it.hasNext()) {
            Unit u = it.next();
            if (u.getId().equals(unitId)) {
                it.remove();
                return true;
            }
            if (deleteFromParent(u.getUnits(), unitId)) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------------------------------------------------
    // ✅ Update an existing subunit by ID
    // ----------------------------------------------------------------------
    public boolean updateUnit(String dbname, String collection, String rootId, Unit updatedUnit) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbname);
        UnitRequest root = mongoTemplate.findById(rootId, UnitRequest.class, collection);

        if (root == null) return false;
        boolean updated = updateInParent(root, updatedUnit);
        if (updated) mongoTemplate.save(root, collection);
        return updated;
    }

    private boolean updateInParent(UnitRequest root, Unit updated) {
        if (root.getId().equals(updated.getId())) {
            root.setUnitName(updated.getUnitName());
            root.setExplanation(updated.getExplanation());
            root.setAudioFileId(updated.getAudioFileId());
            root.setImageUrls(updated.getImageUrls());
            root.setAiVideoUrl(updated.getAiVideoUrl());
            return true;
        }

        if (root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                if (updateInChild(u, updated)) return true;
            }
        }
        return false;
    }

    private boolean updateInChild(Unit current, Unit updated) {
        if (current.getId().equals(updated.getId())) {
            current.setUnitName(updated.getUnitName());
            current.setExplanation(updated.getExplanation());
            current.setAudioFileId(updated.getAudioFileId());
            current.setImageUrls(updated.getImageUrls());
            current.setAiVideoUrl(updated.getAiVideoUrl());
            return true;
        }

        if (current.getUnits() != null) {
            for (Unit u : current.getUnits()) {
                if (updateInChild(u, updated)) return true;
            }
        }
        return false;
    }
}
