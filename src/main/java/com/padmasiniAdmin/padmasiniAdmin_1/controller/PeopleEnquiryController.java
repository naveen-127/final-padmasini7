package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.PeopleEnquiry;
import com.padmasiniAdmin.padmasiniAdmin_1.service.PeopleEnquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/people-enquiries")
@CrossOrigin(origins = "https://dafj1druksig9.cloudfront.net", allowCredentials = "true")
public class PeopleEnquiryController {
    
    private static final Logger logger = LoggerFactory.getLogger(PeopleEnquiryController.class);
    
    @Autowired
    private PeopleEnquiryService peopleEnquiryService;
    
    // Get all enquiries
    @GetMapping
    public ResponseEntity<?> getAllEnquiries() {
        try {
            logger.info("Fetching all people enquiries");
            List<PeopleEnquiry> enquiries = peopleEnquiryService.getAllEnquiries();
            return ResponseEntity.ok(enquiries);
        } catch (Exception e) {
            logger.error("Error fetching enquiries: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch enquiries", "message", e.getMessage()));
        }
    }
    
    // Get enquiry by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getEnquiryById(@PathVariable String id) {
        try {
            logger.info("Fetching enquiry by ID: {}", id);
            PeopleEnquiry enquiry = peopleEnquiryService.getEnquiryById(id);
            if (enquiry != null) {
                return ResponseEntity.ok(enquiry);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching enquiry {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch enquiry", "message", e.getMessage()));
        }
    }
    
    // Filter enquiries
    @GetMapping("/filter")
    public ResponseEntity<?> filterEnquiries(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false, defaultValue = "all") String category) {
        try {
            logger.info("Filtering enquiries - status: {}, category: {}", status, category);
            List<PeopleEnquiry> enquiries = peopleEnquiryService.filterEnquiries(status, category);
            return ResponseEntity.ok(enquiries);
        } catch (Exception e) {
            logger.error("Error filtering enquiries: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to filter enquiries", "message", e.getMessage()));
        }
    }
    
    // Update status
    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestBody StatusUpdateRequest request) {
        try {
            logger.info("Updating status for enquiry {} to {}", id, request.getStatus());
            
            if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Status is required"));
            }
            
            PeopleEnquiry enquiry = peopleEnquiryService.updateStatus(
                    id, 
                    request.getStatus(), 
                    request.getNotes());
            
            if (enquiry != null) {
                return ResponseEntity.ok(enquiry);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating status for enquiry {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update status", "message", e.getMessage()));
        }
    }
    
    // Assign enquiry
    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignEnquiry(
            @PathVariable String id,
            @RequestBody AssignRequest request) {
        try {
            logger.info("Assigning enquiry {} with data: {}", id, request);
            
            if (request.getAssignedTo() == null || request.getAssignedTo().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "assignedTo is required"));
            }
            
            if (request.getTaskDescription() == null || request.getTaskDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "taskDescription is required"));
            }
            
            if (request.getDeadline() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "deadline is required"));
            }
            
            PeopleEnquiry enquiry = peopleEnquiryService.assignEnquiry(
                    id,
                    request.getAssignedTo(),
                    request.getTaskDescription(),
                    request.getDeadline(),
                    request.getPriority() != null ? request.getPriority() : "medium",
                    request.getAssignedEmployeeName(),
                    request.getAssignedEmployeeDesignation()
            );
            
            if (enquiry != null) {
                return ResponseEntity.ok(enquiry);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error assigning enquiry {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to assign enquiry", "message", e.getMessage()));
        }
    }
    
    // Mark as complete
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeEnquiry(
            @PathVariable String id,
            @RequestBody(required = false) CompleteRequest request) {
        try {
            logger.info("Completing enquiry {}", id);
            
            String notes = request != null ? request.getNotes() : null;
            PeopleEnquiry enquiry = peopleEnquiryService.completeEnquiry(id, notes);
            
            if (enquiry != null) {
                return ResponseEntity.ok(enquiry);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error completing enquiry {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to complete enquiry", "message", e.getMessage()));
        }
    }
    
    // Get statistics
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            logger.info("Fetching people enquiry statistics");
            PeopleEnquiryService.EnquiryStatistics stats = peopleEnquiryService.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("total", stats.getTotal());
            response.put("new", stats.getNew());
            response.put("contacted", stats.getContacted());
            response.put("assigned", stats.getAssigned());
            response.put("resolved", stats.getResolved());
            response.put("spam", stats.getSpam());
            response.put("registered", stats.getRegistered());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching statistics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics", "message", e.getMessage()));
        }
    }
    
    // Get file info
    @GetMapping("/{id}/file-info")
    public ResponseEntity<?> getFileInfo(@PathVariable String id) {
        try {
            logger.info("Getting file info for enquiry {}", id);
            Map<String, Object> info = peopleEnquiryService.getFileInfo(id);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("Error getting file info: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get file info", "message", e.getMessage()));
        }
    }
    
    // Get file
    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        try {
            logger.info("Fetching file for enquiry {}", id);
            
            PeopleEnquiry enquiry = peopleEnquiryService.getEnquiryById(id);
            if (enquiry == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (enquiry.getFileData() == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileBytes = enquiry.getFileData().getData();
            if (fileBytes == null || fileBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }
            
            String contentType = enquiry.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = detectContentType(fileBytes);
            }
            
            String filename = enquiry.getFileName();
            if (filename == null || filename.isEmpty()) {
                filename = "attachment";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename(filename)
                    .build()
            );
            headers.setContentLength(fileBytes.length);
            
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error fetching file: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "People Enquiry API is working");
        response.put("timestamp", new Date());
        response.put("availableEndpoints", List.of(
            "GET /api/people-enquiries",
            "GET /api/people-enquiries/{id}",
            "GET /api/people-enquiries/filter",
            "POST /api/people-enquiries/{id}/assign",
            "POST /api/people-enquiries/{id}/complete",
            "GET /api/people-enquiries/statistics"
        ));
        return ResponseEntity.ok(response);
    }
    
    // Helper method to detect content type
    private String detectContentType(byte[] data) {
        if (data.length < 4) {
            return "application/octet-stream";
        }
        
        // PNG
        if (data[0] == (byte) 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "image/png";
        }
        
        // JPEG
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        
        // GIF
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8') {
            return "image/gif";
        }
        
        // PDF
        if (data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
            return "application/pdf";
        }
        
        return "application/octet-stream";
    }
    
    // Request DTOs
    public static class StatusUpdateRequest {
        private String status;
        private String notes;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    public static class AssignRequest {
        private String assignedTo;
        private String taskDescription;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Date deadline;
        private String priority = "medium";
        private String assignedEmployeeName;
        private String assignedEmployeeDesignation;
        
        // Getters and Setters
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
        
        public String getTaskDescription() { return taskDescription; }
        public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
        
        public Date getDeadline() { return deadline; }
        public void setDeadline(Date deadline) { this.deadline = deadline; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getAssignedEmployeeName() { return assignedEmployeeName; }
        public void setAssignedEmployeeName(String assignedEmployeeName) { this.assignedEmployeeName = assignedEmployeeName; }
        
        public String getAssignedEmployeeDesignation() { return assignedEmployeeDesignation; }
        public void setAssignedEmployeeDesignation(String assignedEmployeeDesignation) { this.assignedEmployeeDesignation = assignedEmployeeDesignation; }
    }
    
    public static class CompleteRequest {
        private String notes;
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
