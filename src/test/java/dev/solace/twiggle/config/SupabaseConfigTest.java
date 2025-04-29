package dev.solace.twiggle.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SupabaseConfigTest {

    @Test
    void testSupabaseConfigGettersAndSetters() {
        // Create instance
        SupabaseConfig config = new SupabaseConfig();

        // Test data
        String serviceRoleKey = "test-service-role-key";
        String projectId = "test-project-id";

        // Set values
        config.setServiceRoleKey(serviceRoleKey);
        config.setProjectId(projectId);

        // Verify getters
        assertEquals(serviceRoleKey, config.getServiceRoleKey());
        assertEquals(projectId, config.getProjectId());
    }

    @Test
    void testSupabaseConfigValidation() {
        // Create instance
        SupabaseConfig config = new SupabaseConfig();

        // Test with null values (should not throw validation errors as @Validated is
        // handled by Spring)
        assertDoesNotThrow(() -> {
            config.setServiceRoleKey(null);
            config.setProjectId(null);
        });

        // Test with empty strings (should not throw validation errors as @Validated is
        // handled by Spring)
        assertDoesNotThrow(() -> {
            config.setServiceRoleKey("");
            config.setProjectId("");
        });
    }
}
