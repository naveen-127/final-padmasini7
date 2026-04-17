package com.padmasiniAdmin.padmasiniAdmin_1.service;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Coupon;
import com.mongodb.client.MongoClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CouponService {
    
    @Autowired
    private MongoClient mongoClient;
    
    private MongoTemplate couponTemplate;
    
    private final String databaseName = "studentUsers"; // Same as PeopleEnquiry
    private final String collectionName = "Coupons";
    
    @PostConstruct
    public void init() {
        try {
            System.out.println("🚀 Initializing CouponService...");
            System.out.println("📊 Connecting to database: " + databaseName);
            
            // Create MongoTemplate
            this.couponTemplate = new MongoTemplate(mongoClient, databaseName);
            System.out.println("✅ CouponService initialized successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing CouponService: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize CouponService", e);
        }
    }
    
    // Create new coupon
    public Coupon createCoupon(Coupon coupon) {
        try {
            System.out.println("🎫 Creating new coupon: " + coupon.getCouponCode());
            
            // Check if coupon code already exists
            if (couponExistsByCode(coupon.getCouponCode())) {
                throw new RuntimeException("Coupon code '" + coupon.getCouponCode() + "' already exists");
            }
            
            // Set timestamps
            coupon.setCreatedAt(new Date());
            coupon.setUpdatedAt(new Date());
            
            if (coupon.getUsedMembers() == null) coupon.setUsedMembers(0);
            if (coupon.getIsActive() == null) coupon.setIsActive(true);
            
            // Save coupon
            Coupon savedCoupon = couponTemplate.save(coupon, collectionName);
            System.out.println("✅ Coupon created successfully with ID: " + savedCoupon.getId());
            
            return savedCoupon;
            
        } catch (Exception e) {
            System.err.println("❌ Error creating coupon: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create coupon: " + e.getMessage(), e);
        }
    }
    
    // Get all coupons
    public List<Coupon> getAllCoupons() {
        try {
            System.out.println("🔍 Fetching all coupons...");
            
            Query query = new Query();
            query.with(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
            ));
            
            List<Coupon> coupons = couponTemplate.find(query, Coupon.class, collectionName);
            System.out.println("✅ Found " + coupons.size() + " coupons");
            
            return coupons;
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching coupons: " + e.getMessage());
            throw new RuntimeException("Failed to fetch coupons: " + e.getMessage(), e);
        }
    }
    
    // Get coupon by ID
    public Coupon getCouponById(String id) {
        try {
            Query query = new Query(Criteria.where("_id").is(id));
            return couponTemplate.findOne(query, Coupon.class, collectionName);
        } catch (Exception e) {
            System.err.println("Error fetching coupon by ID " + id + ": " + e.getMessage());
            throw e;
        }
    }
    
    // Get coupon by code
    public Coupon getCouponByCode(String couponCode) {
        try {
            Query query = new Query(Criteria.where("couponCode").is(couponCode));
            return couponTemplate.findOne(query, Coupon.class, collectionName);
        } catch (Exception e) {
            System.err.println("Error fetching coupon by code " + couponCode + ": " + e.getMessage());
            throw e;
        }
    }
    
    // Update coupon
    public Coupon updateCoupon(String id, Coupon couponDetails) {
        try {
            System.out.println("✏️ Updating coupon: " + id);
            
            Coupon existingCoupon = getCouponById(id);
            if (existingCoupon == null) {
                throw new RuntimeException("Coupon not found with id: " + id);
            }
            
            // Check if coupon code is being changed and already exists
            if (couponDetails.getCouponCode() != null && 
                !couponDetails.getCouponCode().equals(existingCoupon.getCouponCode()) &&
                couponExistsByCode(couponDetails.getCouponCode())) {
                throw new RuntimeException("Coupon code '" + couponDetails.getCouponCode() + "' already exists");
            }
            
            // Update fields
            Update update = new Update();
            
            if (couponDetails.getOrganizationName() != null) {
                update.set("organizationName", couponDetails.getOrganizationName());
            }
            if (couponDetails.getCouponCode() != null) {
                update.set("couponCode", couponDetails.getCouponCode());
            }
            if (couponDetails.getDiscountPercentage() != null) {
                update.set("discountPercentage", couponDetails.getDiscountPercentage());
            }
            if (couponDetails.getValidityStartDate() != null) {
                update.set("validityStartDate", couponDetails.getValidityStartDate());
            }
            if (couponDetails.getValidityEndDate() != null) {
                update.set("validityEndDate", couponDetails.getValidityEndDate());
            }
            if (couponDetails.getMaxMembers() != null) {
                update.set("maxMembers", couponDetails.getMaxMembers());
            }
            if (couponDetails.getDescription() != null) {
                update.set("description", couponDetails.getDescription());
            }
            if (couponDetails.getIsActive() != null) {
                update.set("isActive", couponDetails.getIsActive());
            }
            
            // Always update timestamp
            update.set("updatedAt", new Date());
            
            Query query = new Query(Criteria.where("_id").is(id));
            couponTemplate.updateFirst(query, update, Coupon.class, collectionName);
            
            System.out.println("✅ Coupon updated successfully");
            return getCouponById(id);
            
        } catch (Exception e) {
            System.err.println("❌ Error updating coupon: " + e.getMessage());
            throw e;
        }
    }
    
    // Delete coupon
    public boolean deleteCoupon(String id) {
        try {
            System.out.println("🗑️ Deleting coupon: " + id);
            
            Query query = new Query(Criteria.where("_id").is(id));
            Coupon deleted = couponTemplate.findAndRemove(query, Coupon.class, collectionName);
            
            if (deleted != null) {
                System.out.println("✅ Coupon deleted successfully");
                return true;
            } else {
                System.out.println("⚠️ Coupon not found for deletion");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting coupon: " + e.getMessage());
            throw e;
        }
    }
    
    // Validate coupon
 // Validate coupon
    public Coupon validateCoupon(String couponCode) {
        try {
            System.out.println("🔍 Validating coupon: " + couponCode);
            
            Coupon coupon = getCouponByCode(couponCode);
            if (coupon == null) {
                System.out.println("❌ Coupon not found: " + couponCode);
                return null;
            }
            
            // Check if coupon is active
            if (!Boolean.TRUE.equals(coupon.getIsActive())) {
                System.out.println("❌ Coupon is inactive: " + couponCode);
                return null;
            }
            
            Date now = new Date();
            
            // Check validity dates
            if (coupon.getValidityStartDate() != null && now.before(coupon.getValidityStartDate())) {
                System.out.println("❌ Coupon not yet valid: " + couponCode);
                return null;
            }
            
            if (coupon.getValidityEndDate() != null && now.after(coupon.getValidityEndDate())) {
                System.out.println("❌ Coupon expired: " + couponCode);
                // Auto-expire the coupon
                autoExpireCoupon(coupon.getId());
                return null;
            }
            
            // Check max members - if reached, expire the coupon
            if (coupon.getMaxMembers() != null && coupon.getUsedMembers() >= coupon.getMaxMembers()) {
                System.out.println("❌ Coupon member limit reached: " + couponCode);
                // Auto-expire the coupon
                autoExpireCoupon(coupon.getId());
                return null;
            }
            
            System.out.println("✅ Coupon validated successfully: " + couponCode);
            return coupon;
            
        } catch (Exception e) {
            System.err.println("❌ Error validating coupon: " + e.getMessage());
            throw e;
        }
    }
    
 // Auto-expire coupon when max members reached or expired
    private void autoExpireCoupon(String couponId) {
        try {
            System.out.println("🔴 Auto-expiring coupon: " + couponId);
            
            Update update = new Update();
            update.set("isActive", false);
            update.set("updatedAt", new Date());
            
            Query query = new Query(Criteria.where("_id").is(couponId));
            couponTemplate.updateFirst(query, update, Coupon.class, collectionName);
            
            System.out.println("✅ Coupon auto-expired successfully: " + couponId);
            
        } catch (Exception e) {
            System.err.println("❌ Error auto-expiring coupon: " + e.getMessage());
            // Don't throw here to avoid affecting validation flow
        }
    }
    // Increment coupon usage
 // Increment coupon usage
    public Coupon incrementUsage(String couponId, int memberCount) {
        try {
            System.out.println("📈 Incrementing usage for coupon: " + couponId);
            
            Coupon coupon = getCouponById(couponId);
            if (coupon == null) {
                throw new RuntimeException("Coupon not found: " + couponId);
            }
            
            Update update = new Update();
            update.inc("usedCount", 1);
            update.inc("usedMembers", memberCount);
            update.set("updatedAt", new Date());
            
            Query query = new Query(Criteria.where("_id").is(couponId));
            couponTemplate.updateFirst(query, update, Coupon.class, collectionName);
            
            // Get updated coupon
            Coupon updatedCoupon = getCouponById(couponId);
            
            // Check if max members reached after increment
            if (updatedCoupon.getMaxMembers() != null && 
                updatedCoupon.getUsedMembers() >= updatedCoupon.getMaxMembers()) {
                System.out.println("🎯 Max members reached, auto-expiring coupon: " + couponId);
                autoExpireCoupon(couponId);
            }
            
            System.out.println("✅ Usage incremented successfully");
            return getCouponById(couponId);
            
        } catch (Exception e) {
            System.err.println("❌ Error incrementing usage: " + e.getMessage());
            throw e;
            }
        }
    // Get coupon statistics
    public CouponStatistics getStatistics() {
        try {
            System.out.println("📊 Getting coupon statistics...");
            
            List<Coupon> allCoupons = getAllCoupons();
            
            CouponStatistics stats = new CouponStatistics();
            stats.setTotal(allCoupons.size());
            stats.setActive((int) allCoupons.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .count());
            stats.setExpired((int) allCoupons.stream()
                .filter(c -> c.getValidityEndDate() != null && new Date().after(c.getValidityEndDate()))
                .count());
            stats.setUpcoming((int) allCoupons.stream()
                .filter(c -> c.getValidityStartDate() != null && new Date().before(c.getValidityStartDate()))
                .count());
            
            System.out.println("✅ Statistics generated: " + stats);
            return stats;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting statistics: " + e.getMessage());
            throw e;
        }
    }
    
    // Check if coupon exists by code
    private boolean couponExistsByCode(String couponCode) {
        try {
            Query query = new Query(Criteria.where("couponCode").is(couponCode));
            return couponTemplate.exists(query, Coupon.class, collectionName);
        } catch (Exception e) {
            System.err.println("Error checking coupon existence: " + e.getMessage());
            return false;
        }
    }
    
    // Statistics DTO
    public static class CouponStatistics {
        private int total;
        private int active;
        private int expired;
        private int upcoming;
        
        // Getters and Setters
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        
        public int getActive() { return active; }
        public void setActive(int active) { this.active = active; }
        
        public int getExpired() { return expired; }
        public void setExpired(int expired) { this.expired = expired; }
        
        public int getUpcoming() { return upcoming; }
        public void setUpcoming(int upcoming) { this.upcoming = upcoming; }
      
        
        @Override
        public String toString() {
            return "CouponStatistics{" +
                    "total=" + total +
                    ", active=" + active +
                    ", expired=" + expired +
                    ", upcoming=" + upcoming +
                    '}';
        }
    }
}
