package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;

/**
 * Request body for creating / updating a blog post.
 * Field names are a contract with the student website. Do not rename them.
 * publishedAt is an ISO-8601 string (for example 2026-11-01T09:00:00Z). The admin UI
 * sends it only when marketing back-dates a post. It is stored as a BSON date.
 */
public record BlogModel(
        String category,
        String title,
        String slug,
        String shortDescription,
        String image,
        String imageAlt,
        String content,
        String metaTitle,
        String metaDescription,
        List<String> keywords,
        String status,
        String publishedAt) {

    public static final String DRAFT = "DRAFT";
    public static final String PUBLISHED = "PUBLISHED";
}