package com.padmasiniAdmin.padmasiniAdmin_1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
           .allowedOriginPatterns(
               "https://trilokinnovations.com",
               "https://www.trilokinnovations.com",
               "http://trilokinnovations.com",
               "http://www.trilokinnovations.com",
               "https://dafj1druksig9.cloudfront.net",
               "https://d3ty37mf4sf9cz.cloudfront.net",
               "https://api.trilokinnovations.com",
               "http://localhost:5173",
               "http://localhost:5174",
               "http://localhost:3000",
               "https://*.netlify.app",
               "https://ai-generative-*.onrender.com"
           )
           .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
           .allowedHeaders("*")
           .allowCredentials(true)
           .maxAge(3600);
    }
}
