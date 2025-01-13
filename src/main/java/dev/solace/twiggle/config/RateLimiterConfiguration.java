package dev.solace.twiggle.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfiguration {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.of(RateLimiterConfig.custom()
                .limitForPeriod(300)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build());
    }

    @Bean
    public RateLimiter standardApiLimiter(RateLimiterRegistry rateLimiterRegistry) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(300)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();

        return rateLimiterRegistry.rateLimiter("standard-api", config);
    }

    @Bean
    public RateLimiter testErrorLimiter(RateLimiterRegistry rateLimiterRegistry) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(30)
                .limitRefreshPeriod(Duration.ofSeconds(10))
                .timeoutDuration(Duration.ZERO)
                .build();

        return rateLimiterRegistry.rateLimiter("test-error", config);
    }

    @Bean
    public RateLimiter actuatorLimiter(RateLimiterRegistry rateLimiterRegistry) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(60)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();

        return rateLimiterRegistry.rateLimiter("actuator", config);
    }

    // Add Duration bean for test configuration
    @Bean
    public Duration limitRefreshPeriod() {
        return Duration.ofMinutes(1);
    }

    // Add RateLimiterConfig bean for test configuration
    @Bean
    public RateLimiterConfig rateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(300)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();
    }
}
