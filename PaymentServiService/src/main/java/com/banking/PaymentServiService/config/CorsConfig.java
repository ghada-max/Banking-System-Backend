package com.banking.PaymentServiService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig { // 1. Première lettre en majuscule

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) { // 2. Ajout du 's' à addCorsMappings
                registry.addMapping("/api/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // 3. "PU" corrigé en "PUT"
                        .allowedHeaders("*"); // 4. Doublon supprimé
            }
        }; // 5. Point-virgule obligatoire ici !
    }
}