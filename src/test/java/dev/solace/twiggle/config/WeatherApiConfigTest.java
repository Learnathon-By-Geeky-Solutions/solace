package dev.solace.twiggle.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WeatherApiConfig.class)
@TestPropertySource(
        properties = {
            "weather.api.key=test-weather-key",
            "weather.api.base-url=https://test-weather-api.com",
            "weather.api.timeout=3000",
            "weather.api.connect-timeout=2000",
            "weather.api.read-timeout=4000"
        })
@EnableConfigurationProperties
class WeatherApiConfigTest {

    @Autowired
    private WeatherApiConfig weatherApiConfig;

    @Test
    void weatherApiConfig_shouldLoadPropertiesCorrectly() {
        assertThat(weatherApiConfig.getKey()).isEqualTo("test-weather-key");
        assertThat(weatherApiConfig.getBaseUrl()).isEqualTo("https://test-weather-api.com");
        assertThat(weatherApiConfig.getTimeout()).isEqualTo(3000);
        assertThat(weatherApiConfig.getConnectTimeout()).isEqualTo(2000);
        assertThat(weatherApiConfig.getReadTimeout()).isEqualTo(4000);
    }

    @Test
    void weatherApiConfig_shouldHaveDefaultValues() {
        // Create a new instance without overridden properties
        WeatherApiConfig config = new WeatherApiConfig();

        assertThat(config.getBaseUrl()).isEqualTo("https://api.worldweatheronline.com/premium/v1");
        assertThat(config.getTimeout()).isEqualTo(5000);
        assertThat(config.getConnectTimeout()).isEqualTo(5000);
        assertThat(config.getReadTimeout()).isEqualTo(5000);
    }
}
