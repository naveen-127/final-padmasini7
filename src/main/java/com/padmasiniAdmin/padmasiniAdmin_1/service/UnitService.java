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
    // 🔹 Mongo Helper
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

    public List<UnitRequest> getAllUnit(String dbName, String subjectName, String standard) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        Query query = new Query(Criteria.where("standard").is(standard));
        return mongoTemplate.find(query, UnitRequest.class, subjectName);
    }

    // =============================
    // 🔹 Add Unit (return inserted subunit ID) - FIXED for nested subtopics
    // =============================
 // =============================
 // 🔹 Add Unit (return inserted subunit ID) - FIXED for image handling
 // =============================
 public String addUnit(WrapperUnit data) {
     System.out.println("📥 Received addUnit request");
     System.out.println("🧩 RootUnitId: " + data.getRootId());
     System.out.println("🧩 ParentId: " + data.getParentId());
     System.out.println("🧩 UnitName: " + data.getUnitName());
     System.out.println("🖼️ Image URLs: " + (data.getImageUrls() != null ? data.getImageUrls() : "null"));
     System.out.println("🎵 Audio Files: " + (data.getAudioFileId() != null ? data.getAudioFileId() : "null"));
     System.out.println("🏷️ Tags: " + (data.getTags() != null ? data.getTags() : "null"));


     UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
     if (root == null) {
         System.out.println("❌ Root unit not found");
         return null;
     }

     Unit unit = new Unit();
     unit.setId(new ObjectId().toString());
     unit.setParentId(data.getParentId());
     unit.setUnitName(data.getUnitName());
     unit.setExplanation(data.getExplanation());
     
     // ✅ FIXED: Properly handle image URLs - ensure it's never null
     if (data.getImageUrls() != null && !data.getImageUrls().isEmpty()) {
         unit.setImageUrls(new ArrayList<>(data.getImageUrls()));
         System.out.println("✅ Setting " + data.getImageUrls().size() + " image URLs");
     } else {
         unit.setImageUrls(new ArrayList<>());
         System.out.println("ℹ️ No image URLs provided, setting empty list");
     }
     
     // ✅ FIXED: Properly handle audio files - ensure it's never null
     if (data.getAudioFileId() != null && !data.getAudioFileId().isEmpty()) {
         unit.setAudioFileId(new ArrayList<>(data.getAudioFileId()));
         System.out.println("✅ Setting " + data.getAudioFileId().size() + " audio files");
     } else {
         unit.setAudioFileId(new ArrayList<>());
         System.out.println("ℹ️ No audio files provided, setting empty list");
     }

     if (data.getTags() != null && !data.getTags().isEmpty()) {
        unit.setTags(new ArrayList<>(data.getTags()));
        System.out.println("✅ Setting " + data.getTags().size() + " tags: " + data.getTags());
    } else {
        unit.setTags(new ArrayList<>());
        System.out.println("ℹ️ No tags provided, setting empty list");
    }
     
     unit.setAiVideoUrl(data.getAiVideoUrl() != null ? data.getAiVideoUrl() : "");
     unit.setUnits(new ArrayList<>()); // initialize nested units list

     boolean inserted;
     if (root.getId().equals(data.getParentId())) {
         if (root.getUnits() == null) root.setUnits(new ArrayList<>());
         root.getUnits().add(unit);
         inserted = true;
     } else {
         inserted = insertIntoParent(root.getUnits(), data.getParentId(), unit);
     }

     if (!inserted) {
         System.out.println("⚠️ Parent ID not found");
         return null;
     }

     MongoTemplate mongoTemplate = getTemplate(data.getDbname());
     mongoTemplate.save(root, data.getSubjectName());
     System.out.println("✅ Unit added successfully: " + unit.getUnitName() + " with " + unit.getImageUrls().size() + " images");

     return unit.getId();
 }

    // =============================
    // 🔹 Recursive insertion helper
    // =============================
    private boolean insertIntoParent(List<Unit> units, String targetParentId, Unit newUnit) {
        if (units == null) return false;

        for (Unit u : units) {
            if (u.getId().equals(targetParentId)) {
                if (u.getUnits() == null) u.setUnits(new ArrayList<>());
                u.getUnits().add(newUnit);
                return true;
            }
            if (u.getUnits() != null && !u.getUnits().isEmpty()) {
                if (insertIntoParent(u.getUnits(), targetParentId, newUnit)) return true;
            }
        }

        return false;
    }

 // =============================
 // 🔹 Update Unit (recursive, like addUnit)
 // =============================
 public boolean updateUnit(WrapperUnit data) {
     System.out.println("✏️ Received updateUnit request for: " + data.getUnitName());
     System.out.println("🏷️ Tags in update: " + (data.getTags() != null ? data.getTags() : "null"));


     UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
     if (root == null) {
         System.out.println("❌ Root unit not found");
         return false;
     }

     boolean updated = false;

     // Check if root is the target
     if (root.getId().equals(data.getParentId())) {
         root.setUnitName(data.getUnitName());
         root.setExplanation(data.getExplanation());
         root.setImageUrls(data.getImageUrls());
         root.setAudioFileId(data.getAudioFileId());
         root.setTags(data.getTags()); // ✅ Update tags
         root.setAiVideoUrl(data.getAiVideoUrl());
         updated = true;
     } else {
         updated = updateRecursive(root.getUnits(), data);
     }

     if (updated) {
         MongoTemplate mongoTemplate = getTemplate(data.getDbname());
         mongoTemplate.save(root, data.getSubjectName());
         System.out.println("✅ Unit updated successfully");
     } else {
         System.out.println("⚠️ Parent ID not found");
     }

     return updated;
 }

 // Recursive helper for updating nested units
 private boolean updateRecursive(List<Unit> units, WrapperUnit data) {
     if (units == null) return false;

     for (Unit u : units) {
         if (u.getId().equals(data.getParentId())) {
             u.setUnitName(data.getUnitName());
             u.setExplanation(data.getExplanation());
             u.setImageUrls(data.getImageUrls());
             u.setAudioFileId(data.getAudioFileId());
             u.setTags(data.getTags()); // ✅ Update tags
             u.setAiVideoUrl(data.getAiVideoUrl());
             return true;
         }
         if (u.getUnits() != null && !u.getUnits().isEmpty()) {
             if (updateRecursive(u.getUnits(), data)) return true;
         }
     }

     return false;
 }


