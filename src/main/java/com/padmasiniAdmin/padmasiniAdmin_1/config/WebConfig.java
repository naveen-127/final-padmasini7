package com.padmasiniAdmin.padmasiniAdmin_1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Single CORS configuration for all endpoints
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "https://majestic-frangollo-031fed.netlify.app",
                    "https://classy-kulfi-cddfef.netlify.app",
                    "https://padmasini7-frontend.netlify.app",
                    "http://localhost:5173",
                    "http://localhost:5174", 
                    "https://ai-generative-rhk1.onrender.com",
                    "https://ai-generative-1.onrender.com",
                    "http://localhost:*", // Allow any localhost port
                    "https://*.netlify.app", // Allow all Netlify subdomains
                    "https://www.trilokinnovations.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
