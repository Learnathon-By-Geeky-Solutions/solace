package dev.solace.twiggle.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
    private static final String API_VERSION = "1.0";
    private static final String SECURITY_SCHEME_NAME = "oAuth2PasswordAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Twiggle API Documentation")
                        .description("API endpoints for Urban Garden Planner. Use the Authorize button to log in.")
                        .version(API_VERSION))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("OAuth2 Password Flow for authentication")
                                        .flows(new OAuthFlows()
                                                .password(new OAuthFlow()
                                                        .tokenUrl("/api/v1/auth/login")
                                                        .scopes(new io.swagger.v3.oas.models.security.Scopes())))));
    }

    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group(ACTUATOR_GROUP)
                .pathsToMatch(ACTUATOR_PATH_PATTERN)
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Actuator API Documentation")
                        .description("API endpoints for application monitoring and management")
                        .version(API_VERSION)))
                .build();
    }

    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group(APPLICATION_GROUP)
                .pathsToMatch(APPLICATION_PATH_PATTERN)
                .build();
    }
}
