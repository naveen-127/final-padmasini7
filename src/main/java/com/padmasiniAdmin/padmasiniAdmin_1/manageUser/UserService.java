package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
@Service
public class UserService {
	@Autowired
    private MongoClient mongoClient;
	String dbName = "users";
    String collectionName = "users";
public boolean saveNewUser(UserDTO user) {
	if(!checkUser(user.getUser().getUserName())&&!checkGmail(user.getUser().getGmail())) {
		
		UserModel userModel=user.getUser();
		System.out.println("in user service for subject: " +userModel.getSubjects());
		MongoTemplate mongoTemplate=new MongoTemplate(mongoClient,dbName);
		mongoTemplate.save(userModel,collectionName);
		System.out.println("data saved look db");
		return true;
	}
	else {
		return false;
	}
}
private boolean checkGmail(String gmail) {
//	String dbName = "users";
//    String collectionName = "users";
    MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
    Query query = new Query(Criteria.where("gmail").is(gmail));
    return mongoTemplate.exists(query, UserModel.class, collectionName);
}
private boolean checkUser(String name) {
//	 String dbName = "users";
//	    String collectionName = "users";
	    MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
	    Query query = new Query(Criteria.where("userName").is(name));
	    return mongoTemplate.exists(query, UserModel.class, collectionName);
}
public List<UserModel> getUsers() {
//	String dbName="users";
//	String collectionName="users";
	MongoTemplate mt=new MongoTemplate(mongoClient,dbName);
	return mt.findAll(UserModel.class,collectionName);
}

public void deleteUser(String gmail) {
    MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, dbName);
    System.out.println("Deleting user: [" + gmail + "]");
    Query query = new Query(Criteria.where("gmail").is(gmail));
    mongoTemplate.remove(query, UserModel.class, collectionName);
    System.out.println("deleted  "+gmail);
}
}
