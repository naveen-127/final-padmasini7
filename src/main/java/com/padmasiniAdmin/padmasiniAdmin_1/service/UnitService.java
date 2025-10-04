package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;

import java.util.List;

@Service
public class UnitService {

    @Autowired
    private MongoClient mongoClient;

    private MongoTemplate getTemplate(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    public UnitRequest getById(String id, String collectionName, String dbName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        try {
            return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
        } catch (IllegalArgumentException e) {
            return mongoTemplate.findById(id, UnitRequest.class, collectionName);
        }
    }

    public void saveRoot(UnitRequest root, String dbName, String collectionName) {
        getTemplate(dbName).save(root, collectionName);
    }

    // ✅ Update or add content inside a Unit
    public void updateUnitContent(Unit unit, String explanation, List<String> audioIds,
                                  List<String> imageUrls, String aiVideoUrl) {
        if (unit == null) return;

        if (explanation != null) unit.setExplanation(explanation);
        if (audioIds != null) unit.setAudioFileId(audioIds);
        if (imageUrls != null) unit.setImageUrls(imageUrls);
        if (aiVideoUrl != null) unit.setAiVideoUrl(aiVideoUrl);
    }

    // ✅ Find unit recursively by ID
    public Unit findUnitById(UnitRequest root, String unitId) {
        if (root.getId().equals(unitId)) return null; // root is handled separately
        for (Unit u : root.getUnits()) {
            Unit found = findUnitRecursive(u, unitId);
            if (found != null) return found;
        }
        return null;
    }

    private Unit findUnitRecursive(Unit unit, String unitId) {
        if (unit.getId().equals(unitId)) return unit;
        for (Unit child : unit.getUnits()) {
            Unit found = findUnitRecursive(child, unitId);
            if (found != null) return found;
        }
        return null;
    }
}
