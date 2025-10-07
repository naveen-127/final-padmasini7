package com.padmasiniAdmin.padmasiniAdmin_1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/")
                        // ✅ Exact origins used for dev + Netlify frontend
                        .allowedOrigins(
                            "http://localhost:5173",
                            "http://localhost:5174",
                            "https://padmasini7-frontend.netlify.app",
                            "https://d2kr3vc90ue6me.cloudfront.net"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true); // important for session cookies
            }
        };
    }
}
