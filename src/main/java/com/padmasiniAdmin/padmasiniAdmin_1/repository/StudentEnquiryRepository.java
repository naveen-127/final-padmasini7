package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.PeopleEnquiry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface StudentEnquiryRepository extends MongoRepository<PeopleEnquiry, String> {
    
    List<PeopleEnquiry> findByStatus(String status);
    List<PeopleEnquiry> findByCategory(String category);
    List<PeopleEnquiry> findByAssignedTo(String assignedTo);
    List<PeopleEnquiry> findByIsRegistered(Boolean isRegistered);
    
    // Custom queries
    List<PeopleEnquiry> findByStatusAndCategory(String status, String category);
    List<PeopleEnquiry> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    List<PeopleEnquiry> findBySubmittedAtBetween(Date startDate, Date endDate);
}
