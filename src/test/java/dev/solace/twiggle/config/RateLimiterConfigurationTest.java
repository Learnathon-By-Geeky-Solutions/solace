package dev.solace.twiggle.config;

import static org.junit.jupiter.api.Assertions.*;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link RateLimiterConfiguration} class.
 */
class RateLimiterConfigurationTest {

    private RateLimiterConfiguration configuration;
    private RateLimiterRegistry rateLimiterRegistry;

    @BeforeEach
    void setUp() {
        configuration = new RateLimiterConfiguration();
        rateLimiterRegistry = configuration.rateLimiterRegistry();
    }

    @Test
    void rateLimiterRegistry_ShouldCreateWithDefaultConfig() {
        // When
        RateLimiterRegistry registry = configuration.rateLimiterRegistry();
        RateLimiterConfig config = registry.getDefaultConfig();

        // Then
        assertNotNull(registry, "Registry should not be null");
        assertEquals(300, config.getLimitForPeriod(), "Default limit for period should be 300");
        assertEquals(
                Duration.ofMinutes(1), config.getLimitRefreshPeriod(), "Default refresh period should be 1 minute");
        assertEquals(Duration.ZERO, config.getTimeoutDuration(), "Default timeout duration should be ZERO");
    }

    @Test
    void defaultRateLimiterConfig_ShouldCreateCorrectConfig() {
        // When
        RateLimiterConfig config = configuration.defaultRateLimiterConfig();

        // Then
        assertNotNull(config, "Default config should not be null");
        assertEquals(300, config.getLimitForPeriod(), "Default limit for period should be 300");
        assertEquals(
                Duration.ofMinutes(1), config.getLimitRefreshPeriod(), "Default refresh period should be 1 minute");
        assertEquals(Duration.ZERO, config.getTimeoutDuration(), "Default timeout duration should be ZERO");
    }

    @Test
    void standardApiLimiter_ShouldCreateWithCorrectConfig() {
        // When
        RateLimiter limiter = configuration.standardApiLimiter(rateLimiterRegistry);

        // Then
        assertNotNull(limiter, "Standard API limiter should not be null");
        assertEquals("standard-api", limiter.getName(), "Limiter name should match");
        assertEquals(300, limiter.getRateLimiterConfig().getLimitForPeriod(), "Limit for period should be 300");
        assertEquals(
                Duration.ofMinutes(1),
                limiter.getRateLimiterConfig().getLimitRefreshPeriod(),
                "Refresh period should be 1 minute");
    }

    @Test
    void testErrorLimiter_ShouldCreateWithCorrectConfig() {
        // When
        RateLimiter limiter = configuration.testErrorLimiter(rateLimiterRegistry);
        RateLimiterConfig config = limiter.getRateLimiterConfig();

        // Then
        assertNotNull(limiter, "Test error limiter should not be null");
        assertEquals("test-error", limiter.getName(), "Limiter name should match");
        assertEquals(30, config.getLimitForPeriod(), "Limit for period should be 30");
        assertEquals(Duration.ofSeconds(10), config.getLimitRefreshPeriod(), "Refresh period should be 10 seconds");
        assertEquals(Duration.ZERO, config.getTimeoutDuration(), "Timeout duration should be ZERO");

        // Verify that the configuration is built correctly
        RateLimiterConfig customConfig = RateLimiterConfig.custom()
                .limitForPeriod(30)
                .limitRefreshPeriod(Duration.ofSeconds(10))
                .timeoutDuration(Duration.ZERO)
                .build();
        assertEquals(customConfig.getLimitForPeriod(), config.getLimitForPeriod());
        assertEquals(customConfig.getLimitRefreshPeriod(), config.getLimitRefreshPeriod());
        assertEquals(customConfig.getTimeoutDuration(), config.getTimeoutDuration());
    }

