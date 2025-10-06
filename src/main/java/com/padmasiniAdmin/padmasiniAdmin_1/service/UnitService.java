package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;

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

    // =============================
    // 🔹 Add Unit (with S3 URL handling)
    // =============================
    public void addUnit(WrapperUnit data) {
        System.out.println("📥 Received addUnit request");
        System.out.println("🧩 RootUnitId: " + data.getRootUnitId());
        System.out.println("🧩 ParentId: " + data.getParentId());
        System.out.println("🧩 UnitName: " + data.getUnitName());
        System.out.println("🖼 Received imageUrls: " + data.getImageUrls());
        System.out.println("🎧 Received audioFileId: " + data.getAudioFileId());
        System.out.println("📹 Received aiVideoUrl: " + data.getAiVideoUrl());

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

        // ✅ Ensure non-null lists
        unit.setAudioFileId(data.getAudioFileId() != null ? data.getAudioFileId() : new ArrayList<>());
        unit.setImageUrls(data.getImageUrls() != null ? data.getImageUrls() : new ArrayList<>());
        unit.setAiVideoUrl(data.getAiVideoUrl() != null ? data.getAiVideoUrl() : "");

        System.out.println("✅ Processed unit for saving: " + unit);

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
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit added successfully");
        } else {
            System.out.println("⚠️ Parent ID not found");
        }
    }

    // =============================
    // 🔹 Update Unit
    // =============================
    public void updateUnit(WrapperUnit data) {
        System.out.println("✏️ Received updateUnit request for: " + data.getUnitName());
        System.out.println("🖼 Updating imageUrls: " + data.getImageUrls());

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
            System.out.println("✅ Root unit updated with new media URLs");
        } else if (root.getUnits() != null) {
            for (Unit unit : root.getUnits()) {
                if (updateParent(unit, data.getParentId(), data)) {
                    updated = true;
                    break;
                }
            }
        }

        if (updated) {
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit updated successfully with new imageUrls/audio/video");
        } else {
            System.out.println("⚠️ Parent ID not found");
        }
    }

    // =============================
    // 🔹 Delete Unit
    // =============================
    public void deleteUnit(WrapperUnit data) {
        System.out.println("🗑️ Deleting unit with ID: " + data.getParentId());

        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found");
            return;
        }

        boolean deleted = false;

        if (root.getId().equals(data.getParentId())) {
            if (root.getUnits() != null) {
                for (Unit u : root.getUnits()) {
                    deleteAllFiles(u);
                }
            }
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(root.getId())),
                    UnitRequest.class, data.getSubjectName());
            System.out.println("🗑️ Root unit deleted");
            return;
        }

        if (root.getUnits() != null) {
            deleted = removeUnitById(root.getUnits(), data.getParentId());
        }

        if (!deleted && root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                if (deleteFromSubUnits(u, data.getParentId())) {
                    deleted = true;
                    break;
                }
            }
        }

        if (deleted) {
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit deleted successfully");
        } else {
            System.out.println("⚠️ Parent ID not found");
        }
    }

    // =============================
    // 🔹 Recursive Helpers
    // =============================
    private boolean insertIntoParent(Unit current, String targetParentId, Unit newUnit) {
        if (targetParentId.equals(current.getId())) {
            current.getUnits().add(newUnit);
            System.out.println("📦 Inserted subunit under: " + current.getUnitName());
            System.out.println("🖼 Subunit image URLs: " + newUnit.getImageUrls());
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
            System.out.println("✅ Updated unit " + current.getUnitName() + " with media URLs: " + current.getImageUrls());
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
        try (S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build());
            System.out.println("🗑️ Deleted from S3: " + fileKey);
        } catch (Exception e) {
            System.err.println("❌ S3 delete failed: " + e.getMessage());
        }
    }

    private void deleteAllFiles(Unit unit) {
        if (unit.getAudioFileId() != null) {
            for (String audioUrl : unit.getAudioFileId()) deleteFileFromS3(audioUrl);
        }
        if (unit.getImageUrls() != null) {
            for (String imageUrl : unit.getImageUrls()) deleteFileFromS3(imageUrl);
        }
        if (unit.getUnits() != null) {
            for (Unit sub : unit.getUnits()) deleteAllFiles(sub);
        }
    }

    // =============================
    // 🔹 Utility
    // =============================
    public UnitRequest getById(String id, String collectionName, String dbName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        try {
            return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
        } catch (IllegalArgumentException e) {
            return mongoTemplate.findById(id, UnitRequest.class, collectionName);
        }
    }
}
