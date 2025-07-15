package com.example.bitirmeprojemfinal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Handle static resources from classpath:/static/
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        // Handle uploaded images from external directory
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // Cache for 1 hour
    }
} 