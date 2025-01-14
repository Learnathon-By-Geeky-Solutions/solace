package dev.solace.twiggle.config;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

/**
 * Unit tests for the {@link SwaggerConfig} class.
 */
class SwaggerConfigTest {

    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    void actuatorApi_ShouldReturnCorrectlyConfiguredGroupedOpenApi() {
        // When
        GroupedOpenApi actuatorApi = swaggerConfig.actuatorApi();
        OpenAPI openAPI = new OpenAPI();
        actuatorApi.getOpenApiCustomizers().forEach(customizer -> customizer.customise(openAPI));

        // Then
        assertNotNull(actuatorApi, "Actuator API configuration should not be null");
        assertEquals("Actuator API", actuatorApi.getGroup(), "Group name should match");
        assertTrue(actuatorApi.getPathsToMatch().contains("/actuator/**"), "Should include actuator path pattern");

        // Verify OpenAPI metadata
        Info info = openAPI.getInfo();
        assertNotNull(info, "OpenAPI Info should not be null");
        assertEquals("Actuator API Documentation", info.getTitle(), "Title should match");
        assertEquals(
                "API endpoints for application monitoring and management",
                info.getDescription(),
                "Description should match");
        assertEquals("1.0", info.getVersion(), "Version should match");
    }

    @Test
    void applicationApi_ShouldReturnCorrectlyConfiguredGroupedOpenApi() {
        // When
        GroupedOpenApi applicationApi = swaggerConfig.applicationApi();
        OpenAPI openAPI = new OpenAPI();
        applicationApi.getOpenApiCustomizers().forEach(customizer -> customizer.customise(openAPI));

        // Then
        assertNotNull(applicationApi, "Application API configuration should not be null");
        assertEquals("Application API", applicationApi.getGroup(), "Group name should match");
        assertTrue(applicationApi.getPathsToMatch().contains("/api/**"), "Should include API path pattern");

        // Verify OpenAPI metadata
        Info info = openAPI.getInfo();
        assertNotNull(info, "OpenAPI Info should not be null");
        assertEquals("Twiggle API Documentation", info.getTitle(), "Title should match");
        assertEquals("API endpoints for Urban Garden Planner", info.getDescription(), "Description should match");
        assertEquals("1.0", info.getVersion(), "Version should match");
    }
}
