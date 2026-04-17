package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.TestSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSubmissionRepository extends MongoRepository<TestSubmission, String> {
    List<TestSubmission> findByTestPaperId(String testPaperId);
    List<TestSubmission> findByStatus(String status);
}
