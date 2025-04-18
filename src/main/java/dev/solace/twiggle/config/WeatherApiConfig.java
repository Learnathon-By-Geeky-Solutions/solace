package dev.solace.twiggle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the World Weather Online API.
 */
@Configuration
@ConfigurationProperties(prefix = "weather.api")
@Data
public class WeatherApiConfig {
    private String key;
    private String baseUrl = "https://api.worldweatheronline.com/premium/v1";
    private int timeout = 5000; // Connection timeout in milliseconds
    private int connectTimeout = 5000; // Connection timeout in milliseconds
    private int readTimeout = 5000; // Read timeout in milliseconds
}