//=============================
//🔹 Delete Unit (recursive, like addUnit)
//=============================
public boolean deleteUnit(WrapperUnit data) {
  System.out.println("🗑️ Deleting unit with ID: " + data.getParentId());

  UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
  if (root == null) {
      System.out.println("❌ Root unit not found");
      return false;
  }

  boolean deleted = false;

  // If root is target
  if (root.getId().equals(data.getParentId())) {
      if (root.getUnits() != null) {
          for (Unit u : root.getUnits()) deleteAllFiles(u);
      }
      MongoTemplate mongoTemplate = getTemplate(data.getDbname());
      mongoTemplate.remove(Query.query(Criteria.where("_id").is(root.getId())), UnitRequest.class, data.getSubjectName());
      System.out.println("🗑️ Root unit deleted");
      return true;
  }

  // Otherwise, recursively delete from nested units
  if (root.getUnits() != null) {
      deleted = deleteRecursive(root.getUnits(), data.getParentId());
  }

  if (deleted) {
      MongoTemplate mongoTemplate = getTemplate(data.getDbname());
      mongoTemplate.save(root, data.getSubjectName());
      System.out.println("✅ Unit deleted successfully");
  } else {
      System.out.println("⚠️ Parent ID not found");
  }

  return deleted;
}

//Recursive helper for deleting nested units
private boolean deleteRecursive(List<Unit> units, String targetId) {
  if (units == null) return false;

  Iterator<Unit> iterator = units.iterator();
  while (iterator.hasNext()) {
      Unit u = iterator.next();
      if (u.getId().equals(targetId)) {
          deleteAllFiles(u); // Optional: remove files if any
          iterator.remove();
          return true;
      }
      if (u.getUnits() != null && !u.getUnits().isEmpty()) {
          if (deleteRecursive(u.getUnits(), targetId)) return true;
      }
  }

  return false;
}


    // =============================
    // 🔹 Recursive Helpers
    // =============================
    private boolean updateParent(Unit current, String targetParentId, WrapperUnit data) {
        if (targetParentId.equals(current.getId())) {
            current.setUnitName(data.getUnitName());
            current.setExplanation(data.getExplanation());
            current.setAudioFileId(data.getAudioFileId() != null ? data.getAudioFileId() : new ArrayList<>());
            current.setImageUrls(data.getImageUrls() != null ? data.getImageUrls() : new ArrayList<>());
            current.setTags(data.getTags() != null ? data.getTags() : new ArrayList<>()); // ✅ Add tags
            current.setAiVideoUrl(data.getAiVideoUrl() != null ? data.getAiVideoUrl() : null);
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
    // 🔹 File Management (S3)
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
        } catch (Exception e) {
            System.err.println("❌ S3 delete failed: " + e.getMessage());
        }
    }

    private void deleteAllFiles(Unit unit) {
        if (unit.getAudioFileId() != null) unit.getAudioFileId().forEach(this::deleteFileFromS3);
        if (unit.getImageUrls() != null) unit.getImageUrls().forEach(this::deleteFileFromS3);
        if (unit.getUnits() != null) unit.getUnits().forEach(this::deleteAllFiles);
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
