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

    /* ---------------------------------------------------------
     *  HEAD-UNIT  MANAGEMENT
     * --------------------------------------------------------- */

    private boolean headUnitExist(String dbName, String unitName, String collectionName) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
        Query query = new Query(Criteria.where("unitName").is(unitName));
        return mongoTemplate.exists(query, Unit.class, collectionName);
    }

    public boolean addNewHeadUnit(WrapperUnitRequest request) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, request.getDbname());
        if (!headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            // convert UnitRequest → Unit
            Unit head = convertToUnit(request.getUnit());
            mongoTemplate.save(head, request.getSubjectName());
            System.out.println("✅ Head unit saved successfully.");
            return true;
        }
        System.out.println("⚠️ Head unit already exists.");
        return false;
    }

    public boolean deleteHeadUnit(WrapperUnitRequest request) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, request.getDbname());
        if (headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            Query query = new Query(Criteria.where("unitName").is(request.getUnit().getUnitName()));
            mongoTemplate.remove(query, Unit.class, request.getSubjectName());
            System.out.println("✅ Head unit deleted successfully.");
            return true;
        }
        System.out.println("⚠️ Head unit not found for deletion.");
        return false;
    }

    public boolean updateHeadUnitName(WrapperUnitRequest request, String newUnitName) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, request.getDbname());
        if (headUnitExist(request.getDbname(), request.getUnit().getUnitName(), request.getSubjectName())) {
            Query query = new Query(Criteria.where("unitName").is(request.getUnit().getUnitName()));
            Update update = new Update().set("unitName", newUnitName);
            mongoTemplate.updateFirst(query, update, Unit.class, request.getSubjectName());
            System.out.println("✅ Head unit name updated to: " + newUnitName);
            return true;
        }
        System.out.println("❌ Update failed — unit not found.");
        return false;
    }

    public List<Unit> getAllUnit(String dbName, String subjectName, String standard) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
        Query query = new Query(Criteria.where("standard").is(standard));
        return mongoTemplate.find(query, Unit.class, subjectName);
    }

    /* ---------------------------------------------------------
     *  SUB-UNIT  MANAGEMENT
     * --------------------------------------------------------- */

    public void addUnit(WrapperUnit data) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, data.getDbname());
        Unit root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());

        if (root == null) {
            System.out.println("❌ Root unit not found for addUnit()");
            return;
        }

        Unit newUnit = new Unit();
        newUnit.setParentId(data.getParentId());
        newUnit.setUnitName(data.getUnitName());
        newUnit.setExplanation(data.getExplanation());
        newUnit.setAudioFileId(defaultList(data.getAudioFileId()));
        newUnit.setAiVideoUrl(defaultList(data.getAiVideoUrl()));
        newUnit.setImageUrls(defaultList(data.getImageUrls()));

        boolean inserted = false;
        if (root.getId().equals(data.getParentId())) {
            root.getUnits().add(newUnit);
            inserted = true;
        } else if (root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                if (insertIntoParent(u, data.getParentId(), newUnit)) {
                    inserted = true;
                    break;
                }
            }
        }

        if (inserted) {
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit added successfully: " + data.getUnitName());
        } else {
            System.out.println("⚠️ Parent ID not found — unable to insert.");
        }
    }

    private boolean insertIntoParent(Unit current, String targetParentId, Unit newUnit) {
        if (current.getId() != null && current.getId().equals(targetParentId)) {
            current.getUnits().add(newUnit);
            return true;
        }
        for (Unit child : current.getUnits()) {
            if (insertIntoParent(child, targetParentId, newUnit)) return true;
        }
        return false;
    }

    public void updateUnit(WrapperUnit data) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, data.getDbname());
        Unit root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());

        if (root == null) {
            System.out.println("❌ Root unit not found for updateUnit()");
            return;
        }

        boolean updated = updateParent(root, data.getParentId(), data);
        if (updated) {
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit updated successfully: " + data.getUnitName());
        } else {
            System.out.println("⚠️ Parent ID not found for update.");
        }
    }

    private boolean updateParent(Unit current, String targetParentId, WrapperUnit data) {
        if (current.getId() != null && current.getId().equals(targetParentId)) {
            current.setUnitName(data.getUnitName());
            current.setExplanation(data.getExplanation());
            current.setAudioFileId(defaultList(data.getAudioFileId()));
            current.setAiVideoUrl(defaultList(data.getAiVideoUrl()));
            current.setImageUrls(defaultList(data.getImageUrls()));
            return true;
        }
        for (Unit child : current.getUnits()) {
            if (updateParent(child, targetParentId, data)) return true;
        }
        return false;
    }

    public void deleteUnit(WrapperUnit data) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, data.getDbname());
        Unit root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());

        if (root == null) {
            System.out.println("❌ Root unit not found for deleteUnit()");
            return;
        }

        boolean deleted = removeUnitById(root.getUnits(), data.getParentId());

        if (deleted) {
            mongoTemplate.save(root, data.getSubjectName());
            System.out.println("✅ Unit deleted successfully (with media).");
        } else {
            System.out.println("⚠️ Parent ID not found for deletion.");
        }
    }

    private boolean removeUnitById(List<Unit> units, String targetId) {
        Iterator<Unit> iterator = units.iterator();
        while (iterator.hasNext()) {
            Unit unit = iterator.next();
            if (unit.getId() != null && unit.getId().equals(targetId)) {
                deleteAllMediaFiles(unit);
                iterator.remove();
                return true;
            }
            if (removeUnitById(unit.getUnits(), targetId)) return true;
        }
        return false;
    }

    /* ---------------------------------------------------------
     *  AWS S3 MEDIA HANDLING
     * --------------------------------------------------------- */

    private void deleteAllMediaFiles(Unit unit) {
        if (unit.getAudioFileId() != null) {
            for (String audioUrl : unit.getAudioFileId()) deleteFromS3(audioUrl);
        }
        if (unit.getAiVideoUrl() != null) {
            for (String videoUrl : unit.getAiVideoUrl()) deleteFromS3(videoUrl);
        }
        for (Unit sub : unit.getUnits()) deleteAllMediaFiles(sub);
    }

    private void deleteFromS3(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) {
            System.err.println("⚠️ Invalid S3 URL: " + fileUrl);
            return;
        }
        String key = fileUrl.split(".amazonaws.com/")[1];
        try (S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3.deleteObject(req);
            System.out.println("🗑️ Deleted from S3: " + key);
        } catch (Exception e) {
            System.err.println("❌ Failed to delete: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------------
     *  FETCH UTILITIES
     * --------------------------------------------------------- */

    public Unit getById(String id, String collection, String dbName) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
        try {
            return mongoTemplate.findById(new ObjectId(id), Unit.class, collection);
        } catch (IllegalArgumentException e) {
            return mongoTemplate.findById(id, Unit.class, collection);
        }
    }

    /* ---------------------------------------------------------
     *  HELPERS
     * --------------------------------------------------------- */

    private Unit convertToUnit(UnitRequest req) {
        Unit unit = new Unit();
        unit.setUnitName(req.getUnitName());
        unit.setExplanation(req.getExplanation());
        unit.setParentId(req.getParentId());
        unit.setAudioFileId(defaultList(req.getAudioFileId()));
        unit.setImageUrls(defaultList(req.getImageUrls()));
        unit.setAiVideoUrl(defaultList(req.getAiVideoUrl()));
        unit.setUnits(req.getUnits() != null ? req.getUnits() : new ArrayList<>());
        unit.setTest(req.getTest() != null ? req.getTest() : new ArrayList<>());
        return unit;
    }

    private List<String> defaultList(List<String> list) {
        return list != null ? list : new ArrayList<>();
    }
}