    @Test
    void actuatorLimiter_ShouldCreateWithCorrectConfig() {
        // When
        RateLimiter limiter = configuration.actuatorLimiter(rateLimiterRegistry);
        RateLimiterConfig config = limiter.getRateLimiterConfig();

        // Then
        assertNotNull(limiter, "Actuator limiter should not be null");
        assertEquals("actuator", limiter.getName(), "Limiter name should match");
        assertEquals(60, config.getLimitForPeriod(), "Limit for period should be 60");
        assertEquals(Duration.ofMinutes(1), config.getLimitRefreshPeriod(), "Refresh period should be 1 minute");
        assertEquals(Duration.ZERO, config.getTimeoutDuration(), "Timeout duration should be ZERO");

        // Verify that the configuration is built correctly
        RateLimiterConfig customConfig = RateLimiterConfig.custom()
                .limitForPeriod(60)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();
        assertEquals(customConfig.getLimitForPeriod(), config.getLimitForPeriod());
        assertEquals(customConfig.getLimitRefreshPeriod(), config.getLimitRefreshPeriod());
        assertEquals(customConfig.getTimeoutDuration(), config.getTimeoutDuration());
    }

    @Test
    void multipleRateLimiters_ShouldCreateIndependentInstances() {
        // When
        RateLimiter standardLimiter = configuration.standardApiLimiter(rateLimiterRegistry);
        RateLimiter errorLimiter = configuration.testErrorLimiter(rateLimiterRegistry);
        RateLimiter actuatorLimiter = configuration.actuatorLimiter(rateLimiterRegistry);

        // Then
        assertNotNull(standardLimiter, "Standard limiter should not be null");
        assertNotNull(errorLimiter, "Error limiter should not be null");
        assertNotNull(actuatorLimiter, "Actuator limiter should not be null");

        // Verify they are different instances with different configurations
        assertNotEquals(
                standardLimiter.getRateLimiterConfig().getLimitForPeriod(),
                errorLimiter.getRateLimiterConfig().getLimitForPeriod());
        assertNotEquals(
                standardLimiter.getRateLimiterConfig().getLimitForPeriod(),
                actuatorLimiter.getRateLimiterConfig().getLimitForPeriod());
        assertNotEquals(
                errorLimiter.getRateLimiterConfig().getLimitForPeriod(),
                actuatorLimiter.getRateLimiterConfig().getLimitForPeriod());

        // Verify they have different names
        assertNotEquals(standardLimiter.getName(), errorLimiter.getName());
        assertNotEquals(standardLimiter.getName(), actuatorLimiter.getName());
        assertNotEquals(errorLimiter.getName(), actuatorLimiter.getName());
    }

    @Test
    void rateLimiterRegistry_ShouldContainAllLimiters() {
        RateLimiterRegistry registry = configuration.rateLimiterRegistry();

        // Create all limiters
        RateLimiter standardLimiter = configuration.standardApiLimiter(registry);
        RateLimiter errorLimiter = configuration.testErrorLimiter(registry);
        RateLimiter actuatorLimiter = configuration.actuatorLimiter(registry);

        // Verify registry contains all limiters by name
        assertTrue(registry.getAllRateLimiters().contains(standardLimiter));
        assertTrue(registry.getAllRateLimiters().contains(errorLimiter));
        assertTrue(registry.getAllRateLimiters().contains(actuatorLimiter));

        // Verify registry can retrieve limiters by name
        assertEquals(standardLimiter, registry.rateLimiter("standard-api"));
        assertEquals(errorLimiter, registry.rateLimiter("test-error"));
        assertEquals(actuatorLimiter, registry.rateLimiter("actuator"));
    }

    @Test
    void defaultRateLimiterConfig_ShouldHaveZeroWaitForPermission() {
        RateLimiterConfig config = configuration.defaultRateLimiterConfig();
        // Test additional config properties
        assertTrue(config.isWritableStackTraceEnabled());
        assertEquals(Duration.ZERO, config.getTimeoutDuration());
    }

