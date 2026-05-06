// File: /home/ec2-user/final-padmasini7/src/main/java/com/padmasiniAdmin/padmasiniAdmin_1/service/StudentDataService.java
package com.padmasiniAdmin.padmasiniAdmin_1.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;

@Service
public class StudentDataService {
    private static final List<String> NEET_STANDARDS = Arrays.asList("11th", "12th");
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public List<UnitRequest> getUnitData(String subject, WrapperUnit request) {
        Query query = new Query();
        query.addCriteria(Criteria.where("standard").in(NEET_STANDARDS));
        query.with(Sort.by(Sort.Direction.ASC, "standard"));
        return mongoTemplate.find(query, UnitRequest.class, request.getSubjectName());
    }
}
