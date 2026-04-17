package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

@Document(collection = "Coupons")
public class Coupon {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    @Field("couponCode")
    private String couponCode;
    
    @Field("organizationName")
    private String organizationName;
    
    @Field("discountPercentage")
    private Double discountPercentage;
    
    @Field("validityStartDate")
    private Date validityStartDate;
    
    @Field("validityEndDate")
    private Date validityEndDate;
    
    
    @Field("maxMembers")
    private Integer maxMembers;
    
    @Field("usedMembers")
    private Integer usedMembers = 0;
    
    @Field("description")
    private String description;
    
    @Field("isActive")
    private Boolean isActive = true;
    
    @Field("createdAt")
    private Date createdAt;
    
    @Field("updatedAt")
    private Date updatedAt;
    
    // Constructors
    public Coupon() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    
    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public Date getValidityStartDate() { return validityStartDate; }
    public void setValidityStartDate(Date validityStartDate) { this.validityStartDate = validityStartDate; }
    
    public Date getValidityEndDate() { return validityEndDate; }
    public void setValidityEndDate(Date validityEndDate) { this.validityEndDate = validityEndDate; }
    
    
    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    
    public Integer getUsedMembers() { return usedMembers; }
    public void setUsedMembers(Integer usedMembers) { this.usedMembers = usedMembers; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "Coupon{" +
                "id='" + id + '\'' +
                ", couponCode='" + couponCode + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", discountPercentage=" + discountPercentage +
                ", isActive=" + isActive +
                '}';
    }
}
