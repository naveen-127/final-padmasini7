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
               "https://api.trilokinnovations.com",
               "https://d3ty37mf4sf9cz.cloudfront.net",
               "http://3.91.243.188:3000",
               "https://majestic-frangollo-031fed.netlify.app",
               "https://classy-kulfi-cddfeb.netlify.app",
               "https://padmasini7-frontend.netlify.app",
               "http://localhost:5173",
               "http://localhost:5174", 
               "https://ai-generative-rhk1.onrender.com",
               "https://ai-generative-1.onrender.com",
               "http://localhost:*",
               "https://*.netlify.app",
               "https://www.trilokinnovations.com",
               "http://www.trilokinnovations.com", // Add HTTP version
               "https://trilokinnovations.com", // Add without www
               "http://trilokinnovations.com", // Add HTTP without www
               "http://trilokinnovations.s3-website.ap-south-1.amazonaws.com",
               "https://dafj1druksig9.cloudfront.net" // ← No trailing comma on last item
)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
