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
import java.util.Arrays;
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
    public MongoTemplate getTemplate(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    // =============================
    // 🔹 Head Unit CRUD - COMPLETELY FIXED
    // =============================
    public boolean addNewHeadUnit(WrapperUnitRequest request) {
        String unitName = request.getUnit().getUnitName();
        String dbName = request.getDbname();
        String collectionName = request.getSubjectName();
        String standard = request.getUnit().getStandard();
        
        System.out.println("📥 Add head unit request:");
        System.out.println("  - Unit Name: " + unitName);
        System.out.println("  - DB Name: " + dbName);
        System.out.println("  - Collection: " + collectionName);
        System.out.println("  - Standard: " + standard);
        
        // Define subjects that don't require standard
        List<String> subjectsWithoutStandard = Arrays.asList("NEET Previous Questions", "Formulas");
        
        // ✅ FIX: Decode URL-encoded collection name
        String decodedCollectionName;
        try {
            decodedCollectionName = java.net.URLDecoder.decode(collectionName, "UTF-8");
            System.out.println("  - Decoded Collection: " + decodedCollectionName);
        } catch (java.io.UnsupportedEncodingException e) {
            decodedCollectionName = collectionName;
            System.out.println("  - Could not decode collection name, using as-is");
        }
        
        MongoTemplate mongoTemplate = getTemplate(dbName);
        
        // Check if unit already exists with proper query
        boolean exists = false;
        Query checkQuery;
        
        // ✅ FIX: Check if this is a special subject WITHOUT standard
        boolean isSubjectWithoutStandard = subjectsWithoutStandard.contains(decodedCollectionName);
        
        if (isSubjectWithoutStandard) {
            // For subjects without standard, check only by unitName
            checkQuery = new Query(Criteria.where("unitName").is(unitName));
            System.out.println("ℹ️ Checking for existing unit in subject without standard: " + decodedCollectionName);
        } else {
            // For other subjects, check by both unitName and standard
            checkQuery = new Query(Criteria.where("unitName").is(unitName)
                    .and("standard").is(standard));
            System.out.println("ℹ️ Checking for existing unit with standard: " + standard);
        }
        
        // ✅ FIX: Use the original collectionName for MongoDB operations
        exists = mongoTemplate.exists(checkQuery, UnitRequest.class, collectionName);
        
        if (!exists) {
            // ✅ IMPORTANT: Create a NEW UnitRequest object with proper structure
            UnitRequest newHeadUnit = createProperHeadUnit(unitName, standard, isSubjectWithoutStandard);
            
            // Save the new head unit
            mongoTemplate.save(newHeadUnit, collectionName);
            System.out.println("✅ Head unit saved successfully with ID: " + newHeadUnit.getId());
            return true;
        }
        
        System.out.println("⚠️ Head unit already exists");
        return false;
    }
    // Helper method to create a proper head unit
  

    public boolean deleteHeadUnit(WrapperUnitRequest request) {
        String unitName = request.getUnit().getUnitName();
        String dbName = request.getDbname();
        String collectionName = request.getSubjectName();
        String standard = request.getUnit().getStandard();
        
        System.out.println("🗑️ Delete head unit request:");
        System.out.println("  - Unit Name: " + unitName);
        System.out.println("  - DB Name: " + dbName);
        System.out.println("  - Collection: " + collectionName);
        System.out.println("  - Standard: " + standard);
        
        // Define subjects that don't require standard
        List<String> subjectsWithoutStandard = Arrays.asList("NEET Previous Questions", "Formulas");
        
        // ✅ FIX: Decode collection name
        String decodedCollectionName;
        try {
            decodedCollectionName = java.net.URLDecoder.decode(collectionName, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            decodedCollectionName = collectionName;
        }
        
        // Prepare query based on whether standard is required
        Query query;
        if (subjectsWithoutStandard.contains(decodedCollectionName)) {
            // For subjects without standard, delete only by unitName
            query = new Query(Criteria.where("unitName").is(unitName));
            System.out.println("ℹ️ Deleting unit for subject without standard: " + decodedCollectionName);
        } else {
            // For other subjects, delete by both unitName and standard
            query = new Query(Criteria.where("unitName").is(unitName)
                             .and("standard").is(standard));
            System.out.println("ℹ️ Deleting unit with standard: " + standard);
        }
        
        MongoTemplate mongoTemplate = getTemplate(dbName);
        boolean exists = mongoTemplate.exists(query, UnitRequest.class, collectionName);
        
        if (exists) {
            mongoTemplate.remove(query, UnitRequest.class, collectionName);
            System.out.println("🗑️ Head unit deleted successfully");
            return true;
        }
        
        System.out.println("⚠️ Head unit not found");
        return false;
    }

    public boolean updateHeadUnitName(WrapperUnitRequest request, String newUnitName) {
        String oldUnitName = request.getUnit().getUnitName();
        String dbName = request.getDbname();
        String collectionName = request.getSubjectName();
        String standard = request.getUnit().getStandard();
        
        System.out.println("✏️ Update head unit request:");
        System.out.println("  - Old Unit Name: " + oldUnitName);
        System.out.println("  - New Unit Name: " + newUnitName);
        System.out.println("  - DB Name: " + dbName);
        System.out.println("  - Collection: " + collectionName);
        System.out.println("  - Standard: " + standard);
        
        // Define subjects that don't require standard
        List<String> subjectsWithoutStandard = Arrays.asList("NEET Previous Questions", "Formulas");
        
        // ✅ FIX: Decode collection name
        String decodedCollectionName;
        try {
            decodedCollectionName = java.net.URLDecoder.decode(collectionName, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            decodedCollectionName = collectionName;
        }
        
        // Prepare query based on whether standard is required
        Query query;
        if (subjectsWithoutStandard.contains(decodedCollectionName)) {
            // For subjects without standard, find by unitName only
            query = new Query(Criteria.where("unitName").is(oldUnitName));
            System.out.println("ℹ️ Updating unit for subject without standard: " + decodedCollectionName);
        } else {
            // For other subjects, find by both unitName and standard
            query = new Query(Criteria.where("unitName").is(oldUnitName)
                             .and("standard").is(standard));
            System.out.println("ℹ️ Updating unit with standard: " + standard);
        }
        
        MongoTemplate mongoTemplate = getTemplate(dbName);
        UnitRequest existingUnit = mongoTemplate.findOne(query, UnitRequest.class, collectionName);
        
        if (existingUnit != null) {
            Update update = new Update().set("unitName", newUnitName);
            mongoTemplate.updateFirst(query, update, UnitRequest.class, collectionName);
            System.out.println("✏️ Head unit renamed successfully");
            return true;
        }
        
        System.out.println("❌ Head unit not found");
        return false;
    }

    public List<UnitRequest> getAllUnit(String dbName, String subjectName, String standard) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        Query query = new Query(Criteria.where("standard").is(standard));
        List<UnitRequest> units = mongoTemplate.find(query, UnitRequest.class, subjectName);
        
        // Ensure all units have proper structure
        if (units != null) {
            for (UnitRequest unit : units) {
                ensureUnitStructure(unit);
            }
        }
        
        System.out.println("🔍 Found " + (units != null ? units.size() : 0) + 
                          " units for " + subjectName + " with standard: " + standard);
        
        return units != null ? units : new ArrayList<>();
    }
    
 // Update your getAllUnitWithoutStandard method to properly handle special subjects
    public List<UnitRequest> getAllUnitWithoutStandard(String dbName, String subjectName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        
        // Define subjects that don't require standard
        List<String> subjectsWithoutStandard = Arrays.asList("NEET Previous Questions", "Formulas");
        
        // ✅ FIX: Decode subject name if needed
        String decodedSubjectName;
        try {
            decodedSubjectName = java.net.URLDecoder.decode(subjectName, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            decodedSubjectName = subjectName;
        }
        
        System.out.println("🔍 getAllUnitWithoutStandard called for: " + decodedSubjectName);
        
        // Always return all units for these subjects
        Query query = new Query();
        List<UnitRequest> units = mongoTemplate.find(query, UnitRequest.class, subjectName);
        
        System.out.println("🔍 Found " + (units != null ? units.size() : 0) + 
                          " units for " + subjectName + " (no standard filter)");
        
        // Ensure all units have proper structure
        if (units != null) {
            for (UnitRequest unit : units) {
                ensureUnitStructure(unit);
                // ✅ IMPORTANT: For special subjects, ensure isLesson is true
                unit.setIsLesson(true);
            }
        }
        
        return units != null ? units : new ArrayList<>();
    }
    
    // Helper method to ensure unit has proper structure
 // Helper method to create a proper head unit
    private UnitRequest createProperHeadUnit(String unitName, String standard, boolean isSubjectWithoutStandard) {
        UnitRequest unit = new UnitRequest();
        unit.setId(new ObjectId().toString());
        unit.setUnitName(unitName);
        
        if (isSubjectWithoutStandard) {
            // For subjects without standard
            unit.setStandard(null);
            unit.setIsLesson(true); // ✅ Set isLesson to true
            System.out.println("ℹ️ Creating head unit for subject without standard");
        } else {
            // For subjects with standard
            unit.setStandard(standard);
            unit.setIsLesson(true); // ✅ Set isLesson to true
            System.out.println("ℹ️ Creating head unit with standard: " + standard);
        }
        
        // Initialize all arrays to empty
        unit.setUnits(new ArrayList<>());
        unit.setExplanation("");
        unit.setImageUrls(new ArrayList<>());
        unit.setAudioFileId(new ArrayList<>());
        unit.setTags(new ArrayList<>());
        unit.setAiVideoUrl("");
        
        System.out.println("✅ Created proper head unit structure with isLesson: true");
        return unit;
    }

    // Helper method to ensure unit has proper structure
    private void ensureUnitStructure(UnitRequest unit) {
        if (unit == null) return;
        
        System.out.println("🔧 Ensuring structure for unit: " + unit.getUnitName());
        
        if (unit.getUnits() == null) {
            unit.setUnits(new ArrayList<>());
            System.out.println("  - Initialized units array");
        }
        if (unit.getImageUrls() == null) {
            unit.setImageUrls(new ArrayList<>());
            System.out.println("  - Initialized imageUrls array");
        }
        if (unit.getAudioFileId() == null) {
            unit.setAudioFileId(new ArrayList<>());
            System.out.println("  - Initialized audioFileId array");
        }
        if (unit.getTags() == null) {
            unit.setTags(new ArrayList<>());
            System.out.println("  - Initialized tags array");
        }
        if (unit.getIsLesson() == null) {
            // Default to true for head units
            unit.setIsLesson(true);
            System.out.println("  - Set isLesson to true");
        }
        if (unit.getExplanation() == null) {
            unit.setExplanation("");
            System.out.println("  - Initialized explanation");
        }
        if (unit.getAiVideoUrl() == null) {
            unit.setAiVideoUrl("");
            System.out.println("  - Initialized aiVideoUrl");
        }
    }

    // =============================
    // 🔹 Add Unit (for subtopics) - DIFFERENT from head units
    // =============================
 // =============================
 // 🔹 Add Unit (return inserted subunit ID) - FIXED for special subjects
 // =============================
 // =============================
    // 🔹 Add Unit (return inserted subunit ID) - FIXED for Special Subjects
    // =============================
    public String addUnit(WrapperUnit data) {
        System.out.println("📥 Received addUnit request");
        System.out.println("🧩 RootUnitId: " + data.getRootId());
        System.out.println("🧩 ParentId: " + data.getParentId());
        System.out.println("🧩 UnitName: " + data.getUnitName());

        String subjectName = data.getSubjectName();
        
        // 1. Fetch the Root document to modify its nested hierarchy
        UnitRequest root = getById(data.getRootId(), subjectName, data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found");
            return null;
        }

        // 2. Create the new Subtopic object (Unit)
        Unit unit = new Unit();
        unit.setId(new ObjectId().toString());
        unit.setParentId(data.getParentId());
        unit.setUnitName(data.getUnitName());
        unit.setExplanation(data.getExplanation());
        
        if (data.getTableData() != null && !data.getTableData().isEmpty()) {
            unit.setTableData(data.getTableData());
            unit.setRows(data.getRows() != null ? data.getRows() : 0);
            unit.setCols(data.getCols() != null ? data.getCols() : 0);
            unit.setShowMatches(data.getShowMatches() != null ? data.getShowMatches() : false);
        } else {
            unit.setTableData(new ArrayList<>());
            unit.setRows(0);
            unit.setCols(0);
            unit.setShowMatches(false);
        }
        
        // Initialize and populate Image URLs
        if (data.getImageUrls() != null && !data.getImageUrls().isEmpty()) {
            unit.setImageUrls(new ArrayList<>(data.getImageUrls()));
        } else {
            unit.setImageUrls(new ArrayList<>());
        }
        
        // Initialize and populate Audio files
        if (data.getAudioFileId() != null && !data.getAudioFileId().isEmpty()) {
            unit.setAudioFileId(new ArrayList<>(data.getAudioFileId()));
        } else {
            unit.setAudioFileId(new ArrayList<>());
        }

        // Initialize and populate Tags
        if (data.getTags() != null && !data.getTags().isEmpty()) {
            unit.setTags(new ArrayList<>(data.getTags()));
        } else {
            unit.setTags(new ArrayList<>());
        }
        
        unit.setAiVideoUrl(data.getAiVideoUrl() != null ? data.getAiVideoUrl() : "");
        unit.setUnits(new ArrayList<>()); // initialize nested units list for this subtopic

        boolean inserted = false;

        // 3. Recursive Nesting Logic
        // If the ParentId matches the RootId, add directly to the root document's units list
        if (root.getId().equals(data.getParentId())) {
            if (root.getUnits() == null) root.setUnits(new ArrayList<>());
            root.getUnits().add(unit);
            inserted = true;
        } else {
            // Otherwise, search recursively through the root's children to find the correct parent
            inserted = insertIntoParent(root.getUnits(), data.getParentId(), unit);
        }

        if (!inserted) {
            System.out.println("⚠️ Parent ID not found in tree structure");
            return null;
        }

        // 4. Save the updated root document back to the collection
        MongoTemplate mongoTemplate = getTemplate(data.getDbname());
        mongoTemplate.save(root, subjectName);
        System.out.println("✅ Subtopic '" + unit.getUnitName() + "' nested successfully in " + subjectName);

        return unit.getId();
    }
    // =============================
    // 🔹 Recursive insertion helper for subtopics
    // =============================
    private boolean insertIntoParent(List<Unit> units, String targetParentId, Unit newUnit) {
        if (units == null) return false;

        for (Unit u : units) {
            if (u.getId().equals(targetParentId)) {
                if (u.getUnits() == null) u.setUnits(new ArrayList<>());
                u.getUnits().add(newUnit);
                System.out.println("✅ Found parent and added subtopic");
                return true;
            }
            if (u.getUnits() != null && !u.getUnits().isEmpty()) {
                if (insertIntoParent(u.getUnits(), targetParentId, newUnit)) return true;
            }
        }

        return false;
    }

    // =============================
    // 🔹 Update Unit (for subtopics)
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

        // Check if root is the target (shouldn't happen for subtopics)
        if (root.getId().equals(data.getParentId())) {
            // This is updating a head unit via subtopic API - shouldn't happen
            System.out.println("⚠️ Warning: Updating head unit via subtopic API");
            root.setUnitName(data.getUnitName());
            root.setExplanation(data.getExplanation());
            root.setImageUrls(data.getImageUrls());
            root.setAudioFileId(data.getAudioFileId());
            root.setTags(data.getTags());
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
                u.setTags(data.getTags());
                u.setAiVideoUrl(data.getAiVideoUrl());
                
                if (data.getTableData() != null) {
                    u.setTableData(data.getTableData());
                    u.setRows(data.getRows() != null ? data.getRows() : 0);
                    u.setCols(data.getCols() != null ? data.getCols() : 0);
                    u.setShowMatches(data.getShowMatches() != null ? data.getShowMatches() : false);
                }
                return true;
            }
            if (u.getUnits() != null && !u.getUnits().isEmpty()) {
                if (updateRecursive(u.getUnits(), data)) return true;
            }
        }

        return false;
    }

    // =============================
    // 🔹 Delete Unit (for subtopics)
    // =============================
    public boolean deleteUnit(WrapperUnit data) {
        System.out.println("🗑️ Deleting unit with ID: " + data.getParentId());

        UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found");
            return false;
        }

        boolean deleted = false;

        // If root is target (shouldn't happen for subtopics)
        if (root.getId().equals(data.getParentId())) {
            System.out.println("⚠️ Warning: Deleting head unit via subtopic API");
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

    // Recursive helper for deleting nested units
    private boolean deleteRecursive(List<Unit> units, String targetId) {
        if (units == null) return false;

        Iterator<Unit> iterator = units.iterator();
        while (iterator.hasNext()) {
            Unit u = iterator.next();
            if (u.getId().equals(targetId)) {
                deleteAllFiles(u);
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
 // 🔹 Move Unit (Subtopic) Up/Down
 // =============================
 public boolean moveUnit(WrapperUnit data, String direction) {
     System.out.println("🔄 Moving unit ID: " + data.getParentId() + " direction: " + direction);
     System.out.println("📊 Data: rootId=" + data.getRootId() + ", parentId=" + data.getParentId() 
                       + ", db=" + data.getDbname() + ", subject=" + data.getSubjectName());
     
     UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
     if (root == null) {
         System.out.println("❌ Root unit not found");
         return false;
     }
     
     boolean moved = false;
     
     // First, find which list contains our target unit
     List<Unit> targetList = findUnitList(root, data.getParentId());
     
     if (targetList == null) {
         System.out.println("❌ Unit not found in any list");
         return false;
     }
     
     System.out.println("✅ Found unit in list with size: " + targetList.size());
     
     // Move within the found list
     moved = moveInList(targetList, data.getParentId(), direction);
     
     if (moved) {
         MongoTemplate mongoTemplate = getTemplate(data.getDbname());
         mongoTemplate.save(root, data.getSubjectName());
         System.out.println("✅ Unit moved successfully");
     } else {
         System.out.println("⚠️ Unit not found or cannot be moved in the list");
     }
     
     return moved;
 }

 private List<Unit> findUnitList(UnitRequest root, String targetId) {
     System.out.println("🔍 Searching for unit with ID: " + targetId);
     
     // First check root's direct children
     if (root.getUnits() != null) {
         for (Unit unit : root.getUnits()) {
             if (unit.getId().equals(targetId)) {
                 System.out.println("✅ Found as direct child of root");
                 return root.getUnits();
             }
         }
         
         // Recursively search nested units
         for (Unit unit : root.getUnits()) {
             List<Unit> result = findUnitListRecursive(unit, targetId);
             if (result != null) return result;
         }
     }
     
     return null;
 }

 private List<Unit> findUnitListRecursive(Unit parent, String targetId) {
     // Check if this parent contains the target
     if (parent.getUnits() != null) {
         // First check direct children
         for (Unit unit : parent.getUnits()) {
             if (unit.getId().equals(targetId)) {
                 System.out.println("✅ Found as child of unit: " + parent.getUnitName());
                 return parent.getUnits();
             }
         }
         
         // Then check deeper nesting
         for (Unit child : parent.getUnits()) {
             List<Unit> result = findUnitListRecursive(child, targetId);
             if (result != null) return result;
         }
     }
     
     return null;
 }

 private boolean moveInList(List<Unit> units, String targetId, String direction) {
     if (units == null || units.isEmpty()) {
         System.out.println("⚠️ Unit list is null or empty");
         return false;
     }
     
     System.out.println("📊 List contains " + units.size() + " units");
     for (int i = 0; i < units.size(); i++) {
         System.out.println("  [" + i + "] " + units.get(i).getUnitName() + " (ID: " + units.get(i).getId() + ")");
     }
     
     // Find the index of the target unit
     int currentIndex = -1;
     for (int i = 0; i < units.size(); i++) {
         if (units.get(i).getId().equals(targetId)) {
             currentIndex = i;
             System.out.println("🎯 Target found at index: " + currentIndex);
             break;
         }
     }
     
     if (currentIndex == -1) {
         System.out.println("❌ Target unit not found in list");
         return false;
     }
     
     if ("up".equals(direction)) {
         // Move up
         if (currentIndex > 0) {
             System.out.println("⬆️ Moving from position " + currentIndex + " to " + (currentIndex - 1));
             Unit temp = units.get(currentIndex - 1);
             units.set(currentIndex - 1, units.get(currentIndex));
             units.set(currentIndex, temp);
             return true;
         } else {
             System.out.println("⚠️ Cannot move up - already at position 0");
         }
     } else if ("down".equals(direction)) {
         // Move down
         if (currentIndex < units.size() - 1) {
             System.out.println("⬇️ Moving from position " + currentIndex + " to " + (currentIndex + 1));
             Unit temp = units.get(currentIndex + 1);
             units.set(currentIndex + 1, units.get(currentIndex));
             units.set(currentIndex, temp);
             return true;
         } else {
             System.out.println("⚠️ Cannot move down - already at last position");
         }
     }
     
     return false;
 }

 // =============================
 // 🔹 Move Test Up/Down
 // =============================
 public boolean moveTest(String rootId, String parentId, String testName, String direction, 
                        String dbName, String subjectName) {
     System.out.println("🔄 Moving test '" + testName + "' direction: " + direction);
     System.out.println("📊 Params: rootId=" + rootId + ", parentId=" + parentId 
                       + ", db=" + dbName + ", subject=" + subjectName);
     
     UnitRequest root = getById(rootId, subjectName, dbName);
     if (root == null) {
         System.out.println("❌ Root unit not found");
         return false;
     }
     
     // Find the parent unit that contains the tests
     Unit parentUnit = findParentUnit(root, parentId);
     List<com.padmasiniAdmin.padmasiniAdmin_1.model.MotherMCQTest> tests = null;
     
     if (parentUnit != null) {
         tests = parentUnit.getTest();
     } else if (root.getId().equals(parentId)) {
         tests = root.getTest();
     }
     
     if (tests == null || tests.isEmpty()) {
         System.out.println("❌ No tests found");
         return false;
     }
     
     System.out.println("✅ Found " + tests.size() + " tests");
     
     // Find the test index
     int currentIndex = -1;
     for (int i = 0; i < tests.size(); i++) {
         com.padmasiniAdmin.padmasiniAdmin_1.model.MotherMCQTest test = tests.get(i);
         if (test.getTestName() != null && test.getTestName().equals(testName)) {
             currentIndex = i;
             System.out.println("🎯 Test found at index: " + currentIndex);
             break;
         }
     }
     
     if (currentIndex == -1) {
         System.out.println("❌ Test not found in list");
         return false;
     }
     
     boolean moved = false;
     if ("up".equals(direction)) {
         if (currentIndex > 0) {
             System.out.println("⬆️ Moving test up from position " + currentIndex + " to " + (currentIndex - 1));
             com.padmasiniAdmin.padmasiniAdmin_1.model.MotherMCQTest temp = tests.get(currentIndex - 1);
             tests.set(currentIndex - 1, tests.get(currentIndex));
             tests.set(currentIndex, temp);
             moved = true;
         } else {
             System.out.println("⚠️ Cannot move up - already at position 0");
         }
     } else if ("down".equals(direction)) {
         if (currentIndex < tests.size() - 1) {
             System.out.println("⬇️ Moving test down from position " + currentIndex + " to " + (currentIndex + 1));
             com.padmasiniAdmin.padmasiniAdmin_1.model.MotherMCQTest temp = tests.get(currentIndex + 1);
             tests.set(currentIndex + 1, tests.get(currentIndex));
             tests.set(currentIndex, temp);
             moved = true;
         } else {
             System.out.println("⚠️ Cannot move down - already at last position");
         }
     }
     
     if (moved) {
         // Update the tests in the parent
         if (parentUnit != null) {
             parentUnit.setTest(tests);
         } else {
             root.setTest(tests);
         }
         
         MongoTemplate mongoTemplate = getTemplate(dbName);
         mongoTemplate.save(root, subjectName);
         System.out.println("✅ Test moved and saved successfully");
         return true;
     }
     
     return false;
 }

 private Unit findParentUnit(UnitRequest root, String parentId) {
     if (root.getUnits() != null) {
         for (Unit unit : root.getUnits()) {
             if (unit.getId().equals(parentId)) {
                 return unit;
             }
             
             Unit found = findParentUnitRecursive(unit, parentId);
             if (found != null) return found;
         }
     }
     
     return null;
 }

 private Unit findParentUnitRecursive(Unit unit, String parentId) {
     if (unit.getUnits() != null) {
         for (Unit child : unit.getUnits()) {
             if (child.getId().equals(parentId)) {
                 return child;
             }
             
             Unit found = findParentUnitRecursive(child, parentId);
             if (found != null) return found;
         }
     }
     
     return null;
 }
    // =============================
    // 🔹 Utility
    // =============================
    public UnitRequest getById(String id, String collectionName, String dbName) {
        MongoTemplate mongoTemplate = getTemplate(dbName);
        try {
            UnitRequest unit = mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
            if (unit != null) {
                ensureUnitStructure(unit);
            }
            return unit;
        } catch (IllegalArgumentException e) {
            UnitRequest unit = mongoTemplate.findById(id, UnitRequest.class, collectionName);
            if (unit != null) {
                ensureUnitStructure(unit);
            }
            return unit;
        }
    }
}
