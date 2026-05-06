package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.manageUser.UserModel;

@Service
public class SignInService {
	//public UserModel currentUser;
    @Autowired
    private MongoClient mongoClient;
public UserModel checkUserName(String userName,String password) {
	UserModel user=checkDbForUser(userName);
	//System.out.println("usernames:"+userName+" "+user.getUserName());
	if(user!=null&&user.getPassword().equals(password)) {
		//currentUser=user;
		return user;}
	System.out.println("no user found");
	return null;
}
public UserModel checkUserGmail(String gmail,String password) {
	UserModel user=checkDbForUserGmail(gmail);
	//System.out.println("usernames:"+userName+" "+user.getUserName());
	if(user!=null&&user.getPassword().equals(password)) {
		//currentUser=user;
		return user;}
	System.out.println("no user found");
	return null;
}
private UserModel checkDbForUserGmail(String gmail) {
	MongoTemplate mongo=new MongoTemplate(mongoClient,"users");
	Query query=new Query(Criteria.where("gmail").is(gmail));
	
	return mongo.findOne(query, UserModel.class, "users");
}
private UserModel checkDbForUser(String userName) {
	MongoTemplate mongo=new MongoTemplate(mongoClient,"users");
	Query query=new Query(Criteria.where("userName").is(userName));
	
	return mongo.findOne(query, UserModel.class, "users");
}
}
