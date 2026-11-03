package com.parikshaguru.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Clock;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private final AppProperties props;

    /** Injectable clock: all exam timing uses server time from this bean. */
    @Bean
    public Clock clock() { return Clock.systemUTC(); }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("ParikshaGuru API").version("1.0.0")
                        .description("Central Examination & Proctoring Portal - Spring Boot REST API"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Configure from properties or environment
        if (props.getCors().getAllowedOrigins() != null && !props.getCors().getAllowedOrigins().isEmpty()) {
            config.setAllowedOrigins(props.getCors().getAllowedOrigins());
        } else {
            // Default for development
            config.addAllowedOrigin("http://localhost:3000");
            config.addAllowedOrigin("http://localhost:3001");
        }

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
