package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.StudentEnquiry;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.StudentEnquiryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class StudentEnquiryService {
    
    @Autowired
    private MongoClient mongoClient; // ✅ Inject the raw MongoDB client
    
    private MongoTemplate contactDataTemplate; // ✅ Template for Contact_Data database
    
    // ✅ Database and collection names
    private final String contactDataDatabaseName = "Contact_Data";
    private final String supportCollectionName = "Support";
    
    @Autowired
    private StudentEnquiryRepository studentEnquiryRepository;
    
    // ✅ Initialize connection to Contact_Data database
    @PostConstruct
    public void init() {
        try {
            System.out.println("🚀 Initializing StudentEnquiryService...");
            System.out.println("📊 Trying to connect to database: " + contactDataDatabaseName);
            
            // Test if database exists first
            MongoDatabase db = mongoClient.getDatabase(contactDataDatabaseName);
            System.out.println("✅ Got database reference: " + db.getName());
            
            // List collections to verify
            System.out.println("📁 Checking collections in " + contactDataDatabaseName + "...");
            for (String collectionName : db.listCollectionNames()) {
                System.out.println("   - " + collectionName);
            }
            
            // Create MongoTemplate
            this.contactDataTemplate = new MongoTemplate(mongoClient, contactDataDatabaseName);
            System.out.println("✅ StudentEnquiryService initialized successfully!");
            System.out.println("✅ Connected to DB: " + contactDataDatabaseName);
            
        } catch (Exception e) {
            System.err.println("❌❌❌ CRITICAL ERROR in StudentEnquiryService.init() ❌❌❌");
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.err.println("💡 TROUBLESHOOTING:");
            System.err.println("1. Check if 'Contact_Data' database exists in MongoDB Atlas");
            System.err.println("2. Check if MongoDB Atlas connection string is correct");
            System.err.println("3. Check if user has access to 'Contact_Data' database");
            throw new RuntimeException("Failed to initialize StudentEnquiryService: " + e.getMessage(), e);
        }
    }
    
    // Get all enquiries from Contact_Data database - SIMPLIFIED VERSION
    public List<StudentEnquiry> getAllEnquiries() {
        try {
            System.out.println("🔍 getAllEnquiries() called");
            
            if (contactDataTemplate == null) {
                System.err.println("❌ contactDataTemplate is NULL! Service not initialized properly.");
                throw new RuntimeException("MongoTemplate not initialized. Check @PostConstruct init() method.");
            }
            
            // Simple query - no complex debugging
            Query query = new Query();
            List<StudentEnquiry> enquiries = contactDataTemplate.find(query, StudentEnquiry.class, supportCollectionName);
            
            System.out.println("✅ Found " + enquiries.size() + " enquiries in " + contactDataDatabaseName + "." + supportCollectionName);
            
            if (!enquiries.isEmpty()) {
                System.out.println("📋 Sample data - First enquiry:");
                StudentEnquiry first = enquiries.get(0);
                System.out.println("   ID: " + first.getId());
                System.out.println("   Name: " + first.getName());
                System.out.println("   Email: " + first.getEmail());
                System.out.println("   Status: " + first.getStatus());
            }
            
            return enquiries;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR in getAllEnquiries: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch enquiries: " + e.getMessage(), e);
        }
    }
    
    // Get enquiry by ID - SIMPLIFIED
 // Update getEnquiryById method in StudentEnquiryService.java
    public Optional<StudentEnquiry> getEnquiryById(String id) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            
            // FIX: Use findOne with explicit projection to include all fields
            StudentEnquiry enquiry = contactDataTemplate.findOne(query, StudentEnquiry.class, supportCollectionName);
            
            if (enquiry != null) {
                System.out.println("✅ Found enquiry " + id);
                System.out.println("📄 FileName: " + enquiry.getFileName());
                System.out.println("📄 ContentType: " + enquiry.getContentType());
                System.out.println("📄 FileData exists: " + (enquiry.getFileData() != null));
                
                // Debug: Check what fields are actually present in the document
                Document rawDoc = contactDataTemplate.findById(id, Document.class, supportCollectionName);
                if (rawDoc != null) {
                    System.out.println("🔍 Raw document fields: " + rawDoc.keySet());
                    if (rawDoc.containsKey("fileData")) {
                        System.out.println("🔍 fileData field type: " + rawDoc.get("fileData").getClass().getName());
                        System.out.println("🔍 fileData value: " + rawDoc.get("fileData"));
                    }
                }
                
                return Optional.of(enquiry);
            } else {
                System.out.println("❌ Enquiry not found: " + id);
                return Optional.empty();
            }
        } catch (Exception e) {
            System.err.println("Error fetching enquiry by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    // Filter enquiries by status and category - SIMPLIFIED
    public List<StudentEnquiry> filterEnquiries(String status, String category) {
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
            return contactDataTemplate.find(query, StudentEnquiry.class, supportCollectionName);
        } catch (Exception e) {
            System.err.println("Error filtering enquiries: " + e.getMessage());
            throw e;
        }
    }
    
    // Assign enquiry to employee - SIMPLIFIED
 // In StudentEnquiryService.java, update the assignEnquiry method:
    public StudentEnquiry assignEnquiry(String id, String assignedTo, 
                                       String taskDescription, Date deadline,
                                       String status, String priority) {
        try {
            System.out.println("Assigning enquiry " + id + " to " + assignedTo);
            
            // Find the enquiry
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            StudentEnquiry enquiry = contactDataTemplate.findOne(query, StudentEnquiry.class, supportCollectionName);
            
            if (enquiry != null) {
                // Update fields
                enquiry.setAssignedTo(assignedTo);
                enquiry.setTaskDescription(taskDescription);
                
                // Convert Date to String for deadline
                if (deadline != null) {
                    enquiry.setDeadline(deadline.toInstant().toString());
                }
                
                enquiry.setStatus(status);
                enquiry.setPriority(priority);
                
                // Set assignment date as String
                enquiry.setAssignmentDate(new Date());
                
                System.out.println("Enquiry before save: " + enquiry);
                
                // Save back to database
                StudentEnquiry saved = contactDataTemplate.save(enquiry, supportCollectionName);
                System.out.println("Successfully assigned enquiry " + id);
                System.out.println("Saved enquiry: " + saved);
                return saved;
            } else {
                System.err.println("Enquiry not found with ID: " + id);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error assigning enquiry " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Update completeEnquiry method:
    public StudentEnquiry completeEnquiry(String id, String employeeNotes) {
        try {
            System.out.println("Completing enquiry " + id);
            
            // Find the enquiry
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            StudentEnquiry enquiry = contactDataTemplate.findOne(query, StudentEnquiry.class, supportCollectionName);
            
            if (enquiry != null) {
                enquiry.setStatus("completed");
                
                // Set completed date as String
                enquiry.setCompletedDate(new Date());
                
                if (employeeNotes != null && !employeeNotes.trim().isEmpty()) {
                    enquiry.setEmployeeNotes(employeeNotes);
                }
                
                StudentEnquiry saved = contactDataTemplate.save(enquiry, supportCollectionName);
                System.out.println("Successfully completed enquiry " + id);
                return saved;
            } else {
                System.err.println("Enquiry not found with ID: " + id);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error completing enquiry " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
   
    
    // Get statistics - SIMPLIFIED
    public SupportStatistics getStatistics() {
        try {
            List<StudentEnquiry> allEnquiries = getAllEnquiries();
            
            SupportStatistics stats = new SupportStatistics();
            stats.setTotal(allEnquiries.size());
            stats.setPending((int) allEnquiries.stream()
                .filter(e -> "pending".equals(e.getStatus()))
                .count());
            stats.setAssigned((int) allEnquiries.stream()
                .filter(e -> "assigned".equals(e.getStatus()))
                .count());
            stats.setCompleted((int) allEnquiries.stream()
                .filter(e -> "completed".equals(e.getStatus()))
                .count());
            
            return stats;
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            throw e;
        }
    }
    
    // Statistics DTO class
    public static class SupportStatistics {
        private int total;
        private int pending;
        private int assigned;
        private int completed;
        
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
        public int getAssigned() { return assigned; }
        public void setAssigned(int assigned) { this.assigned = assigned; }
        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }
    }

	public StudentEnquiry updateEnquiry(String id, StudentEnquiry updatedEnquiry) {
		// TODO Auto-generated method stub
		return null;
	}
}
