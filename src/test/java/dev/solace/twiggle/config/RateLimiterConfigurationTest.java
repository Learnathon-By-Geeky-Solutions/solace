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
}
