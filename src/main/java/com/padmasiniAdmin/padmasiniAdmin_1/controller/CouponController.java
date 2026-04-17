package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Coupon;
import com.padmasiniAdmin.padmasiniAdmin_1.service.CouponService;
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
@RequestMapping("/api/coupons")
@CrossOrigin(origins = {"http://localhost:3000", "https://trilokinnovations.com"}, 
allowCredentials = "true",
maxAge = 3600)
public class CouponController {
    
    private static final Logger logger = LoggerFactory.getLogger(CouponController.class);
    
    @Autowired
    private CouponService couponService;
    
    // Create new coupon
    @PostMapping
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        try {
            logger.info("Creating new coupon: {}", coupon.getCouponCode());
            
            // Validate required fields
            if (coupon.getCouponCode() == null || coupon.getCouponCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Coupon code is required"));
            }
            
            if (coupon.getOrganizationName() == null || coupon.getOrganizationName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Organization name is required"));
            }
            
            if (coupon.getDiscountPercentage() == null || coupon.getDiscountPercentage() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid discount percentage is required"));
            }
            
            if (coupon.getMaxMembers() == null || coupon.getMaxMembers() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid max members is required"));
            }
            
            if (coupon.getValidityStartDate() == null || coupon.getValidityEndDate() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validity dates are required"));
            }
            
            // Validate dates
            if (coupon.getValidityEndDate().before(coupon.getValidityStartDate())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "End date must be after start date"));
            }
            
            Coupon createdCoupon = couponService.createCoupon(coupon);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Coupon created successfully");
            response.put("data", createdCoupon);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error creating coupon: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Get all coupons
    @GetMapping
    public ResponseEntity<?> getAllCoupons() {
        try {
            logger.info("Fetching all coupons");
            
            List<Coupon> coupons = couponService.getAllCoupons();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", coupons);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching coupons: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Get coupon by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCouponById(@PathVariable String id) {
        try {
            logger.info("Fetching coupon by ID: {}", id);
            
            Coupon coupon = couponService.getCouponById(id);
            if (coupon != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("data", coupon);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Coupon not found"));
            }
            
        } catch (Exception e) {
            logger.error("Error fetching coupon {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Update coupon
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable String id, @RequestBody Coupon coupon) {
        try {
            logger.info("Updating coupon: {}", id);
            
            Coupon updatedCoupon = couponService.updateCoupon(id, coupon);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Coupon updated successfully");
            response.put("data", updatedCoupon);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating coupon {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Delete coupon
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable String id) {
        try {
            logger.info("Deleting coupon: {}", id);
            
            boolean deleted = couponService.deleteCoupon(id);
            
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Coupon deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Coupon not found"));
            }
            
        } catch (Exception e) {
            logger.error("Error deleting coupon {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Validate coupon
    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateCoupon(@PathVariable String code) {
        try {
            logger.info("Validating coupon: {}", code);
            
            Coupon coupon = couponService.validateCoupon(code);
            
            if (coupon != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("valid", true);
                response.put("data", coupon);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("valid", false);
                response.put("message", "Invalid or expired coupon");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            logger.error("Error validating coupon {}: ", code, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Increment usage
    @PostMapping("/{id}/use")
    public ResponseEntity<?> useCoupon(
            @PathVariable String id,
            @RequestBody(required = false) UsageRequest request) {
        try {
            logger.info("Incrementing usage for coupon: {}", id);
            
            int memberCount = request != null && request.getMemberCount() != null 
                    ? request.getMemberCount() : 1;
            
            if (memberCount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "Member count must be positive"));
            }
            
            // First validate coupon
            Coupon coupon = couponService.getCouponById(id);
            if (coupon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Coupon not found"));
            }
            
            Coupon validated = couponService.validateCoupon(coupon.getCouponCode());
            if (validated == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "Coupon is not valid for use"));
            }
            
            // Increment usage
            Coupon updated = couponService.incrementUsage(id, memberCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Coupon usage recorded successfully");
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error using coupon {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Get statistics
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            logger.info("Fetching coupon statistics");
            
            CouponService.CouponStatistics stats = couponService.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching statistics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Coupon API is working");
        response.put("timestamp", new Date());
        response.put("availableEndpoints", List.of(
            "POST /api/coupons - Create coupon",
            "GET /api/coupons - Get all coupons",
            "GET /api/coupons/{id} - Get coupon by ID",
            "PUT /api/coupons/{id} - Update coupon",
            "DELETE /api/coupons/{id} - Delete coupon",
            "GET /api/coupons/validate/{code} - Validate coupon",
            "POST /api/coupons/{id}/use - Use coupon",
            "GET /api/coupons/statistics - Get statistics"
        ));
        return ResponseEntity.ok(response);
    }
    
    // Health check
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "coupon-service");
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }
    
    // Request DTOs
    public static class UsageRequest {
        private Integer memberCount;
        
        public Integer getMemberCount() { return memberCount; }
        public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    }
}
