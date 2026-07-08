package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Visitor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VisitorRepository extends MongoRepository<Visitor, String> {
    
    // Find visitors by status
    List<Visitor> findByStatus(String status);
    
    // Find visitors with score greater than threshold
    List<Visitor> findByScoreGreaterThan(int score);
    
    // Custom query for percentage range
    @Query("{ 'percentage': { $gte: ?0, $lte: ?1 } }")
    List<Visitor> findVisitorsByPercentageRange(double min, double max);
    
    // Search by name (case-insensitive)
    List<Visitor> findByNameContainingIgnoreCase(String name);
    
    // Search by email
    List<Visitor> findByEmailContainingIgnoreCase(String email);
    
    // Search by phone
    List<Visitor> findByPhoneContaining(String phone);
    
    // Get all visitors with specific class
    List<Visitor> findBy_class(String _class);
}