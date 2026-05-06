package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.LearningMaterial;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningMaterialRepository extends MongoRepository<LearningMaterial, String> {
}
