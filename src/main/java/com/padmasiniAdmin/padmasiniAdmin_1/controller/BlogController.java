package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.padmasiniAdmin.padmasiniAdmin_1.model.BlogModel;
import com.padmasiniAdmin.padmasiniAdmin_1.service.BlogImageService;
import com.padmasiniAdmin.padmasiniAdmin_1.service.BlogService;
import com.padmasiniAdmin.padmasiniAdmin_1.service.BlogService.BlogException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Admin-only blog endpoints. The public read API (GET /api/blogs, /api/blogs/{slug})
 * belongs to the student-side backend.
 */
@RestController
@RequestMapping("/api/admin/blogs")
public class BlogController {

    private final BlogService blogService;
    private final BlogImageService imageService;

    public BlogController(BlogService blogService, BlogImageService imageService) {
        this.blogService = blogService;
        this.imageService = imageService;
    }

    @GetMapping
    public List<Map<String, Object>> list(HttpServletRequest request) {
        requireAdmin(request);
        return blogService.list();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable String id, HttpServletRequest request) {
        requireAdmin(request);
        return blogService.get(id);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody BlogModel body, HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.create(body));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable String id, @RequestBody BlogModel body,
                                      HttpServletRequest request) {
        requireAdmin(request);
        return blogService.update(id, body);
    }

    @PatchMapping("/{id}/status")
    public Map<String, Object> setStatus(@PathVariable String id, @RequestBody Map<String, String> body,
                                         HttpServletRequest request) {
        requireAdmin(request);
        return blogService.setStatus(id, body == null ? null : body.get("status"));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable String id, HttpServletRequest request) {
        requireAdmin(request);
        blogService.delete(id);
        return Map.of("message", "Deleted");
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        requireAdmin(request);
        return Map.of("url", imageService.upload(file));
    }

    /**
     * Admin check. Right now this only requires a signed-in session.
     * Tighten it to match /checkSession, for example by also checking the role stored in the session.
     */
    private void requireAdmin(HttpServletRequest request) {
        if (request.getSession(false) == null) {
            throw new BlogException(HttpStatus.UNAUTHORIZED, "Please sign in as admin.");
        }
    }

    @ExceptionHandler(BlogException.class)
    public ResponseEntity<Map<String, String>> handle(BlogException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}