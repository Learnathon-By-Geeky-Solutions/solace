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

        // Then
        assertNotNull(limiter, "Test error limiter should not be null");
        assertEquals("test-error", limiter.getName(), "Limiter name should match");
        assertEquals(30, limiter.getRateLimiterConfig().getLimitForPeriod(), "Limit for period should be 30");
        assertEquals(
                Duration.ofSeconds(10),
                limiter.getRateLimiterConfig().getLimitRefreshPeriod(),
                "Refresh period should be 10 seconds");
    }

    @Test
    void actuatorLimiter_ShouldCreateWithCorrectConfig() {
        // When
        RateLimiter limiter = configuration.actuatorLimiter(rateLimiterRegistry);

        // Then
        assertNotNull(limiter, "Actuator limiter should not be null");
        assertEquals("actuator", limiter.getName(), "Limiter name should match");
        assertEquals(60, limiter.getRateLimiterConfig().getLimitForPeriod(), "Limit for period should be 60");
        assertEquals(
                Duration.ofMinutes(1),
                limiter.getRateLimiterConfig().getLimitRefreshPeriod(),
                "Refresh period should be 1 minute");
    }
}
