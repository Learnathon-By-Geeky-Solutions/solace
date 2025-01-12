package dev.solace.twiggle.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Creates a Swagger/OpenAPI group configuration for Actuator API endpoints.
     *
     * @return A {@code GroupedOpenApi} instance configured for Actuator API documentation
     * @see GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group("Actuator API")
                .pathsToMatch("/actuator/**") // Include all actuator endpoints
                .build();
    }

    /**
     * Creates a Swagger/OpenAPI group configuration for Application API endpoints.
     *
     * @return A {@code GroupedOpenApi} instance configured for application-specific API endpoints
     * @see GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group("Application API")
                .pathsToMatch("/api/**") // Adjust based on your controllers' base paths
                .build();
    }
}
