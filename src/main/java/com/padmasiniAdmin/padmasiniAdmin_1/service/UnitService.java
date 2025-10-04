package com.padmasiniAdmin.padmasiniAdmin_1.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

@Service
public class UnitService {

    @Autowired
    private MongoClient mongoClient;

    private final Region region = Region.AP_SOUTH_1;
    private final String bucketName = "trilokinnovations-test-admin";

    // ----- Head Unit Methods -----
    private boolean headUnitExist(String dbname, String name, String collectionName) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbname);
        Query query = new Query(Criteria.where("unitName").is(name));
        return mongoTemplate.exists(query, UnitRequest.class, collectionName);
    }

    public boolean addNewHeadUnit(WrapperUnitRequest request) {
        if (!headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            UnitRequest unitRequest = request.getUnit();
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, request.getDbname());
            mongoTemplate.save(unitRequest, request.getSubjectName());
            System.out.println("Data saved to DB");
            return true;
        }
        return false;
    }

    public boolean deleteHeadUnit(WrapperUnitRequest request) {
        if (headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            String unitId = request.getUnit().getUnitName();
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, request.getDbname());
            Query query = new Query(Criteria.where("unitName").is(unitId));
            mongoTemplate.remove(query, UnitRequest.class, request.getSubjectName());
            return true;
        }
        return false;
    }

    public boolean updateHeadUnitName(WrapperUnitRequest request, String newUnitName) {
        if (headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, request.getDbname());
            Query query = new Query(Criteria.where("unitName").is(request.getUnit().getUnitName()));
            Update update = new Update().set("unitName", newUnitName);
            mongoTemplate.updateFirst(query, update, UnitRequest.class, request.getSubjectName());
            return true;
        }
        return false;
    }

    public List<UnitRequest> getAllUnit(String dbName, String subjectName, String standard) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
        Query query = new Query(Criteria.where("standard").is(standard));
        return mongoTemplate.find(query, UnitRequest.class, subjectName);
    }

    // ----- Unit Methods -----
    public void addUnit(WrapperUnit data) {
        Unit unit = new Unit(data.getParentId().equals(data.getRootUnitId())); // assignTest
        unit.setParentId((data.getParentId() == null || data.getParentId().isEmpty()) ? null : data.getParentId());
        unit.setUnitName((data.getUnitName() == null || data.getUnitName().isEmpty()) ? null : data.getUnitName());
        unit.setExplanation((data.getExplanation() == null || data.getExplanation().isEmpty()) ? null : data.getExplanation());
        unit.setAudioFileId(data.getAudioFileId());
        unit.setImageUrls(data.getImageUrls());           // ✅ store images
        unit.setAiVideoUrl(data.getAiVideoUrl());         // ✅ store AI video

        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) return;

        boolean inserted = false;
        if (root.getId().equals(data.getParentId())) {
            root.getUnits().add(unit);
            inserted = true;
        } else if (root.getUnits() != null) {
            for (Unit units : root.getUnits()) {
                if (insertIntoParent(units, data.getParentId(), unit)) {
                    inserted = true;
                    break;
                }
            }
        }

        if (inserted) {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
        }
    }

    public void updateUnit(WrapperUnit data) {
        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) return;

        boolean updated = false;
        if (root.getId().equals(data.getParentId())) {
            root.setUnitName(data.getUnitName());
            root.setExplanation(data.getExplanation());
            root.setAudioFileId(data.getAudioFileId());
            root.setImageUrls(data.getImageUrls());       // ✅ update images
            root.setAiVideoUrl(data.getAiVideoUrl());     // ✅ update AI video
            updated = true;
        } else if (root.getUnits() != null) {
            for (Unit units : root.getUnits()) {
                if (updateParent(units, data.getParentId(), data)) {
                    updated = true;
                    break;
                }
            }
        }

        if (updated) {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
        }
    }

    public void deleteUnit(WrapperUnit data) {
        UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
        if (root == null) return;

        if (root.getId().equals(data.getParentId())) {
            if (root.getUnits() != null) {
                for (Unit i : root.getUnits()) deleteAllAudioFiles(i);
            }
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, data.getDbname());
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(root.getId())), UnitRequest.class, data.getSubjectName());
            return;
        }

        boolean deleted = false;
        if (root.getUnits() != null) {
            deleted = removeUnitById(root.getUnits(), data.getParentId());
            if (!deleted) {
                for (Unit unit : root.getUnits()) {
                    if (deleteFromSubUnits(unit, data.getParentId())) {
                        deleted = true;
                        break;
                    }
                }
            }
        }

        if (deleted) {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());
        }
    }

    // ----- Helper Methods -----
    private boolean insertIntoParent(Unit current, String targetParentId, Unit newUnit) {
        if (current.getId() != null && current.getId().equals(targetParentId)) {
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
        if (current.getId() != null && current.getId().equals(targetParentId)) {
            current.setUnitName(data.getUnitName());
            current.setExplanation(data.getExplanation());
            current.setAudioFileId(data.getAudioFileId());
            current.setImageUrls(data.getImageUrls());     // ✅ update images
            current.setAiVideoUrl(data.getAiVideoUrl());   // ✅ update AI video
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
            if (unit.getId() != null && unit.getId().equals(targetId)) {
                deleteAllAudioFiles(unit);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private void deleteAllAudioFiles(Unit unit) {
        if (unit.getAudioFileId() != null) {
            for (String audioUrl : unit.getAudioFileId()) {
                try { deleteAudioFromS3(audioUrl); } catch (Exception e) { e.printStackTrace(); }
            }
        }
        if (unit.getUnits() != null) {
            for (Unit sub : unit.getUnits()) deleteAllAudioFiles(sub);
        }
    }

    private void deleteAudioFromS3(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) return;
        String fileKey = fileUrl.split(".amazonaws.com/")[1];
        try (S3Client s3 = S3Client.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create()).build()) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build();
            s3.deleteObject(deleteRequest);
        }
    }

    public UnitRequest getById(String id, String collectionName, String dbname) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbname);
        try { return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName); }
        catch (IllegalArgumentException e) { return mongoTemplate.findById(id, UnitRequest.class, collectionName); }
    }
}
