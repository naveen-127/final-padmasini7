package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.ClassAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends MongoRepository<ClassAssignment, String> {
}