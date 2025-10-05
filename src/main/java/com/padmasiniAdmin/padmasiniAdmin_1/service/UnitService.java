package com.padmasiniAdmin.padmasiniAdmin_1.service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.UnitRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
@Service
public class UnitService {
	 @Autowired
	    private GridFsTemplate gridFsTemplate;
//    @Autowired
//    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoClient mongoClient;
    private final Region region = Region.AP_SOUTH_1;
    private final String bucketName = "trilokinnovations-test-admin";
    private boolean headUnitExist(String dbname,String name,String collectionName) {
    	
    	 MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbname);
 	    Query query = new Query(Criteria.where("unitName").is(name));
 	    return mongoTemplate.exists(query, UnitRequest.class, collectionName);
    }
    public boolean addNewHeadUnit(WrapperUnitRequest request) {
    	if(!headUnitExist(request.getDbname(),request.getUnit().getUnitName(),request.getSubjectName())) {
    		UnitRequest unitRequest=request.getUnit();
        	MongoTemplate mongoTemplate=new MongoTemplate(mongoClient,request.getDbname());
    		mongoTemplate.save(unitRequest,request.getSubjectName());
    		System.out.println("data saved look db");
    		return true;
    	}
    	return false;
    }
    public boolean deleteHeadUnit(WrapperUnitRequest request) {
    	if(headUnitExist(request.getDbname(),request.getUnit().getUnitName(),request.getSubjectName())) {
    		String unitId=request.getUnit().getUnitName();
        	MongoTemplate mongoTemplate=new MongoTemplate(mongoClient,request.getDbname());
        	Query query = new Query(Criteria.where("unitName").is(unitId));
            mongoTemplate.remove(query, UnitRequest.class, request.getSubjectName());
            return true;
    	}
    	return false;
    }
    public boolean updateHeadUnitName(WrapperUnitRequest request,String newUnitName) {
    	 if(headUnitExist(request.getDbname(),request.getUnit().getUnitName(),request.getSubjectName())) {
    		 String dbName = request.getDbname();
     	    String collectionName = request.getSubjectName();
     	    String unitId = request.getUnit().getUnitName();
     	    MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
     	    Query query = new Query(Criteria.where("unitName").is(unitId));
     	    Update update = new Update().set("unitName", newUnitName);
     	    mongoTemplate.updateFirst(query, update, UnitRequest.class, collectionName);
     	    return true;
    	 }
    	 System.out.println("not added failed here");
    	 return false;
    }
    public List<UnitRequest> getAllUnit(String dbName, String subjectName, String standard) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);

        Query query = new Query(Criteria.where("standard").is(standard));
        return mongoTemplate.find(query, UnitRequest.class, subjectName);
    }

    
    
    
    
    
    
    public void deleteUnit(WrapperUnit data) {
    	UnitRequest root = getById(data.getRootUnitId(),data.getSubjectName(),data.getDbname());
        if (root == null) {
            System.out.println("Root unit not found");
            return;
        }
        System.out.println("before: "+root);
        boolean deleted = false;
        if (root.getId().equals(data.getParentId())) {
        	 if(root.getUnits()!=null) {
        		 for(Unit i:root.getUnits()) {
        			 deleteAllAudioFiles(i);
        		 }
        	 }
            // If root and parent are the same, delete the whole document using Query
        	MongoTemplate mongoTemplate=new MongoTemplate(mongoClient,data.getDbname());
        	mongoTemplate.remove(
                Query.query(Criteria.where("_id").is(root.getId())),
                UnitRequest.class,
                data.getSubjectName()
            );
            System.out.println("Root unit deleted.");
            return;
        }
        if (root.getUnits() != null) {
            deleted = removeUnitById(root.getUnits(), data.getParentId());
        }

        // Case 2: Recursive deletion from nested units
        if (!deleted && root.getUnits() != null) {
            for (Unit unit : root.getUnits()) {
                if (deleteFromSubUnits(unit, data.getParentId())) {
                    deleted = true;
                    break;
                }
            }
        }
        if (deleted) {
        	MongoTemplate mongoTemplate=new MongoTemplate(mongoClient,data.getDbname());
            mongoTemplate.save(root, data.getSubjectName());  // Save updated document
            System.out.println("Unit deleted successfully.");
        } else {
            System.out.println("Parent ID not found.");
        }
    }
    private boolean deleteFromSubUnits(Unit current, String targetId) {
        if (current.getUnits() != null) {
            boolean removed = removeUnitById(current.getUnits(), targetId);
            if (removed) return true;

            for (Unit child : current.getUnits()) {
                if (deleteFromSubUnits(child, targetId)) {
                    return true;
                }
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

    public void addUnit(WrapperUnit data) {
    	Unit unit=null;
    	System.out.println(data);
    	boolean assignTest = false;

    	// Case 1: It's the top-level unit (direct child of root)
    	if (data.getParentId().equals(data.getRootUnitId())) {
    	    assignTest = true;
    	    System.out.println("Adding top-level unit");
    	} 
//    	else {
//    	    // Case 2: Check if it's first subunit under top unit
//    	    UnitRequest root = getById(data.getRootUnitId(), data.getSubjectName(), data.getDbname());
//    	    if (root != null && root.getUnits() != null) {
//    	        for (Unit child : root.getUnits()) {
//    	            if (child.getId().equals(data.getParentId())) {
//    	                assignTest = true;
//    	                System.out.println("Adding first-level subunit");
//    	                break;
//    	            }
//    	        }
//    	    }
//    	}

    	unit = new Unit(assignTest);
        if (data.getParentId() == null || data.getParentId().isEmpty()) {
            unit.setParentId(null);  // Store null if empty or not provided
        } else {
            unit.setParentId(data.getParentId());  // Store the provided value
        }
        if(data.getAudioFileId()!=null||data.getAudioFileId().size()!=0) {
        	unit.setAudioFileId(data.getAudioFileId());
        }
//        if (data.getAudioFileId() == null || data.getAudioFileId().length==0) {
//        	System.out.println("this is emptys");
//            unit.setAudioFileId(null);  // Store null if empty or not provided
//        } else {
//        	 List<String> storedIds = new ArrayList<>();
//        	 for (MultipartFile audio : data.getAudioFileId()) {
//        	        try {
//        	            String storedId = storeAudio(audio, data.getDbname(), data.getSubjectName());
//        	            storedIds.add(storedId);
//        	        } catch (IOException e) {
//        	            e.printStackTrace();
//        	            return;
//        	        }
//        	    }
//        	 unit.setAudioFileId(storedIds); // if you're storing as comma-separated
//        	    System.out.println("Stored Audio IDs: " + storedIds);
//        }
        if (data.getUnitName() == null || data.getUnitName().isEmpty()) {
            unit.setUnitName(null);  // Store null if empty or not provided
        } else {
            unit.setUnitName(data.getUnitName());  // Store the provided value
        }
        // Set explanation as null if it's empty or not provided
        if (data.getExplanation() == null || data.getExplanation().isEmpty()) {
            unit.setExplanation(null);  // Store null if empty or not provided
        } else {
            unit.setExplanation(data.getExplanation());  // Store the provided value
        }

        System.out.println(unit);
        System.out.println("Using DB: " + data.getDbname());
        System.out.println("Using Collection: " + data.getSubjectName());
        System.out.println("Looking for ID: " + data.getRootUnitId());

        UnitRequest root = getById(data.getRootUnitId(),data.getSubjectName(),data.getDbname());
        if (root == null) {
            System.out.println("Root unit not found");
            return;
        }
        System.out.println("before: "+root);
        boolean inserted = false;
        if(root.getId().equals(data.getParentId())) {
        	root.getUnits().add(unit);
        	inserted=true;
        }
        else if (root.getUnits() != null) {
            for (Unit units : root.getUnits()) {
                if (insertIntoParent(units, data.getParentId(), unit)) {
                    inserted = true;
                    break;
                }
            }
        }
        if (inserted) {
        	System.out.println("in insertion");
        	MongoTemplate mongoTemplate=new MongoTemplate(mongoClient,data.getDbname());
        	mongoTemplate.save(root, data.getSubjectName());  // ✅ correct: saves in dynamic collection
            System.out.println(root);// Save the updated root document
        } else {
            System.out.println("Parent ID not found in any subunit");
        }
//        Unit parent=getById(data.getParentId());
//        if(parent ==null) {
//        	System.out.println("no parent");
//        	return;
//        }
//        parent.getSubunit().add(unit);
//        unitRepository.save(parent);
    }
    
    public void updateUnit(WrapperUnit data) {
    	UnitRequest root = getById(data.getRootUnitId(),data.getSubjectName(),data.getDbname());
        if (root == null) {
            System.out.println("Root unit not found");
            return;
        }
        System.out.println("before: "+root);
        boolean inserted = false;
        if(root.getId().equals(data.getParentId())) {
        	root.setUnitName(data.getUnitName());
        	
        	inserted=true;
        }
        else if (root.getUnits() != null) {
            for (Unit units : root.getUnits()) {
                if (updateParent(units,data.getParentId(),data)) {
                    inserted = true;
                    break;
                }
            }
        }
        if (inserted) {
        	System.out.println("in insertion");
        	MongoTemplate mongoTemplate=new MongoTemplate(mongoClient,data.getDbname());
        	mongoTemplate.save(root, data.getSubjectName());  // ✅ correct: saves in dynamic collection
            System.out.println(root);// Save the updated root document
        } else {
            System.out.println("Parent ID not found in any subunit");
        }
    }
    private boolean updateParent(Unit current, String targetParentId,WrapperUnit data) {
    	
    	if (current.getId() != null && current.getId().equals(targetParentId)) {
            current.setUnitName(data.getUnitName());
            //current.setExplanation(data.getExplanation());
            
            	current.setExplanation(data.getExplanation());
            
            
            	current.setAudioFileId(data.getAudioFileId());
            
            
//            if (data.getAudioFileId() != null && data.getAudioFileId().length > 0) {
//                for (MultipartFile audio : data.getAudioFileId()) {
//                    try {
//                        String storedId = storeAudio(audio, data.getDbname(), data.getSubjectName());
//                        newAudioIds.add(storedId);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        return false;
//                    }
//                }
//            }
//            List<String> existingIds = current.getAudioFileId();
//            List<String> keepIds = data.getKeepAudioFileIds(); // This comes from frontend
//            if (existingIds != null) {
//                for (String oldId : existingIds) {
//                    if (keepIds == null || !keepIds.contains(oldId)) {
//                        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(oldId)));
//                    }
//                }
//            }
//            List<String> updatedAudioFileIds = new ArrayList<>();
//            if (keepIds != null) updatedAudioFileIds.addAll(keepIds);
//            updatedAudioFileIds.addAll(newAudioIds);
//            current.setAudioFileId(updatedAudioFileIds);
           
           
			
            return true;
        }

        if (current.getUnits() != null) {
            for (Unit child : current.getUnits()) {
                if (updateParent(child, targetParentId,data)) {
                    return true;
                }
            }
        }

        return false;
    }
    private boolean insertIntoParent(Unit current, String targetParentId, Unit newUnit) {
        if (current.getId() != null && current.getId().equals(targetParentId)) {
            current.getUnits().add(newUnit);
            return true;
        }

        if (current.getUnits() != null) {
            for (Unit child : current.getUnits()) {
                if (insertIntoParent(child, targetParentId, newUnit)) {
                    return true;
                }
            }
        }

        return false;
    }
    

    // Fetch by ID
    public UnitRequest getById(String id,String collectionName,String dbname) {
    	
    	MongoTemplate mongoTemplate=new MongoTemplate(mongoClient,dbname);
    	try {
            return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
        } catch (IllegalArgumentException e) {
            // If not a valid ObjectId, fallback to String ID
            return mongoTemplate.findById(id, UnitRequest.class, collectionName);
        }
    }
    public void deleteAudioFromS3(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) {
            throw new IllegalArgumentException("Invalid file URL");
        }

        String fileKey = fileUrl.split(".amazonaws.com/")[1];

        try (S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3.deleteObject(deleteRequest);
            System.out.println("🗑️ S3 file deleted: " + fileKey);

        } catch (Exception e) {
            System.err.println("❌ Failed to delete from S3: " + e.getMessage());
            throw e; // Re-throw so caller can handle it
        }
    }
    private void deleteAllAudioFiles(Unit unit) {
        if (unit.getAudioFileId() != null) {
            for (String audioUrl : unit.getAudioFileId()) {
                try {
                    deleteAudioFromS3(audioUrl);  // ✅ Delete from S3
                } catch (Exception e) {
                    System.err.println("Error deleting audio: " + audioUrl);
                }
            }
        }

        if (unit.getUnits() != null) {
            for (Unit sub : unit.getUnits()) {
                deleteAllAudioFiles(sub);  // ✅ Recursively delete for children
            }
        }
    }

}
//    public String storeAudio(MultipartFile file, String dbName, String collectionName) throws IOException {
//        MongoDatabase db = mongoClient.getDatabase(dbName);
//        GridFSBucket bucket = GridFSBuckets.create(db, collectionName); // dynamic collection
//
//        try (InputStream inputStream = file.getInputStream()) {
//            ObjectId fileId = bucket.uploadFromStream(file.getOriginalFilename(), inputStream,
//                new GridFSUploadOptions().chunkSizeBytes(358400).metadata(
//                    new org.bson.Document("contentType", file.getContentType())
//                )
//            );
//            return fileId.toHexString();
//        }
//    }
//    public String deleteAudio(String id,String dbName,String collectionName) {
//    	try {
//	         MongoDatabase db = mongoClient.getDatabase(dbName);
//	         GridFSBucket bucket = GridFSBuckets.create(db, collectionName);
//	         bucket.delete(new ObjectId(id));
//	         return "Deleted";
//	     } catch (Exception e) {
//	         return "error";
//	     }
//    }
//    public static class GridFSFileWithStream {
//        public final GridFSFile file;
//        public final InputStream stream;
//
//        public GridFSFileWithStream(GridFSFile file, InputStream stream) {
//            this.file = file;
//            this.stream = stream;
//        }
//    }
//    public GridFSFileWithStream getAudioFile(String dbName, String collectionName, String fileId) {
//        MongoDatabase db = mongoClient.getDatabase(dbName);
//        GridFSBucket bucket = GridFSBuckets.create(db, collectionName);
//
//        ObjectId id = new ObjectId(fileId);
//        GridFSFile file = bucket.find(new org.bson.Document("_id", id)).first();
//
//        if (file == null) return null;
//
//        return new GridFSFileWithStream(file, bucket.openDownloadStream(id));
//    }


