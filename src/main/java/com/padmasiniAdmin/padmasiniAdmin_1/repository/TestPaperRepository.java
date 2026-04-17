package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.TestPaper;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TestPaperRepository extends MongoRepository<TestPaper, String> {
    List<TestPaper> findBySubject(String subject);
}