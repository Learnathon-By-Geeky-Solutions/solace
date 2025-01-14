package dev.solace.twiggle.config;

import static org.junit.jupiter.api.Assertions.*;

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

        // Then
        assertNotNull(actuatorApi, "Actuator API configuration should not be null");
        assertEquals("Actuator API", actuatorApi.getGroup(), "Group name should match");
        assertTrue(actuatorApi.getPathsToMatch().contains("/actuator/**"), "Should include actuator path pattern");
    }

    @Test
    void applicationApi_ShouldReturnCorrectlyConfiguredGroupedOpenApi() {
        // When
        GroupedOpenApi applicationApi = swaggerConfig.applicationApi();

        // Then
        assertNotNull(applicationApi, "Application API configuration should not be null");
        assertEquals("Application API", applicationApi.getGroup(), "Group name should match");
        assertTrue(applicationApi.getPathsToMatch().contains("/api/**"), "Should include API path pattern");
    }
}
