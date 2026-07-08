package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Visitor;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VisitorService {
    
    @Autowired
    private VisitorRepository visitorRepository;
    
    // Get all visitors from studentUsers collection
    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }
    
    // Get visitor by ID
    public Optional<Visitor> getVisitorById(String id) {
        return visitorRepository.findById(id);
    }
    
    // Create or update visitor
    public Visitor saveVisitor(Visitor visitor) {
        if (visitor.getRegisteredAt() == null) {
            visitor.setRegisteredAt(LocalDateTime.now());
        }
        visitor.setSubmittedAt(LocalDateTime.now());
        // Set the class if not set
        if (visitor.get_class() == null) {
            visitor.set_class("com.padmasiniAdmin.padmasiniAdmin_1.model.Visitor");
        }
        return visitorRepository.save(visitor);
    }
    
    // Delete visitor
    public void deleteVisitor(String id) {
        visitorRepository.deleteById(id);
    }
    
    // Find visitors by status
    public List<Visitor> getVisitorsByStatus(String status) {
        return visitorRepository.findByStatus(status);
    }
    
    // Search visitors
    public List<Visitor> searchVisitors(String keyword) {
        List<Visitor> byName = visitorRepository.findByNameContainingIgnoreCase(keyword);
        List<Visitor> byEmail = visitorRepository.findByEmailContainingIgnoreCase(keyword);
        List<Visitor> byPhone = visitorRepository.findByPhoneContaining(keyword);
        
        // Merge and remove duplicates
        byName.addAll(byEmail);
        byName.addAll(byPhone);
        return byName.stream().distinct().collect(Collectors.toList());
    }
    
    // Get visitor statistics
    public VisitorStatistics getStatistics() {
        List<Visitor> all = visitorRepository.findAll();
        int total = all.size();
        long passed = all.stream().filter(v -> "Passed".equalsIgnoreCase(v.getStatus())).count();
        long failed = all.stream().filter(v -> "Failed".equalsIgnoreCase(v.getStatus())).count();
        double avgPercentage = all.stream().mapToDouble(Visitor::getPercentage).average().orElse(0.0);
        
        return new VisitorStatistics(total, passed, failed, avgPercentage);
    }
    
    // Inner class for statistics
    public static class VisitorStatistics {
        private int total;
        private long passed;
        private long failed;
        private double averagePercentage;
        
        public VisitorStatistics(int total, long passed, long failed, double averagePercentage) {
            this.total = total;
            this.passed = passed;
            this.failed = failed;
            this.averagePercentage = averagePercentage;
        }
        
        // Getters
        public int getTotal() { return total; }
        public long getPassed() { return passed; }
        public long getFailed() { return failed; }
        public double getAveragePercentage() { return averagePercentage; }
    }
}