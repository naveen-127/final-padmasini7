package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.TestPaperSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestPaperSubmissionRepository extends MongoRepository<TestPaperSubmission, String> {
    List<TestPaperSubmission> findByTestId(String testId);
}
