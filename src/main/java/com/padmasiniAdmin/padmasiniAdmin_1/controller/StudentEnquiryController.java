package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.StudentEnquiry;
import com.padmasiniAdmin.padmasiniAdmin_1.service.StudentEnquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import org.bson.types.Binary;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "https://dafj1druksig9.cloudfront.net/:3000", allowCredentials = "true")
public class StudentEnquiryController {
    
    private static final Logger logger = LoggerFactory.getLogger(StudentEnquiryController.class);
    
    @Autowired
    private StudentEnquiryService studentEnquiryService;
    
    // Get all enquiries
    @GetMapping("/enquiries")
    public ResponseEntity<?> getAllEnquiries() {
        try {
            logger.info("Fetching all enquiries");
            List<StudentEnquiry> enquiries = studentEnquiryService.getAllEnquiries();
            
            // Log the first enquiry to see structure
            if (!enquiries.isEmpty()) {
                logger.info("First enquiry sample: ID={}, Name={}, Email={}", 
                    enquiries.get(0).getId(), 
                    enquiries.get(0).getName(),
                    enquiries.get(0).getEmail());
            }
            
            return ResponseEntity.ok(enquiries);
        } catch (Exception e) {
            logger.error("Error fetching all enquiries: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch enquiries");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    // Get enquiry by ID
    @GetMapping("/enquiries/{id}")
    public ResponseEntity<?> getEnquiryById(@PathVariable String id) {
        try {
            logger.info("Fetching enquiry by ID: {}", id);
            return studentEnquiryService.getEnquiryById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching enquiry by ID {}: ", id, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch enquiry");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    // Filter enquiries
    @GetMapping("/enquiries/filter")
    public ResponseEntity<?> filterEnquiries(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false, defaultValue = "all") String category) {
        try {
            logger.info("Filtering enquiries - status: {}, category: {}", status, category);
            List<StudentEnquiry> enquiries = studentEnquiryService.filterEnquiries(status, category);
            return ResponseEntity.ok(enquiries);
        } catch (Exception e) {
            logger.error("Error filtering enquiries - status: {}, category: {}: ", status, category, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to filter enquiries");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    // Assign enquiry
    @PostMapping("/enquiries/{id}/assign")
    public ResponseEntity<?> assignEnquiry(
            @PathVariable String id,
            @RequestBody AssignRequest request) {
        try {
            logger.info("Assigning enquiry {} with data: {}", id, request);
            
            if (request.getAssignedTo() == null || request.getAssignedTo().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Validation error");
                errorResponse.put("message", "assignedTo is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            StudentEnquiry enquiry = studentEnquiryService.assignEnquiry(
                    id,
                    request.getAssignedTo(),
                    request.getTaskDescription(),
                    request.getDeadline(),
                    request.getStatus(),
                    request.getPriority()
            );
            
            if (enquiry != null) {
                logger.info("Successfully assigned enquiry {}", id);
                return ResponseEntity.ok(enquiry);
            } else {
                logger.warn("Enquiry not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error assigning enquiry {}: ", id, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to assign enquiry");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    // Mark as complete
    @PostMapping("/enquiries/{id}/complete")
    public ResponseEntity<?> completeEnquiry(
            @PathVariable String id,
            @RequestBody(required = false) CompleteRequest request) {
        try {
            logger.info("Completing enquiry {}", id);
            
            String employeeNotes = request != null ? request.getEmployeeNotes() : null;
            StudentEnquiry enquiry = studentEnquiryService.completeEnquiry(id, employeeNotes);
            
            if (enquiry != null) {
                logger.info("Successfully completed enquiry {}", id);
                return ResponseEntity.ok(enquiry);
            } else {
                logger.warn("Enquiry not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error completing enquiry {}: ", id, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to complete enquiry");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    // Update enquiry
    @PutMapping("/enquiries/{id}")
    public ResponseEntity<?> updateEnquiry(
            @PathVariable String id,
            @RequestBody StudentEnquiry updatedEnquiry) {
        try {
            logger.info("Updating enquiry {}", id);
            StudentEnquiry enquiry = studentEnquiryService.updateEnquiry(id, updatedEnquiry);
            
            if (enquiry != null) {
                logger.info("Successfully updated enquiry {}", id);
                return ResponseEntity.ok(enquiry);
            } else {
                logger.warn("Enquiry not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating enquiry {}: ", id, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update enquiry");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    // Get statistics
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            logger.info("Fetching support statistics");
            StudentEnquiryService.SupportStatistics stats = studentEnquiryService.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("total", stats.getTotal());
            response.put("pending", stats.getPending());
            response.put("assigned", stats.getAssigned());
            response.put("completed", stats.getCompleted());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching statistics: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch statistics");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    // Request DTOs
    public static class AssignRequest {
        private String assignedTo;
        private String taskDescription;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Date deadline;
        private String status = "assigned";
        private String priority = "medium";
        
        // Getters and Setters
        public String getAssignedTo() {
            return assignedTo;
        }
        
        public void setAssignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
        }
        
        public String getTaskDescription() {
            return taskDescription;
        }
        
        public void setTaskDescription(String taskDescription) {
            this.taskDescription = taskDescription;
        }
        
        public Date getDeadline() {
            return deadline;
        }
        
        public void setDeadline(Date deadline) {
            this.deadline = deadline;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getPriority() {
            return priority;
        }
        
        public void setPriority(String priority) {
            this.priority = priority;
        }

        @Override
        public String toString() {
            return "AssignRequest{" +
                    "assignedTo='" + assignedTo + '\'' +
                    ", taskDescription='" + taskDescription + '\'' +
                    ", deadline=" + deadline +
                    ", status='" + status + '\'' +
                    ", priority='" + priority + '\'' +
                    '}';
        }
    }
    
    public static class CompleteRequest {
        private String employeeNotes;
        
        public String getEmployeeNotes() {
            return employeeNotes;
        }
        
        public void setEmployeeNotes(String employeeNotes) {
            this.employeeNotes = employeeNotes;
        }
    }
 // Get file for enquiry
 // Update getEnquiryFile method in StudentEnquiryController.java
 // Update the file endpoint - SIMPLER VERSION
 // Update getEnquiryFile method in StudentEnquiryController.java
    @GetMapping("/enquiries/{id}/file")
    public ResponseEntity<byte[]> getEnquiryFile(@PathVariable String id) {
        try {
            logger.info("Fetching file for enquiry ID: {}", id);
            
            Optional<StudentEnquiry> enquiryOpt = studentEnquiryService.getEnquiryById(id);
            
            if (enquiryOpt.isEmpty()) {
                logger.warn("Enquiry not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            StudentEnquiry enquiry = enquiryOpt.get();
            
            // Check if file exists
            if (enquiry.getFileData() == null) {
                logger.warn("No file data found for enquiry ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Get bytes from Binary object
            byte[] fileBytes = enquiry.getFileData().getData();
            
            if (fileBytes == null || fileBytes.length == 0) {
                logger.warn("File data is empty for enquiry ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            logger.info("File data retrieved, size: {} bytes", fileBytes.length);
            
            // Determine content type
            String contentType = enquiry.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                // Try to detect from file bytes
                contentType = detectContentType(fileBytes);
                logger.info("Content type detected as: {}", contentType);
            }
            
            // Determine filename
            String filename = enquiry.getFileName();
            if (filename == null || filename.isEmpty()) {
                filename = "attachment";
            }
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename(filename)
                    .build()
            );
            headers.setContentLength(fileBytes.length);
            headers.setCacheControl("no-cache");
            
            logger.info("Successfully returning file for enquiry ID: {}, filename: {}, content-type: {}, size: {} bytes", 
                        id, filename, contentType, fileBytes.length);
            
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error fetching file for enquiry ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper method to detect content type from file bytes
    private String detectContentType(byte[] data) {
        if (data.length < 4) {
            return "application/octet-stream";
        }
        
        // Check for PNG
        if (data[0] == (byte) 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "image/png";
        }
        
        // Check for JPEG
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        
        // Check for GIF
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8') {
            return "image/gif";
        }
        
        // Check for PDF
        if (data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
            return "application/pdf";
        }
        
        return "application/octet-stream";
    }
    
 // Debug file endpoint
 // Add this to StudentEnquiryController.java
    @GetMapping("/enquiries/{id}/file-info")
    public ResponseEntity<?> getFileInfo(@PathVariable String id) {
        try {
            logger.info("Getting file info for enquiry ID: {}", id);
            
            Optional<StudentEnquiry> enquiryOpt = studentEnquiryService.getEnquiryById(id);
            
            if (enquiryOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Enquiry not found"));
            }
            
            StudentEnquiry enquiry = enquiryOpt.get();
            
            Map<String, Object> info = new HashMap<>();
            info.put("id", enquiry.getId());
            info.put("fileName", enquiry.getFileName());
            info.put("contentType", enquiry.getContentType());
            
            if (enquiry.getFileData() != null) {
                info.put("fileDataExists", true);
                info.put("fileDataType", enquiry.getFileData().getClass().getName());
                info.put("fileDataSize", enquiry.getFileData().getData().length);
                info.put("binaryType", enquiry.getFileData().getType());
                
                // Check first few bytes
                byte[] data = enquiry.getFileData().getData();
                if (data.length > 0) {
                    info.put("firstByteHex", String.format("%02X", data[0]));
                    if (data.length > 1) info.put("secondByteHex", String.format("%02X", data[1]));
                    if (data.length > 2) info.put("thirdByteHex", String.format("%02X", data[2]));
                    if (data.length > 3) info.put("fourthByteHex", String.format("%02X", data[3]));
                }
            } else {
                info.put("fileDataExists", false);
            }
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            logger.error("Error getting file info: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
 // Add this test endpoint FIRST
    @GetMapping("/test-file-endpoint")
    public ResponseEntity<?> testFileEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "File endpoint controller is working");
        response.put("timestamp", new Date());
        response.put("availableEndpoints", List.of(
            "GET /api/support/enquiries",
            "GET /api/support/enquiries/{id}",
            "GET /api/support/enquiries/{id}/file",
            "GET /api/support/enquiries/{id}/debug-file-info"
        ));
        return ResponseEntity.ok(response);
    }
}
