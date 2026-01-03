package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.DeleteResult;

import java.util.List;

@Service
public class StudentService {
    
    @Autowired
    private MongoClient mongoClient;
    
    private static final String DB_NAME = "studentUsers";
    private static final String COLLECTION_NAME = "studentUserDetail";
    
    public List<StudentModel> getAllStudents() {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, DB_NAME);
        return mongoTemplate.findAll(StudentModel.class, COLLECTION_NAME);
    }
    
    public StudentModel getStudentById(String id) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, DB_NAME);
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, StudentModel.class, COLLECTION_NAME);
    }
    
    public StudentModel getStudentByEmail(String email) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, DB_NAME);
        Query query = new Query(Criteria.where("email").is(email));
        return mongoTemplate.findOne(query, StudentModel.class, COLLECTION_NAME);
    }
    
    public boolean deleteStudentById(String id) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, DB_NAME);
        Query query = new Query(Criteria.where("_id").is(id));
        DeleteResult result = mongoTemplate.remove(query, StudentModel.class, COLLECTION_NAME);
        return result.getDeletedCount() > 0;
    }
    
    public boolean deleteStudentByEmail(String email) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, DB_NAME);
        Query query = new Query(Criteria.where("email").is(email));
        DeleteResult result = mongoTemplate.remove(query, StudentModel.class, COLLECTION_NAME);
        return result.getDeletedCount() > 0;
    }
    
    public boolean saveStudent(StudentModel student) {
        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, DB_NAME);
            mongoTemplate.save(student, COLLECTION_NAME);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean studentExists(String email) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, DB_NAME);
        Query query = new Query(Criteria.where("email").is(email));
        return mongoTemplate.exists(query, StudentModel.class, COLLECTION_NAME);
    }
}