    @Test
    void customLimiters_ShouldHaveCorrectSubscriptionPeriod() {
        RateLimiter errorLimiter = configuration.testErrorLimiter(rateLimiterRegistry);
        RateLimiter actuatorLimiter = configuration.actuatorLimiter(rateLimiterRegistry);

        // Verify refresh period settings are correctly applied
        assertEquals(Duration.ofSeconds(10), errorLimiter.getRateLimiterConfig().getLimitRefreshPeriod());
        assertEquals(
                Duration.ofMinutes(1), actuatorLimiter.getRateLimiterConfig().getLimitRefreshPeriod());
    }

    @Test
    void rateLimiter_ShouldEnforceLimitsUnderLoad() {
        // When
        RateLimiter limiter = configuration.testErrorLimiter(rateLimiterRegistry);
        int limitForPeriod = limiter.getRateLimiterConfig().getLimitForPeriod();
        int successfulAcquisitions = 0;

        // Try to acquire more permits than allowed
        for (int i = 0; i < limitForPeriod + 5; i++) {
            if (limiter.acquirePermission()) {
                successfulAcquisitions++;
            }
        }

        // Then
        assertEquals(limitForPeriod, successfulAcquisitions, "Should only allow limitForPeriod acquisitions");
    }

    @Test
    void rateLimiter_ShouldHandleTimeoutCorrectly() {
        // When
        RateLimiterConfig configWithTimeout = RateLimiterConfig.custom()
                .limitForPeriod(1)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(100))
                .build();

        RateLimiter limiter = rateLimiterRegistry.rateLimiter("timeout-test", configWithTimeout);

        // Then
        assertTrue(limiter.acquirePermission(), "First acquisition should succeed");
        assertFalse(limiter.acquirePermission(), "Second acquisition should fail due to timeout");
    }

    @Test
    void rateLimiterRegistry_ShouldHandleLimiterRemoval() {
        // When
        RateLimiter limiter = configuration.standardApiLimiter(rateLimiterRegistry);
        String limiterName = limiter.getName();

        // Then
        assertTrue(rateLimiterRegistry.getAllRateLimiters().contains(limiter), "Limiter should be in registry");

        // When removing the limiter
        rateLimiterRegistry.remove(limiterName);

        // Then
        assertFalse(
                rateLimiterRegistry.getAllRateLimiters().contains(limiter), "Limiter should be removed from registry");
    }

    @Test
    void rateLimiter_ShouldTrackMetrics() {
        // When
        RateLimiter limiter = configuration.standardApiLimiter(rateLimiterRegistry);
        int successfulAcquisitions = 0;

        // Simulate some usage
        for (int i = 0; i < 5; i++) {
            if (limiter.acquirePermission()) {
                successfulAcquisitions++;
            }
        }

        // Then
        assertTrue(successfulAcquisitions > 0, "Should have some successful acquisitions");
        // Check that metrics are being tracked without relying on specific method names
        assertNotNull(limiter.getMetrics(), "Metrics should not be null");
    }

    @Test
    void rateLimiter_ShouldMaintainStateBetweenRefreshes() {
        // When
        // Create a custom limiter with a very short refresh period for testing
        RateLimiterConfig customConfig = RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofMillis(100)) // Very short refresh period for testing
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiter limiter = rateLimiterRegistry.rateLimiter("test-refresh", customConfig);
        int initialAcquisitions = 0;

        // Use up all permits
        while (limiter.acquirePermission()) {
            initialAcquisitions++;
        }

        // Wait for refresh period using a more reliable approach
        // Instead of Thread.sleep, we'll use a polling approach with a timeout
        long startTime = System.currentTimeMillis();
        long timeout = 500; // 500ms timeout

        // Poll until we can acquire a permission or timeout
        boolean acquired = false;
        while (System.currentTimeMillis() - startTime < timeout) {
            if (limiter.acquirePermission()) {
                acquired = true;
                break;
            }
            // Small pause to avoid CPU spinning
            Thread.yield();
        }

        // Then
        assertTrue(acquired, "Should be able to acquire after refresh period");
        assertEquals(
                customConfig.getLimitForPeriod(),
                initialAcquisitions,
                "Should have used up all permits before refresh");
    }
}
