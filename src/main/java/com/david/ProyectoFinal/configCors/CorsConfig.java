package com.david.ProyectoFinal.configCors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.frontend-url:http://localhost:8081}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(construirOrigenesPermitidos().toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    private List<String> construirOrigenesPermitidos() {
        List<String> origenes = new ArrayList<>(List.of(
                "http://localhost",
                "http://localhost:*",
                "http://127.0.0.1",
                "http://127.0.0.1:*",
                "http://209.97.178.50",
                "http://209.97.178.50:*"
        ));

        String origenFrontend = extraerOrigen(frontendUrl);

        if (origenFrontend != null && !origenFrontend.isBlank()) {
            origenes.add(origenFrontend);
        }

        return origenes;
    }

    private String extraerOrigen(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(url.trim());

            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }

            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
        } catch (Exception e) {
            return null;
        }
    }
}
