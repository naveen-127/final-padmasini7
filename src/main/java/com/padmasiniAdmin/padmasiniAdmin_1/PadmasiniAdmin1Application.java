package com.padmasiniAdmin.padmasiniAdmin_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class PadmasiniAdmin1Application {

    public static void main(String[] args) {
        SpringApplication.run(PadmasiniAdmin1Application.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // Use allowedOriginPatterns to support wildcards with credentials
                        .allowedOriginPatterns(
                            "http://localhost:5173",
                            "https://*.trilokinnovations.com",
                            "https://padmasini7-frontend.netlify.app",
                            "https://*.cloudfront.net"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600); // optional: cache preflight requests for 1 hour
            }
        };
    }
}
