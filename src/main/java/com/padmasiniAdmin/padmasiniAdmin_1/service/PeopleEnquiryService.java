package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.PeopleEnquiry;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PeopleEnquiryService {
    
    @Autowired
    private MongoClient mongoClient;
    
    private MongoTemplate enquiryTemplate;
    
    private final String databaseName = "Contact_Data";
    private final String collectionName = "Enquiry";
    
    @PostConstruct
    public void init() {
        try {
            System.out.println("🚀 Initializing PeopleEnquiryService...");
            System.out.println("📊 Connecting to database: " + databaseName);
            
            MongoDatabase db = mongoClient.getDatabase(databaseName);
            System.out.println("✅ Connected to database: " + db.getName());
            
            // List collections
            System.out.println("📁 Collections in " + databaseName + ":");
            for (String collName : db.listCollectionNames()) {
                System.out.println("   - " + collName);
            }
            
            // Create MongoTemplate
            this.enquiryTemplate = new MongoTemplate(mongoClient, databaseName);
            System.out.println("✅ PeopleEnquiryService initialized successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing PeopleEnquiryService: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize PeopleEnquiryService", e);
        }
    }
    
    // Get all enquiries
    public List<PeopleEnquiry> getAllEnquiries() {
        try {
            System.out.println("🔍 Fetching all people enquiries...");
            
            if (enquiryTemplate == null) {
                throw new RuntimeException("MongoTemplate not initialized");
            }
            
            Query query = new Query();
            List<PeopleEnquiry> enquiries = enquiryTemplate.find(query, PeopleEnquiry.class, collectionName);
            
            System.out.println("✅ Found " + enquiries.size() + " enquiries");
            
            // Initialize status for old records
            List<PeopleEnquiry> updatedEnquiries = enquiries.stream()
                .map(enquiry -> {
                    if (enquiry.getStatus() == null) {
                        enquiry.setStatus("new");
                        enquiryTemplate.save(enquiry, collectionName);
                    }
                    return enquiry;
                })
                .collect(Collectors.toList());
            
            return updatedEnquiries;
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching enquiries: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch enquiries: " + e.getMessage(), e);
        }
    }
    
    // Get enquiry by ID
    public PeopleEnquiry getEnquiryById(String id) {
        try {
            Query query = new Query(Criteria.where("_id").is(id));
            return enquiryTemplate.findOne(query, PeopleEnquiry.class, collectionName);
        } catch (Exception e) {
            System.err.println("Error fetching enquiry by ID " + id + ": " + e.getMessage());
            throw e;
        }
    }
    
    // Filter enquiries
    public List<PeopleEnquiry> filterEnquiries(String status, String category) {
        try {
            Query query = new Query();
            Criteria criteria = new Criteria();
            
            if (!"all".equals(status)) {
                criteria.and("status").is(status);
            }
            if (!"all".equals(category)) {
                criteria.and("category").is(category);
            }
            
            query.addCriteria(criteria);
            return enquiryTemplate.find(query, PeopleEnquiry.class, collectionName);
        } catch (Exception e) {
            System.err.println("Error filtering enquiries: " + e.getMessage());
            throw e;
        }
    }
    
    // Update enquiry status
    public PeopleEnquiry updateStatus(String id, String status, String notes) {
        try {
            Query query = new Query(Criteria.where("_id").is(id));
            Update update = new Update();
            
            update.set("status", status);
            
            if (status.equals("resolved")) {
                update.set("completedDate", new Date());
            }
            
            if (notes != null && !notes.trim().isEmpty()) {
                // Append to existing notes
                PeopleEnquiry existing = getEnquiryById(id);
                String existingNotes = existing.getNotes() != null ? existing.getNotes() + "\n" : "";
                update.set("notes", existingNotes + notes);
            }
            
            enquiryTemplate.updateFirst(query, update, PeopleEnquiry.class, collectionName);
            return getEnquiryById(id);
            
        } catch (Exception e) {
            System.err.println("Error updating status for enquiry " + id + ": " + e.getMessage());
            throw e;
        }
    }
    
    // Assign enquiry
    public PeopleEnquiry assignEnquiry(String id, String assignedTo, 
                                       String taskDescription, Date deadline,
                                       String priority, 
                                       String assignedEmployeeName,
                                       String assignedEmployeeDesignation) {
        try {
            System.out.println("🎯 Assigning enquiry " + id + " to " + assignedTo);
            
            Query query = new Query(Criteria.where("_id").is(id));
            Update update = new Update();
            
            update.set("assignedTo", assignedTo);
            update.set("taskDescription", taskDescription);
            update.set("deadline", deadline);
            update.set("priority", priority);
            update.set("status", "assigned");
            update.set("assignmentDate", new Date());
            update.set("assignedEmployeeName", assignedEmployeeName);
            update.set("assignedEmployeeDesignation", assignedEmployeeDesignation);
            
            // Update notes
            PeopleEnquiry existing = getEnquiryById(id);
            String existingNotes = existing.getNotes() != null ? existing.getNotes() + "\n" : "";
            String newNote = "Assigned to " + assignedEmployeeName + " on " + new Date() + 
                           "\nTask: " + taskDescription + 
                           "\nDeadline: " + deadline + 
                           "\nPriority: " + priority;
            update.set("notes", existingNotes + newNote);
            
            enquiryTemplate.updateFirst(query, update, PeopleEnquiry.class, collectionName);
            
            PeopleEnquiry updated = getEnquiryById(id);
            System.out.println("✅ Successfully assigned enquiry " + id);
            return updated;
            
        } catch (Exception e) {
            System.err.println("Error assigning enquiry " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Mark as complete
    public PeopleEnquiry completeEnquiry(String id, String notes) {
        try {
            System.out.println("✅ Completing enquiry " + id);
            
            Query query = new Query(Criteria.where("_id").is(id));
            Update update = new Update();
            
            update.set("status", "resolved");
            update.set("completedDate", new Date());
            
            if (notes != null && !notes.trim().isEmpty()) {
                PeopleEnquiry existing = getEnquiryById(id);
                String existingNotes = existing.getNotes() != null ? existing.getNotes() + "\n" : "";
                update.set("notes", existingNotes + "Completed on " + new Date() + "\n" + notes);
            }
            
            enquiryTemplate.updateFirst(query, update, PeopleEnquiry.class, collectionName);
            
            PeopleEnquiry updated = getEnquiryById(id);
            System.out.println("✅ Successfully completed enquiry " + id);
            return updated;
            
        } catch (Exception e) {
            System.err.println("Error completing enquiry " + id + ": " + e.getMessage());
            throw e;
        }
    }
    
    // Get statistics
    public EnquiryStatistics getStatistics() {
        try {
            List<PeopleEnquiry> allEnquiries = getAllEnquiries();
            
            EnquiryStatistics stats = new EnquiryStatistics();
            stats.setTotal(allEnquiries.size());
            stats.setNew((int) allEnquiries.stream()
                .filter(e -> "new".equals(e.getStatus()))
                .count());
            stats.setContacted((int) allEnquiries.stream()
                .filter(e -> "contacted".equals(e.getStatus()))
                .count());
            stats.setAssigned((int) allEnquiries.stream()
                .filter(e -> "assigned".equals(e.getStatus()))
                .count());
            stats.setResolved((int) allEnquiries.stream()
                .filter(e -> "resolved".equals(e.getStatus()))
                .count());
            stats.setSpam((int) allEnquiries.stream()
                .filter(e -> "spam".equals(e.getStatus()))
                .count());
            stats.setRegistered((int) allEnquiries.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsRegistered()))
                .count());
            
            return stats;
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            throw e;
        }
    }
    
    // Get file info
    public Map<String, Object> getFileInfo(String id) {
        try {
            PeopleEnquiry enquiry = getEnquiryById(id);
            if (enquiry == null) {
                return Map.of("error", "Enquiry not found");
            }
            
            return Map.of(
                "id", enquiry.getId(),
                "fileName", enquiry.getFileName(),
                "contentType", enquiry.getContentType(),
                "fileDataExists", enquiry.getFileData() != null,
                "fileDataSize", enquiry.getFileData() != null ? enquiry.getFileData().getData().length : 0
            );
        } catch (Exception e) {
            System.err.println("Error getting file info: " + e.getMessage());
            throw e;
        }
    }
    
    // Statistics DTO
    public static class EnquiryStatistics {
        private int total;
        private int newCount;
        private int contacted;
        private int assigned;
        private int resolved;
        private int spam;
        private int registered;
        
        // Getters and Setters
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        
        public int getNew() { return newCount; }
        public void setNew(int newCount) { this.newCount = newCount; }
        
        public int getContacted() { return contacted; }
        public void setContacted(int contacted) { this.contacted = contacted; }
        
        public int getAssigned() { return assigned; }
        public void setAssigned(int assigned) { this.assigned = assigned; }
        
        public int getResolved() { return resolved; }
        public void setResolved(int resolved) { this.resolved = resolved; }
        
        public int getSpam() { return spam; }
        public void setSpam(int spam) { this.spam = spam; }
        
        public int getRegistered() { return registered; }
        public void setRegistered(int registered) { this.registered = registered; }
    }
}
