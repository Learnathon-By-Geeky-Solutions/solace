package dev.solace.twiggle.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // Group for Actuator API Endpoints
    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group("Actuator API")
                .pathsToMatch("/actuator/**") // Include all actuator endpoints
                .build();
    }

    // Group for Application API Endpoints
    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group("Application API")
                .pathsToMatch("/api/**") // Adjust based on your controllers' base paths
                .build();
    }
}
