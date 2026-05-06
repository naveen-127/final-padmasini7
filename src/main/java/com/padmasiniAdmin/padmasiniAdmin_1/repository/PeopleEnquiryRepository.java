package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.PeopleEnquiry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeopleEnquiryRepository extends MongoRepository<PeopleEnquiry, String> {
}
