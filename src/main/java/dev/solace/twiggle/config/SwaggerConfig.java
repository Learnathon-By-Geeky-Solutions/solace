package dev.solace.twiggle.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger API documentation.
 */
@Configuration
public class SwaggerConfig {

    private static final String ACTUATOR_GROUP = "Actuator API";
    private static final String APPLICATION_GROUP = "Application API";
    private static final String ACTUATOR_PATH_PATTERN = "/actuator/**";
    private static final String APPLICATION_PATH_PATTERN = "/api/**";

    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group(ACTUATOR_GROUP)
                .pathsToMatch(ACTUATOR_PATH_PATTERN)
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Actuator API Documentation")
                        .description("API endpoints for application monitoring and management")
                        .version("1.0")))
                .build();
    }

    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group(APPLICATION_GROUP)
                .pathsToMatch(APPLICATION_PATH_PATTERN)
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Twiggle API Documentation")
                        .description("API endpoints for Urban Garden Planner")
                        .version("1.0")))
                .build();
    }
}
