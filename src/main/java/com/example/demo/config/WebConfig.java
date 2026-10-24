package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configurare MVC:
 * 1. permite cererile venite din frontend-ul Vue (CORS pentru orice origine);
 * 2. serveste pozele doveditoare salvate in folderul uploads/ la adresa /uploads/**.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploads = Paths.get("uploads").toAbsolutePath();
        try {
            Files.createDirectories(uploads);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create uploads directory", ex);
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(uploads.toUri().toString());
    }
}
