package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class UnitService {

    @Autowired
    private MongoClient mongoClient;

    private final Region region = Region.AP_SOUTH_1;
    private final String bucketName = "trilokinnovations-test-admin";

    // =============================
    // 🔹 MongoDB Helper
    // =============================
    private MongoTemplate getTemplate(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    private boolean headUnitExist(String dbName, String name, String collectionName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        Query query = new Query(Criteria.where("unitName").is(name));
        return mongoTemplate.exists(query, UnitRequest.class, collectionName);
    }

    // =============================
    // 🔹 Head Unit CRUD
    // =============================
    public boolean addNewHeadUnit(WrapperUnitRequest request) {
        if (!headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            MongoTemplate mongoTemplate = getTemplate(request.getDbname());
            mongoTemplate.save(request.getUnit(), request.getSubjectName());
            System.out.println("✅ Head unit saved");
            return true;
        }
        System.out.println("⚠️ Head unit already exists");
        return false;
    }

    public boolean updateHeadUnitName(WrapperUnitRequest request, String newUnitName) {
        if (headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            MongoTemplate mongoTemplate = getTemplate(request.getDbname());
            Query query = new Query(Criteria.where("unitName").is(request.getUnit().getUnitName()));
            Update update = new Update().set("unitName", newUnitName);
            mongoTemplate.updateFirst(query, update, UnitRequest.class, request.getSubjectName());
            System.out.println("✏️ Head unit renamed");
            return true;
        }
        System.out.println("❌ Head unit not found");
        return false;
    }

    public boolean deleteHeadUnit(WrapperUnitRequest request) {
        if (headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            MongoTemplate mongoTemplate = getTemplate(request.getDbname());
            Query query = new Query(Criteria.where("unitName").is(request.getUnit().getUnitName()));
            mongoTemplate.remove(query, UnitRequest.class, request.getSubjectName());
            System.out.println("🗑️ Head unit deleted");
            return true;
        }
        System.out.println("⚠️ Head unit not found");
        return false;
    }

    public List<UnitRequest> getAllUnit(String dbName, String subjectName, String standard) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        Query query = new Query(Criteria.where("standard").is(standard));
        return mongoTemplate.find(query, UnitRequest.class, subjectName);
    }

    // =============================
    // 🔹 Add Unit (with S3 URL handling)
    // =============================
    public void addUnit(WrapperUnit data) {
        System.out.println("📥 Received addUnit request");
        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found");
            return;
        }

        boolean assignTest = data.getParentId().equals(data.getRootUnitId());
        Unit unit = new Unit(assignTest);
        unit.setParentId(data.getParentId());
        unit.setUnitName(data.getUnitName());
        unit.setExplanation(data.getExplanation());
        unit.setAudioFileId(data.getAudioFileId() != null ? data.getAudioFileId() : new ArrayList<>());
        unit.setImageUrls(data.getImageUrls() != null ? data.getImageUrls() : new ArrayList<>());
        unit.setAiVideoUrl(data.getAiVideoUrl() != null ? data.getAiVideoUrl() : "");

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

        if (inserted) {
            getTemplate(data.getDbname()).save(root, data.getSubjectName());
            System.out.println("✅ Unit added successfully");
        } else {
            System.out.println("⚠️ Parent ID not found");
        }
    }

    // =============================
    // 🔹 Update Unit
    // =============================
    public void updateUnit(WrapperUnit data) {
        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found");
            return;
        }

        boolean updated = false;
        if (root.getId().equals(data.getParentId())) {
            root.setUnitName(data.getUnitName());
            root.setExplanation(data.getExplanation());
            root.setAudioFileId(data.getAudioFileId() != null ? data.getAudioFileId() : new ArrayList<>());
            root.setImageUrls(data.getImageUrls() != null ? data.getImageUrls() : new ArrayList<>());
            root.setAiVideoUrl(data.getAiVideoUrl() != null ? data.getAiVideoUrl() : "");
            updated = true;
        } else if (root.getUnits() != null) {
            for (Unit unit : root.getUnits()) {
                if (updateParent(unit, data.getParentId(), data)) {
                    updated = true;
                    break;
                }
            }
        }

        if (updated) {
            getTemplate(data.getDbname()).save(root, data.getSubjectName());
            System.out.println("✅ Unit updated successfully");
        } else {
            System.out.println("⚠️ Parent ID not found");
        }
    }

    // =============================
    // 🔹 Delete Unit
    // =============================
    public void deleteUnit(WrapperUnit data) {
        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) return;

        boolean deleted = false;
        if (root.getId().equals(data.getParentId())) {
            if (root.getUnits() != null) {
                for (Unit u : root.getUnits()) deleteAllFiles(u);
            }
            getTemplate(data.getDbname()).remove(Query.query(Criteria.where("_id").is(root.getId())), UnitRequest.class, data.getSubjectName());
            return;
        }

        if (root.getUnits() != null) deleted = removeUnitById(root.getUnits(), data.getParentId());
        if (!deleted && root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                if (deleteFromSubUnits(u, data.getParentId())) {
                    deleted = true;
                    break;
                }
            }
        }

        if (deleted) getTemplate(data.getDbname()).save(root, data.getSubjectName());
    }

    // =============================
    // 🔹 Recursive Helpers
    // =============================
    private boolean insertIntoParent(Unit current, String targetParentId, Unit newUnit) {
        if (targetParentId.equals(current.getId())) {
            current.getUnits().add(newUnit);
            return true;
        }
        if (current.getUnits() != null) {
            for (Unit child : current.getUnits()) {
                if (insertIntoParent(child, targetParentId, newUnit)) return true;
            }
        }
        return false;
    }

    private boolean updateParent(Unit current, String targetParentId, WrapperUnit data) {
        if (targetParentId.equals(current.getId())) {
            current.setUnitName(data.getUnitName());
            current.setExplanation(data.getExplanation());
            current.setAudioFileId(data.getAudioFileId() != null ? data.getAudioFileId() : new ArrayList<>());
            current.setImageUrls(data.getImageUrls() != null ? data.getImageUrls() : new ArrayList<>());
            current.setAiVideoUrl(data.getAiVideoUrl() != null ? data.getAiVideoUrl() : "");
            return true;
        }
        if (current.getUnits() != null) {
            for (Unit child : current.getUnits()) {
                if (updateParent(child, targetParentId, data)) return true;
            }
        }
        return false;
    }

    private boolean deleteFromSubUnits(Unit current, String targetId) {
        if (current.getUnits() != null) {
            boolean removed = removeUnitById(current.getUnits(), targetId);
            if (removed) return true;
            for (Unit child : current.getUnits()) {
                if (deleteFromSubUnits(child, targetId)) return true;
            }
        }
        return false;
    }

    private boolean removeUnitById(List<Unit> units, String targetId) {
        Iterator<Unit> iterator = units.iterator();
        while (iterator.hasNext()) {
            Unit unit = iterator.next();
            if (targetId.equals(unit.getId())) {
                deleteAllFiles(unit);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // =============================
    // 🔹 S3 File Management
    // =============================
    private void deleteFileFromS3(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) return;
        String fileKey = fileUrl.split(".amazonaws.com/")[1];
        try (S3Client s3 = S3Client.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create()).build()) {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName
